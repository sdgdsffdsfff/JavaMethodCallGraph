package CGG.util;


import CGG.config.CGGConfig;
import CGG.java_callgraph.javacg.core.JavaCallGraphCore;
import CGG.java_callgraph.javacg.stat.CheckMapResult;
import com.google.gson.Gson;

import java.io.File;
import java.util.*;


/**
 * CallGraph对外方法工具类
 */
public class JavaCallGraphUtils {

    private static final String JAVA_CALL_GRAPH_CACHE_DIR = CGGConfig.OUTPUT_DIR + "/cache/cg";
    private static final String JAVA_CALL_GRAPH_ERROR_DIR = CGGConfig.OUTPUT_DIR + "/cache/error";


    /**
     * 根据groupId artifactId version 三元组获取对应jar包的CallGraph
     * 采用缓存策略，
     * 1. 当外部程序通过本方法获取CallGraph时，首先在本地磁盘查找缓存，如果有缓存直接读取缓存并返回
     * 2. 如果没有缓存，则使用Java-CallGraph生成CallGraph并将结果缓存在本地磁盘
     *
     * @param groupId
     * @param artifactId
     * @param version
     * @param jarName    Jar包的文件名
     * @param jarPath    Jar包路径
     * @return CallGraph结果
     * @throws Exception
     */
    public static Map<String, List<String>> generateInvokeMap(String groupId, String artifactId, String version, String jarName, String jarPath) throws Exception {

        /**
         * 获取缓存
         */
        Map<String, List<String>> invokeMap = getCGCache(groupId, artifactId, version, jarName);

        if (invokeMap != null) {
            return invokeMap;
        }
        String[] invokeStringList;
        try {
            invokeStringList = JavaCallGraphCore.getJarInvokeMethodList(groupId, artifactId, version, jarPath, jarName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (invokeStringList == null) {
            return null;
            //throw new Exception("error invoke string list!");
        } else {
            invokeMap = JavaCallGraphCore.generateJarInvokeListToMap(invokeStringList);
        }

        /**
         * 移除JDK方法
         */
        invokeMap = JavaCallGraphCore.removeJDKMethod(invokeMap); //️

        String cachePath = CachePathUtils.generateArtifactIdCachePath(JAVA_CALL_GRAPH_CACHE_DIR, groupId, artifactId, version);
        File cacheFile = new File(cachePath);
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
        FileUtil.writeFile(cachePath + "/" + jarName + ".txt", new Gson().toJson(invokeMap));
        return invokeMap;
    }

    /**
     * 根据 groupId artifactId version 三元组得到对应Jar包的CallGraph
     * 如果有缓存，返回结果
     * 如果没有缓存，返回Null
     *
     * @param groupId
     * @param artifactId
     * @return 缓存结果 or Null
     */
    private static Map<String, List<String>> getCGCache(String groupId, String artifactId, String version, String jarName) {
        String jarCacheFilePath = CachePathUtils.generateArtifactIdCachePath(JAVA_CALL_GRAPH_CACHE_DIR, groupId, artifactId, version) + "/" + jarName + ".txt";
        File cacheFile = new File(jarCacheFilePath);

        //如果Jar包CallGraph生成过程出现问题，需要将该Jar包结果保存在 errorCacheFile 文件中，
        //读取缓存时，返回一个空的HashMap
        String errorCacheFile = CachePathUtils.generateArtifactIdCachePath(JAVA_CALL_GRAPH_ERROR_DIR, groupId, artifactId, version) + "/" + jarName + ".txt";
        if (new File(errorCacheFile).exists()) {
            return new HashMap<>();
        }

        /**
         * 没有缓存，返回Null
         */
        if (!cacheFile.exists()) {
            return null;
        }

        /**
         * 有缓存，返回缓存结果
         */
        String cache = FileUtil.read(cacheFile.getAbsolutePath());
        Map<String, List<String>> cacheMap = new HashMap<>();
        Gson gson = new Gson();
        cacheMap = gson.fromJson(cache, cacheMap.getClass());
        return cacheMap;
    }


    /*
     * 得到以指定方法为根节点的调用关系
     *
     * @param jarName
     * @param jarPath
     * @param methodName
     * @return
     * @throws Exception
     * */
    public static Map<String, List<String>> getInvokeMethodByMethodName(String groupId, String artifactId, String version, String jarName, String jarPath, String methodName) throws Exception {
        //之前预备工作的到的map，存储所有方法
        Map<String, List<String>> jarInvokeMethodMap = generateInvokeMap(groupId, artifactId, version, jarName, jarPath);
        if (jarInvokeMethodMap == null) {
            return null;
        }
        //最后结果
        Map<String, List<String>> resultInvokeMethodMap = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(methodName);

        while (!queue.isEmpty()) {
            String method = queue.poll();
            CheckMapResult checkMapResult = JavaCallGraphCore.checkJarInvokeMethodMapContains(jarInvokeMethodMap, method);
            if (checkMapResult.getSuccess()) {
                //if (jarInvokeMethodMap.containsKey(method)) {
                List<String> invokeMethodList = jarInvokeMethodMap.get(checkMapResult.getMethodString());
                if (resultInvokeMethodMap.containsKey(checkMapResult.getMethodString())) {
                    //已经加过，不用再处理
                } else {
                    //如果该方法还没加过，加入key-value，且把调用的方法加进queue
                    resultInvokeMethodMap.put(checkMapResult.getMethodString(), new ArrayList<>());
                    for (String invokeMethod : invokeMethodList) {
                        resultInvokeMethodMap.get(checkMapResult.getMethodString()).add(invokeMethod);
                        queue.offer(invokeMethod);
                    }
                }
            }
        }

        //如果本身没有调用方法，要将其本身加入其中
        if (resultInvokeMethodMap.size() == 0) {
            resultInvokeMethodMap.put(methodName, new ArrayList<>());
        }
        return resultInvokeMethodMap;
    }


}
