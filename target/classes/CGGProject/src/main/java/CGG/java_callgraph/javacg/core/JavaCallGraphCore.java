package CGG.java_callgraph.javacg.core;

import com.google.gson.Gson;
import CGG.config.CGGConfig;
import CGG.java_callgraph.javacg.stat.CheckMapResult;
import CGG.java_callgraph.javacg.stat.ClassVisitor;
import CGG.util.CachePathUtils;
import CGG.util.JavaMethodUtil;
import CGG.util.FileUtil;
import org.apache.bcel.classfile.ClassParser;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * JavaCallGraph 中间层的核心代码
 */
public class JavaCallGraphCore {

    private static final String JAVA_SUPERCLASS_CACHE_DIR = CGGConfig.OUTPUT_DIR + "/cache/sp";

    /**
     * 得到jar包的调用关系String[]
     *
     * @param jarPath
     * @param jarName
     * @return
     */
    public static String[] getJarInvokeMethodList(String groupId, String artifactId, String version, String jarPath, String jarName) {
        /**
         * lambda表达式
         * 传入参数ClassParser cp
         * 返回ClassVisitor classVisitor
         */
        Function<ClassParser, ClassVisitor> getClassVisitor =
                (ClassParser cp) -> {
                    try {
                        return new ClassVisitor(cp.parse());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                };

        try {
            File f = new File(jarPath);
            if (!f.exists()) {
                System.err.println("Jar file " + jarPath + " does not exist");
                return null;
            }
            try (JarFile jar = new JarFile(f)) {
                //是否需要缓存子类父类关系图
                boolean shouldCacheSuperClass = true;
                File cpCache = new File(JAVA_SUPERCLASS_CACHE_DIR + jarName + ".txt");
                if (cpCache.exists()) {
                    shouldCacheSuperClass = false;
                }
                Enumeration jarEntries = jar.entries();
                Stream<JarEntry> entries = enumerationAsStream(jarEntries);
                Map<String, String> classSuperMap = new HashMap<>();
                boolean finalShouldCacheSuperClass = shouldCacheSuperClass;
                String methodCalls = null;
                try {
                    methodCalls = entries.
                            flatMap(e -> {
                                if (e.isDirectory() || !e.getName().endsWith(".class"))
                                    return (new ArrayList<String>()).stream();
                                ClassParser cp = new ClassParser(jarPath, e.getName());

                                ClassVisitor classVisitor = getClassVisitor.apply(cp).start();

                                Stream<String> result = classVisitor.methodCalls().stream();
                                String currentClass = e.getName();
                                if (finalShouldCacheSuperClass) {
                                    String superClassName = classVisitor.getSuperClassName();
                                    currentClass = currentClass.substring(0, currentClass.length() - 6).replaceAll("/", ".");
                                    classSuperMap.put(currentClass, superClassName);
                                }
                                return result;
                            }).
                            map(s -> s + "\n").
                            reduce(new StringBuilder(),
                                    StringBuilder::append,
                                    StringBuilder::append).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (shouldCacheSuperClass) {
                    String cachePath = CachePathUtils.generateArtifactIdCachePath(JAVA_SUPERCLASS_CACHE_DIR, groupId, artifactId, version);
                    File spCacheFile = new File(cachePath);
                    if (!spCacheFile.exists()) {
                        spCacheFile.mkdirs();
                    }
                    FileUtil.writeFile(cachePath + "/" + jarName + ".txt", new Gson().toJson(classSuperMap));
                }
                if (methodCalls == null) {
                    return null;
                } else {
                    return methodCalls.split("\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error while processing jar: " + e.getMessage());
            e.printStackTrace();
        }

        return null;

    }

    private static <T> Stream<T> enumerationAsStream(Enumeration<T> e) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new Iterator<T>() {
                            public T next() {
                                return e.nextElement();
                            }

                            public boolean hasNext() {
                                return e.hasMoreElements();
                            }
                        },
                        Spliterator.ORDERED), false);
    }

    /**
     * 将调用关系的String[]转化为Map
     *
     * @param invokeStringList
     * @return
     */
    public static Map<String, List<String>> generateJarInvokeListToMap(String[] invokeStringList) {
        Map<String, List<String>> resultList = new HashMap<>();
        for (String invokeString : invokeStringList) {
            if (invokeString.startsWith("M:")) {
                String[] invokeStrings = invokeString.split(" ");
                String callerMethodString = invokeStrings[0].split("M:")[1].replace(":", ".");
                String invokeMethodString = invokeStrings[1].replace(":", ".").substring(3);
                if (!resultList.containsKey(callerMethodString)) {
                    resultList.put(callerMethodString, new ArrayList<>());
                }
                resultList.get(callerMethodString).add(invokeMethodString);
            }
        }
        return resultList;
    }


    /**
     * 去除invokeMap中的JDK方法
     * <p>
     * 以Key遍历，得到每个Key调用的方法列表,再逐一检查List中的方法
     *
     * @param invokeMap
     */
    public static Map<String, List<String>> removeJDKMethod(Map<String, List<String>> invokeMap) {
        Set<String> jdkMethodSet = new HashSet<>();
        Set<String> normalMethodSet = new HashSet<>();

        for (Map.Entry<String, List<String>> invokeMapEntry : invokeMap.entrySet()) {
            String method = invokeMapEntry.getKey();
            List<String> invokedMethodList = invokeMapEntry.getValue();

            for (int i = invokedMethodList.size() - 1; i >= 0; i--) {
                String invokedMethod = invokedMethodList.get(i);
                /**
                 * 如果方法在jdkMethodSet中说明是jdkMethod，直接删除
                 */
                boolean isInJdkMethodSet = jdkMethodSet.contains(invokedMethod);
                if (isInJdkMethodSet) {
                    invokedMethodList.remove(i);
                    continue;
                }

                /**
                 * 如果方法在NormalMethodSet中出现，说明不是JDKMethod，直接跳过循环，不需要再跟你局文件判断
                 */
                boolean isInNormalMethodSet = normalMethodSet.contains(invokedMethod);
                if (isInNormalMethodSet) {
                    continue;
                }

                /**
                 * 如果两个set都没有出现，说明还没有处理过这个方法
                 * 从文档处理
                 */

                try {
                    boolean isJDKMethod = JavaMethodUtil.isJDKMethod2(invokedMethod);
                    if (isJDKMethod) {
                        invokedMethodList.remove(i);
                        jdkMethodSet.add(invokedMethod);
                    } else {
                        normalMethodSet.add(invokedMethod);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return invokeMap;
    }

    /**
     * 检查某一个Callgraph中是否含有某个方法
     *
     * @param jarInvokeMethodMap
     * @param method
     * @return
     */
    public static CheckMapResult checkJarInvokeMethodMapContains(Map<String, List<String>> jarInvokeMethodMap, String method) {
        boolean constructor = JavaMethodUtil.isConstructor(method);
        if (constructor) {
            //如果是构造函数
            method = JavaMethodUtil.formatConstructor(method);
        }
        /**
         * 直接找到 or 经过转换后通过构造函数找到
         */
        if (jarInvokeMethodMap.containsKey(method)) {
            return new CheckMapResult(method, true);
        }
        String s = method;

        /**
         * 没有括号，说明是变量，不考虑
         */
        int index2 = s.indexOf('(');
        if (index2 == -1) {
            return new CheckMapResult(method, false);
        }

        String subS = s.substring(0, index2);
        int index = subS.lastIndexOf('.');
        String className = s.substring(0, index);//类名
        String methodName = s.substring(index + 1, index2);//方法名
        String params = s.substring(index2);//参数列表
        //initClassNames(classNames, className);
        while (true) {
            /**
             * 从后到前每次替换一个.为$（内部类）,直到全部替换完成
             */
            int dotIndex = className.lastIndexOf(".");
            if (dotIndex == -1) {
                return new CheckMapResult("", false);
            }
            StringBuilder classNameSb = new StringBuilder(className);
            classNameSb.replace(dotIndex, dotIndex + 1, "$");
            className = classNameSb.toString();
            String totalMethodName = className + "." + methodName + params;
            if (jarInvokeMethodMap.containsKey(totalMethodName)) {
                return new CheckMapResult(totalMethodName, true);
            }
        }
    }
}