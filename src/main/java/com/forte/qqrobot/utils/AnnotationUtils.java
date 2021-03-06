package com.forte.qqrobot.utils;

import com.forte.lang.Language;
import com.forte.qqrobot.anno.ByNameFrom;
import com.forte.qqrobot.anno.ByNameType;
import com.forte.qqrobot.anno.Constr;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.anno.depend.Beans;
import com.forte.qqrobot.exception.AnnotationException;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 对于一些注解的获取等相关的工具类
 *
 * @author ForteScarlet <[163邮箱地址]ForteScarlet@163.com>
 * @since JDK1.8
 **/
public class AnnotationUtils {

    /**
     * java原生注解所在包路径
     */
    private static final Package JAVA_ANNOTATION_PACKAGE = Target.class.getPackage();


    /**
     * 注解缓存，记录曾经保存过的注解与其所在类
     *
     */
    private static final Map<AnnotatedElement, Set<Annotation>> ANNOTATION_CACHE = new ConcurrentHashMap<>(32);

    /**
     * 此工具类中使用到的异常的语言前缀
     */
    private static final String LANG_EX_TAG_HEAD = "exception.annotation";

    /**
     * 获取语言结果字符串
     * @param tag       tag
     * @param format    格式化参数
     */
    private static String getLang(String tag, Object... format){
        return Language.format(LANG_EX_TAG_HEAD, tag, format);
    }


    /**
     * 尝试从一个类对象中获取到@Beans注解
     */
    public static Beans getBeansAnnotationIfListen(Class<?> from) {
        Beans fromClass = getAnnotation(from, Beans.class);
        if (fromClass != null) {
            return fromClass;
        }


        //类上没有，查询所有方法是否存在@Listen注解
        //因为@Listen上的@Beans注解都是一样的
        for (Method method : from.getMethods()) {
            Listen listenAnnotation = getAnnotation(method, Listen.class);
            if (listenAnnotation != null) {
                //Listen注解必定存在@Beans注解
                return listenAnnotation.annotationType().getAnnotation(Beans.class);
            }
        }

        //如果还是没有，返回null
        return null;
    }

    /**
     * 从某个类上获取注解对象，注解可以深度递归
     * 如果存在多个继承注解，则优先获取浅层第一个注解，如果浅层不存在，则返回第一个获取到的注解
     * 请尽可能保证仅存在一个或者一种继承注解，否则获取到的类型将不可控
     *
     * @param from           获取注解的某个类
     * @param annotationType 想要获取的注解类型
     * @return 获取到的第一个注解对象
     */
    public static <T extends Annotation> T getAnnotation(AnnotatedElement from, Class<T> annotationType) {
        return getAnnotation(from, annotationType, (Class<T>[]) new Class[]{});
    }

    /**
     * 从某个类上获取注解对象，注解可以深度递归
     * 如果存在多个继承注解，则优先获取浅层第一个注解，如果浅层不存在，则返回第一个获取到的注解
     * 请尽可能保证仅存在一个或者一种继承注解，否则获取到的类型将不可控
     *
     * @param from           获取注解的某个类
     * @param annotationType 想要获取的注解类型
     * @param ignored        获取注解列表的时候的忽略列表
     * @return 获取到的第一个注解对象
     */
    public static <T extends Annotation> T getAnnotation(AnnotatedElement from, Class<T> annotationType, Class<T>... ignored) {
        // 首先尝试获取缓存
        T cache = getCache(from, annotationType);
        if(cache != null){
            return cache;
        }


        //先尝试直接获取
        T annotation = from.getAnnotation(annotationType);

        //如果存在直接返回，否则查询
        if (annotation != null) {
            saveCache(from, annotation);
            return annotation;
        }

        // 获取target注解
        Target target = annotationType.getAnnotation(Target.class);
        // 判断这个注解能否标注在其他注解上，如果不能，则不再深入获取
        boolean annotationable = false;
        if (target != null) {
            for (ElementType elType : target.value()) {
                if (elType == ElementType.TYPE || elType == ElementType.ANNOTATION_TYPE) {
                    annotationable = true;
                    break;
                }
            }
        }

        Annotation[] annotations = from.getAnnotations();
        annotation = annotationable ? getAnnotationFromArrays(annotations, annotationType, ignored) : null;


        // 如果还是获取不到，看看查询的注解类型有没有对应的ByNameType
        if (annotation == null) {
            annotation = getByNameAnnotation(from, annotationType);
        }

        // 如果无法通过注解本身所指向的byName注解获取，看看有没有反向指向此类型的注解
        // 此情况下不进行深层获取
        if(annotation == null){
            annotation = getAnnotationFromByNames(annotations, annotationType);
        }

        // 如果最终不是null，计入缓存
        if(annotation != null){
            saveCache(from, annotation);
        }

        return annotation;
    }



    /**
     * 逆向查询，把ByName转化为正常的注解。
     * @param annotations    获取源头上拿到的注解列表，例如类上、字段上等等。
     * @param annotationType 想要获取的注解类型。
     */
    private static <T extends Annotation> T getAnnotationFromByNames(Annotation[] annotations, Class<T> annotationType){
        // 获取所有的注解
        return Arrays.stream(annotations).map(a -> {
            // 这个注解a存在@ByNameFrom
            ByNameFrom byNameFrom = a.annotationType().getAnnotation(ByNameFrom.class);
            if(byNameFrom == null){
                return null;
            }else{
                return new AbstractMap.SimpleEntry<>(a, byNameFrom);
            }
        })
                .filter(Objects::nonNull)
                .filter(a -> a.getValue().value().equals(annotationType))
                .map(a -> AnnotationByNameUtils.byName(a.getKey(), annotationType))
                .findFirst().orElse(null);
    }


