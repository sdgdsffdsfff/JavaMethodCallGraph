package CGG.util;

/**
 * 缓存路径工具
 */
public class CachePathUtils {

    /**
     * 生成Jar包CallGraph的缓存路径
     *
     * @param basicCacheDir
     * @param groupId
     * @param artifactId
     * @return
     */
    public static String generateArtifactIdCachePath(String basicCacheDir, String groupId, String artifactId, String version) {
        return basicCacheDir + "/" + groupId + "/" + artifactId + "/" + version;
    }

}