    /**
     * 通过参数对象获取，且是通过byName注解获取
     * @param from              来源
     * @param annotationType    父注解类型
     */
    private static <T extends Annotation> T getByNameAnnotation(AnnotatedElement from, Class<T> annotationType){
        // 如果还是获取不到，看看查询的注解类型有没有对应的ByNameType
        ByNameType byNameType = annotationType.getAnnotation(ByNameType.class);
        if (byNameType != null) {
            // 存在byNameType，看看是啥
            Class<? extends Annotation> byNameAnnotationType = byNameType.value();
            // 尝试通过这个ByName获取真正的对应注解
            // 获取ByName注解的时候不再使用深层获取，而是直接获取
            Annotation byNameOnFrom = from.getAnnotation(byNameAnnotationType);
            return AnnotationByNameUtils.byName(byNameOnFrom, annotationType);
        } else {
            return null;
        }
    }

    /**
     * @param array
     * @param annotationType
     * @param <T>
     * @return
     */
    private static <T extends Annotation> T getAnnotationFromArrays(Annotation[] array, Class<T> annotationType, Class<T>... ignored) {
        //先浅查询第一层
        //全部注解
        Annotation[] annotations = Arrays.stream(array)
                .filter(a -> {
                    for (Class<? extends Annotation> atype : ignored) {
                        if (a.annotationType().equals(atype)) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(a -> {
                    if (a == null) {
                        return false;
                    }
                    //如果此注解的类型就是我要的，直接放过
                    if (a.annotationType().equals(annotationType)) {
                        return true;
                    }
                    //否则，过滤掉java原生注解对象
                    //通过包路径判断
                    if (JAVA_ANNOTATION_PACKAGE.equals(a.annotationType().getPackage())) {
                        return false;
                    }
                    return true;
                }).toArray(Annotation[]::new);


        if (annotations.length == 0) {
            return null;
        }

        Class<? extends Annotation>[] annotationTypes = new Class[annotations.length];
        for (int i = 0; i < annotations.length; i++) {
            annotationTypes[i] = annotations[i].annotationType();
        }

        Class<T>[] newIgnored = new Class[annotationTypes.length + ignored.length];
        System.arraycopy(ignored, 0, newIgnored, 0, ignored.length);
        System.arraycopy(annotationTypes, 0, newIgnored, ignored.length, annotationTypes.length);

        //遍历
        for (Annotation a : annotations) {
            T annotationGet = a.annotationType().getAnnotation(annotationType);
            if (annotationGet != null) {
                return annotationGet;
            }
        }

        //如果浅层查询还是没有，递归查询
        //再次遍历
        for (Annotation a : annotations) {
            T annotationGet = getAnnotation(a.annotationType(), annotationType, newIgnored);
            if (annotationGet != null) {
                return annotationGet;
            }
        }

        //如果还是没有找到，返回null
        return null;
    }

    /**
     * 获取类中标注了@Constr注解的方法。
     * 如果有多个，获取其中某一个；
     * 如果出现了：
     * - 注解不存在静态方法上、
     * - 方法返回值不是这个类本身或者子类
     * 则会抛出异常
     *
     * @param clz class对象
     * @return 可能存在@Constr注解的静态方法
     * @throws AnnotationException 如果不是静态方法、没有返回值、返回值不是这个类型或者这个类型的字类类型却使用了@Constr注解
     *                             便会抛出此异常
     *                             see lang:
     *                             <ul>
     *                              <li>exception.annotation.notStatic</li>
     *                              <li>exception.annotation.needReturn</li>
     *                              <li>exception.annotation.returnTypeWrong</li>
     *                             </ul>
     */
    public static Method getConstrMethod(Class clz) {
        return Arrays.stream(clz.getDeclaredMethods()).filter(m -> {
            Constr constr = getAnnotation(m, Constr.class);
            if (constr != null) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    throw new AnnotationException(clz, m, Constr.class, getLang("notStatic"));
                }

                if (m.getReturnType().equals(void.class)) {
                    throw new AnnotationException(clz, m, Constr.class, getLang("needReturn"));
                }

                if (!FieldUtils.isChild(m.getReturnType(), clz)) {
                    throw new AnnotationException(clz, m, Constr.class, getLang("returnTypeWrong"));
                }

                return true;
            } else {
                return false;
            }
        }).findAny().orElse(null);
    }

    /**
     * 从缓存中获取缓存注解
     * @param from          来源
     * @param annotatedType 注解类型
     * @return  注解缓存，可能为null
     */
    private static <T extends Annotation> T getCache(AnnotatedElement from, Class<T> annotatedType){
        Set<Annotation> list = ANNOTATION_CACHE.get(from);
        if(list != null){
            // 寻找
            for (Annotation a : list) {
                if(a.annotationType().equals(annotatedType)){
                    return (T) a;
                }
            }
        }
        // 找不到，返回null
        return null;
    }

    /**
     * 记录一条缓存记录。
     */
    private static boolean saveCache(AnnotatedElement from, Annotation annotation){
        Set<Annotation> list;
        synchronized (ANNOTATION_CACHE) {
            list = ANNOTATION_CACHE.get(from);
            // 如果为空，新建一个并保存
            if(list == null){
                list = new CopyOnWriteArraySet<>();
                ANNOTATION_CACHE.put(from, list);
            }
        }

        // 记录这个注解
        return list.add(annotation);
    }

    /**
     * 清除缓存
     */
    public static void cleanCache(){
        ANNOTATION_CACHE.clear();
    }

}
