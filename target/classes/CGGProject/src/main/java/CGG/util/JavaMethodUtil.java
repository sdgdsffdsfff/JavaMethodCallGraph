package CGG.util;
/*

import cn.edu.fudan.se.effort.bean.MethodInJarResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
*/

import CGG.config.CGGConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 对java方法的一些处理
 */
public class JavaMethodUtil {

    private static final String API_DOC_PATH = "D:\\doc\\jdk-8u181-docs-all\\docs\\api\\";
    public static String JAR_TOOL_PATH = "C:\\jd-cli.jar";
    private static final String DECOMPILE_DIR = "H:/shibowen/decompile/";
    private static final String METHOD_IN_JAR_CACHE_DIR = "H:/shibowen/callgraph/methodInJarCache/";


    private static final String JDK_DOC_PATH = CGGConfig.OUTPUT_DIR + "/jdk_doc";


    //方法标记
    public static final int METHOD_TYPE = 0;
    //变量标记
    public static final int VARIABLE_TYPE = 1;


    @Deprecated
    /**
     * 是否是jdk内的方法
     * 判断是否存在该类的html文件，如果存在说明是jdk内的方法，否则不是
     *
     * @param className eg:java.lang.StringBuilder
     * @return
     * @see #isJDKMethod2(String)
     */
    public static boolean isJDKMethod(String className) {

        //把 java.lang.StringBuilder 转变为 java/lang/StringBuilder
        className = className.replaceAll("\\.", "/");
        className = className.replaceAll("\\$", ".");
        String fileName = API_DOC_PATH + className + ".html";

        File file = new File(fileName);
        return file.exists();
    }

    public static InputStream readFileInputStream(String fileName) {
        File file = new File(fileName);
        InputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            return fileInputStream;
        }
    }

    /*    *//**
     * 反编译jar包
     * @param jarPath
     * @param outPath
     * @return 输出路径
     *//*
    public static String decompileJar(String jarPath, String outPath) {
        File file = new File(jarPath);

        if (!file.exists()) {
            return null;
        }

        String decompilePath = outPath + "/" + file.getName().substring(0, file.getName().length() - 4) + "_decompile/";
        File testFile = new File(decompilePath);//testFile用来查看是否已经反编译
        if (testFile.isDirectory() && testFile.exists()) {
            //如果是个目录且存在
            return decompilePath;
        }
*//*        File[] files = f.listFiles();
        List<String> shs = new ArrayList<>();*//*
        String sh = "java -jar " + JAR_TOOL_PATH + " --outputDir ";
        if (file.getAbsolutePath().endsWith(".jar")) {
            sh += outPath + "/" + file.getName().substring(0, file.getName().length() - 4) + "_decompile ";
            sh += file.getPath().replace('\\', '/');
            // System.out.println(sh);
            System.out.println(sh);
            ExecuteCmd.execToString(sh);
        }
        return decompilePath;
    }*/
    /*
     *//**
     * 使用jdt得到一个java文件compilationUnit用于解析
     * @param fileName
     * @return
     *//*
    public static CompilationUnit getCompilationUnit(String fileName) {
        try {
            return getCompilationUnit(readFileInputStream(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    *//**
     * 获取一个文件中所有的方法
     *
     * @param javaFilePath
     * @return
     *//*

    public static List<BodyDeclaration> getAllMethodInFile(String javaFilePath) {

        CompilationUnit compilationUnit = getCompilationUnit(javaFilePath);

        if (compilationUnit.types().size() == 0) {
            return new ArrayList<>();
        }

        AbstractTypeDeclaration typeDeclaration;
        if (compilationUnit.types().get(0) instanceof AnnotationTypeDeclaration) {
            typeDeclaration = (AnnotationTypeDeclaration) compilationUnit.types().get(0);
        } else if (compilationUnit.types().get(0) instanceof EnumDeclaration) {
            typeDeclaration = (EnumDeclaration) compilationUnit.types().get(0);
        } else {
            typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
        }
        List<BodyDeclaration> bodyDeclarationList = typeDeclaration.bodyDeclarations();


        return bodyDeclarationList;
    }


    private static CompilationUnit getCompilationUnit(InputStream is) throws Exception {

        ASTParser astParser = ASTParser.newParser(AST.JLS8);
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
        byte[] input = new byte[bufferedInputStream.available()];
        bufferedInputStream.read(input);
        bufferedInputStream.close();
        Map options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);

//        astParser.setEnvironment(null, null, null, true);
//        astParser.setUnitName("ClusterAction2");//需要与代码文件的名称一致
//        astParser.setResolveBindings(true);
//        astParser.setBindingsRecovery(true);

        astParser.setCompilerOptions(options);
        astParser.setSource(new String(input).toCharArray());
        CompilationUnit result = (CompilationUnit) (astParser.createAST(null));
        return result;
    }*/

    /**
     * 得到方法名
     *
     * @param method com.xxx.xxx.yyy(aaa,bbb) or com.xxx.xxx$xxx.yyy(aaa,bbb,ccc)
     * @return
     */
    public static String getMethodName(String method) {
        String preMethod = method.split("\\(")[0];
        int lastDotIndex = preMethod.lastIndexOf(".");
        return preMethod.substring(lastDotIndex + 1);

    }


    /**
     * 判断是否是构造函数
     * 判断依据为类名和方法名是否相同
     *
     * @param method com.xxx.xxx.method(java.lang.String) or com.xxx.xxx&yyy.method(java.lang.String)
     * @return
     */
    public static boolean isConstructor(String method) {
        String preMethod = method.split("\\(")[0];//com.xxx.xxx.method or com.xxx.xxx&yyy.method
        String[] preMethods = preMethod.split("\\.");
        int size = preMethods.length;

        String methodName = preMethods[size - 1];
        String className = preMethods[size - 2];
        return methodName.equals(className);
    }

    /**
     * 将构造函数转化为 xxx.xxx.<init>(java.lang.String)格式
     *
     * @param method
     */
    public static String formatConstructor(String method) {
        String preMethod = method.split("\\(")[0];
        String postMethod = "(" + method.split("\\(")[1];
        int lastDotIndex = preMethod.lastIndexOf(".");
        return method.substring(0, lastDotIndex + 1) + "<init>" + postMethod;
    }

    /**
     * 获得方法所在的类名
     *
     * @param method org.apache.log4j.NDC$sdf.clear() or org.apache.log4j.NDC.clear()
     * @return class org.apache.log4j.NDC$sdf or org.apache.log4j.NDC
     */
    public static String getMethodClass(String method) {
        String invokeMethodPath = method.split("\\(")[0];
        String path = "";
        String[] invokeMethods = invokeMethodPath.split("\\.");
        int size = invokeMethods.length;
        int segmentIndex = 0;
        for (String invokeMethodSegment : invokeMethods) {
            if (segmentIndex == size - 1) {
                break;
            }
            segmentIndex++;
            if (invokeMethodSegment.contains("$")) {
                invokeMethodSegment = invokeMethodSegment.split("$")[0];
            }
            path += invokeMethodSegment + ".";
        }
        path = path.substring(0, path.length() - 1);
        return path;
    }

    public static String getVariableClass(String method) {
        String invokeMethodPath = method;
        String path = "";
        String[] invokeMethods = invokeMethodPath.split("\\.");
        int size = invokeMethods.length;
        int segmentIndex = 0;
        for (String invokeMethodSegment : invokeMethods) {
            if (segmentIndex == size - 1) {
                break;
            }
            segmentIndex++;
            if (invokeMethodSegment.contains("$")) {
                invokeMethodSegment = invokeMethodSegment.split("$")[0];
            }
            path += invokeMethodSegment + ".";
        }
        path = path.substring(0, path.length() - 1);
        return path;

    }

    /**
     * 获取参数列表
     *
     * @param methodName
     * @return
     */
    public static List<String> getParamNames(String methodName) {

        int left = methodName.indexOf("(");
        int right = methodName.indexOf(")");

        if (left == -1 || right == -1) {
            return null;
        }
        if (right - left == 1) {
            return new ArrayList<>();
        }

        String paramsString = methodName.substring(left + 1, right);
        String[] params = paramsString.split(",");
        return Arrays.asList(params);
    }

    public static boolean match(String prevMethod, String nextMethod) {

        String prevMethodName = JavaMethodUtil.getMethodName(prevMethod);
        List<String> prevParams = JavaMethodUtil.getParamNames(prevMethod);
        String nextMethodName = JavaMethodUtil.getMethodName(nextMethod);
        List<String> nextParams = JavaMethodUtil.getParamNames(nextMethod);

        if (!prevMethodName.equals(nextMethodName)) {
            return false;
        }

        int prevParamsSize = prevParams.size();
        int nextParamSize = nextParams.size();

        if (prevParamsSize != nextParamSize) {
            return false;
        }

        for (int paramIndex = 0; paramIndex < prevParamsSize; paramIndex++) {

            String prevParam = prevParams.get(paramIndex);
            String nextParam = nextParams.get(paramIndex);

            if (prevParam.equals(nextParam) && !prevParam.endsWith(prevParam) && !nextParam.endsWith(prevParam)) {
                //没有匹配上
                return false;
            }
        }
        return true;
    }

    /**
     * @param candidatesMap
     * @param sampleJarName
     * @param sampleJarVersion
     * @return
     */
    public static List<String> getCandidates(Map<String, List<String>> candidatesMap, String sampleJarName, String sampleJarVersion) {

        if (candidatesMap.containsKey(sampleJarName)) {
            List<String> candidates = candidatesMap.get(sampleJarName);

            int startIndex = candidates.indexOf(sampleJarVersion);

            if (startIndex == -1) {
                return null;
            }

            return candidates.subList(startIndex, candidates.size());
//            return candidates.subList(startIndex, startIndex+1);
        }

        return null;

    }

    /**
     * 比较两个jar包的version
     *
     * @param prevVersion
     * @param nextVersion
     * @return
     */
    private static int compareVersion(String prevVersion, String nextVersion) {

        String[] preVersions = prevVersion.split("\\.");
        String[] nextVersions = nextVersion.split("\\.");

        int preSize = preVersions.length;
        int nextSize = nextVersions.length;

        int size = Math.min(preSize, nextSize);

        for (int index = 0; index < size; index++) {

            int prevV = Integer.parseInt(preVersions[index]);
            int nextV = Integer.parseInt(nextVersions[index]);

            if (prevV < nextV) return -1;
            if (prevV > nextV) return 1;
        }


        return 1;
    }


    /*
     */
/**
 * 检查method是否在CandidateJar包中
 *
 * @param method
 * @param candidateJar
 * @param libDir
 *//*

    public static boolean methodInJar(String method, String candidateJar, String libDir) {

        return JDTUtil.checkMethodExist(candidateJar, method, libDir);

    }
*/


    /**
     * 获取jar包的版本号和artifactName
     *
     * @param jarName xxx-1.1.1
     * @return
     */

    public static String[] getJarInfo(String jarName) {

        String artifactName = "";

        String[] jarNames = jarName.split("-");
        for (String jarNameItem : jarNames) {
            if (jarNameItem.toLowerCase().charAt(0) - 'a' >= 0 && jarNameItem.toLowerCase().charAt(0) - 'z' <= 0) {
                artifactName += "-" + jarNameItem;
            } else {
                break;
            }
        }
        artifactName = artifactName.substring(1);

        if (artifactName.length() + 1 >= jarName.length()) {
            return null;
        }
        String version = jarName.substring(artifactName.length() + 1);

        String[] result = new String[2];
        result[0] = artifactName;
        result[1] = version;

        return result;

    }

    /**
     * @param jarName
     * @return
     */
    public static boolean jarExist(String jarName) {

        File jarFile = new File("H:/wangying/lib_all/" + jarName);
        return jarFile.exists();

    }
/*
    public static MethodInJarResult getMethodInJar(String jarName, String jarDir, String method, String projectId) throws Exception {
        return getMethodInJar(jarName, jarDir, method, projectId, false, false);
    }

    public static MethodInJarResult getMethodInJar(String jarName, String jarDir, String method, String projectId, boolean searchParentClass, boolean searchThirdPartyJar) throws Exception {
        return getMethodInJar(jarName, jarDir, method, METHOD_TYPE, projectId, searchParentClass, searchThirdPartyJar);
    }


    *//**
     * 在Jar包中搜索方法
     *
     * @param jarName
     * @param jarDir
     * @param method
     * @param searchParentClass
     * @param searchThirdPartyJar
     * @return
     * @throws Exception
     *//*
    public static MethodInJarResult getMethodInJar(String jarName, String jarDir, String method, int type, String projectId, boolean searchParentClass, boolean searchThirdPartyJar) throws Exception {

        MethodInJarResult methodInJarResult = getMethodInJarCache(method, jarName);
        if (methodInJarResult != null) {
//            System.out.println("findCache");
            return methodInJarResult;
        }
        methodInJarResult = searchMethodInJar(method, type, jarDir, jarName);

        if (methodInJarResult.getResult().equals("found")) {
            //已经找到方法，直接返回
            writeMethodInJarResultInFile(methodInJarResult, method, jarName);
            return methodInJarResult;
        }

        if (searchParentClass) {
            *//**
     * 如果搜索结果为 <方法已经找到> 直接返回
     * 如果搜索结果为<类未找到> 说明这个类可能是来自于三方库
     * 如果搜索结果<方法未找到> 说明这个方法来自于父类
     *//*
            String parentMethod = method;
            while (methodInJarResult.getResult().equals(MethodInJarResult.METHOD_NOT_FOUND)) {
                //如果方法来自于父类，首先找到这个类的父类
                String currentClassName = JavaMethodUtil.getMethodClass(parentMethod);
                String parentClassName = JavaMethodUtil.getSuperClassName(currentClassName, jarName, jarDir);

                if (parentClassName == null) {
                    //没有父类
                    break;
                }

                if (isJDKMethod(parentClassName)) {
                    //如果父类追溯到是jdk的方法，跳出
                    methodInJarResult.setResult(MethodInJarResult.METHOD_FROM_JDK);
                    writeMethodInJarResultInFile(methodInJarResult, method, jarName);
                    return methodInJarResult;
                }
                //将 aa.bb.cc.method()转化为其父类的方法aa.bb.dd.method()
                parentMethod = convertChildMethodToParentMethod(currentClassName, parentClassName, parentMethod);
                methodInJarResult = searchMethodInJar(parentMethod, type, jarDir, jarName);
            }
        }

        if (methodInJarResult.getResult().equals("found")) {
            //已经找到方法，直接返回
            writeMethodInJarResultInFile(methodInJarResult, method, jarName);
            return methodInJarResult;
        }

        if (searchThirdPartyJar) {
            //在三方库中搜索
            List<String> jarList = JarUtil.getDependencyJarList(projectId, jarName);

            if (jarList == null || jarList.size() == 0) {
                return methodInJarResult;
            }

            MethodInJarResult methodInThirdPartyJarResult = checkMethodInThirdPartyJar(method, jarList, projectId);

            if (methodInThirdPartyJarResult != null && methodInThirdPartyJarResult.getResult() == MethodInJarResult.FOUND) {
                writeMethodInJarResultInFile(methodInJarResult, method, jarName);
                return methodInThirdPartyJarResult;
            }

        }

        writeMethodInJarResultInFile(methodInJarResult, method, jarName);
        return methodInJarResult;
    }*/

    /*    *//**
     * 将methodInJarResult写入缓存文件
     *
     * @param methodInJarResult
     * @param method
     * @param jarName
     *//*
    private static void writeMethodInJarResultInFile(MethodInJarResult methodInJarResult, String method, String jarName) {

        String cacheFileName = generateCacheFileName(method, jarName);

        methodInJarResult.initBodyString();

        FileUtil.writeFlie(METHOD_IN_JAR_CACHE_DIR + "/" + cacheFileName + ".txt", new Gson().toJson(methodInJarResult));


    }*/

    /*
     */
/**
 * 查找MethodInJar在磁盘上的缓存
 * 文件名为 method__fdse__jarName 的hash值
 *
 * @param method
 * @param jarName
 * @return
 *//*

    private static MethodInJarResult getMethodInJarCache(String method, String jarName) {

        //生成缓存文件的名字
        String cacheFileName = generateCacheFileName(method, jarName);

        File cacheFile = new File(METHOD_IN_JAR_CACHE_DIR + "/" + cacheFileName + ".txt");

        //如果存在，直接读取文件
        if (cacheFile.exists()) {
            MethodInJarResult methodInJarResult = new Gson().fromJson(FileUtil.read(cacheFile.getAbsolutePath()), new TypeToken<MethodInJarResult>() {
            }.getType());
            return methodInJarResult;
        }
        return null;
    }
*/

    /**
     * 生成缓存文件的文件名
     *
     * @param method
     * @param jarName
     * @return
     */
    private static String generateCacheFileName(String method, String jarName) {
        String originalCacheFileName = jarName + "__fdse__" + method;
        return String.valueOf(originalCacheFileName.hashCode());
    }

    /*    *//**
     * 在依赖的三方库中找方法
     * <p>
     * 遍历所有的三方库
     *
     * @param method
     * @param jarList
     * @return
     * @throws Exception
     *//*
    private static MethodInJarResult checkMethodInThirdPartyJar(String method, List<String> jarList, String projectId) throws Exception {
        String javaFile = JavaMethodUtil.getMethodClass(method);
        MethodInJarResult methodInJarResult;
        //遍历所有的三方库
        for (String jarItem : jarList) {

            String[] jarItems = jarItem.split("__fdse__");
            String jar = jarItems[1] + "-" + jarItems[2] + ".jar";

            String decompileOutPath = decompileJar("H:/wangying/lib_all/" + "/" + jar, DECOMPILE_DIR);
            if (decompileOutPath == null) {
                //jar包不存在
                continue;
            }
            String javaPath = decompileOutPath + "/" + javaFile.replaceAll("\\.", "/") + ".java";
            javaPath = checkClassFileExist(javaPath);
            if (javaPath != null) {
                //找到了这个类
                methodInJarResult = getMethodInJar(jar, "H:/wangying/lib_all/", method, projectId, true, false);
                if (methodInJarResult.getResult().equals("found")) {
                    return methodInJarResult;
                }
            }
        }

        return null;
    }


    private static String convertChildMethodToParentMethod(String currentClassName, String parentClassName, String method) {

        return method.replace(currentClassName, parentClassName);
    }

    *//**
     * 获取当前类的父类
     *
     * @param jarName
     * @param currentClassName
     * @return
     *//*
    private static String getSuperClassName(String currentClassName, String jarName, String libDir) throws Exception {

        return InvokeUtils4.getSuperClassName(jarName, libDir, currentClassName);
    }

    *//**
     * 在jar包中搜索方法
     *
     * @param method
     * @param jarDir
     * @param jarName
     *//*
    private static MethodInJarResult searchMethodInJar(String method, int type, String jarDir, String jarName) {
        MethodInJarResult methodInJarResult = new MethodInJarResult();

        //方法名
        String realMethodName = JavaMethodUtil.getMethodName(method);

        if (realMethodName.equals("<init>")) {
            String className = method.split("<init>")[0];
            String[] directClassName = className.split("\\.");
            realMethodName = directClassName[directClassName.length - 1];
        }

        if (realMethodName.contains("$")) {
            if (realMethodName.split("\\$").length >= 2) {
                realMethodName = realMethodName.split("\\$")[1];
            } else {
                methodInJarResult.setResult(MethodInJarResult.METHOD_NOT_FOUND);
                return methodInJarResult;
            }
        }


        //参数列表
        List<String> params = JavaMethodUtil.getParamNames(method);

        String decompileOutPath = JavaMethodUtil.decompileJar(jarDir + "/" + jarName, DECOMPILE_DIR);
        if (decompileOutPath == null) {
            //jar包不存在
            methodInJarResult.setResult(MethodInJarResult.JAR_NOT_FOUND);
            return methodInJarResult;
        }
        *//**
     *  @param method org.apache.log4j.NDC$sdf.clear() or org.apache.log4j.NDC.clear()
     *  @return class org.apache.log4j.NDC$sdf or org.apache.log4j.NDC
     *//*
        //首先根据方法名获取其java文件的路径 javaPath
        String javaFile = JavaMethodUtil.getMethodClass(method);
        String javaPath = decompileOutPath + "/" + javaFile.replaceAll("\\.", "/") + ".java";
        javaPath = checkClassFileExist(javaPath);
        if (javaPath != null) {
            //存在
            CompilationUnit compilationUnit = JavaMethodUtil.getCompilationUnit(javaPath);
            AbstractTypeDeclaration typeDeclaration;
            if (compilationUnit.types().get(0) instanceof AnnotationTypeDeclaration) {
                typeDeclaration = (AnnotationTypeDeclaration) compilationUnit.types().get(0);
            } else if (compilationUnit.types().get(0) instanceof EnumDeclaration) {
                typeDeclaration = (EnumDeclaration) compilationUnit.types().get(0);
            } else {
                typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
            }
            List<BodyDeclaration> bodyDeclarationList = typeDeclaration.bodyDeclarations();

            BodyDeclaration bodyDeclaration = JDTUtil.getMethod(bodyDeclarationList, realMethodName, params, type);

            if (bodyDeclaration == null) {
                //有这个类，但不存在这个方法
                methodInJarResult.setResult(MethodInJarResult.METHOD_NOT_FOUND);
                methodInJarResult.initBodyString();
                return methodInJarResult;
            } else {
                //找到方法
                methodInJarResult.setResult(MethodInJarResult.FOUND);
                methodInJarResult.setBodyDeclaration(bodyDeclaration);
                methodInJarResult.initBodyString();
                return methodInJarResult;
            }
        } else {
            //该方法所在的类在newVersion中不存在
            methodInJarResult.setResult(MethodInJarResult.CLASS_NOT_FOUND);
            methodInJarResult.initBodyString();
            return methodInJarResult;
        }

    }*/

    /**
     * 检查class文件是否存在
     *
     * @param javaPath com/aaa/bbb/ccc.java
     *                 考虑内部类 可能为com/aaa/bbb$ccc.java com/aaa$bbb$ccc.java
     * @return
     */
    private static String checkClassFileExist(String javaPath) {
        if (new File(javaPath).exists()) {
            return javaPath;
        }
        StringBuilder javaPathSb = new StringBuilder(javaPath.replaceAll("//", "/"));
        while (javaPathSb.lastIndexOf("/") != -1) {
            int index = javaPathSb.lastIndexOf("/");
            javaPathSb.setCharAt(index, '$');
            if (new File(javaPathSb.toString()).exists()) {
                return javaPathSb.toString();
            }
        }
        return null;
    }

    /**
     * 检测是方法还是变量，依据是否是括号
     *
     * @param method
     * @return
     */
    public static int checkMethodType(String method) {

        if (method.contains("(")) {
            return METHOD_TYPE;
        } else {
            return VARIABLE_TYPE;
        }
    }

    /**
     * 比较两个方法是否相同
     *
     * @param method
     * @param methodKey
     */
    public static boolean compare2Method(String method, String methodKey) {

        String methodName = JavaMethodUtil.getMethodName(method);
        List<String> params = JavaMethodUtil.getParamNames(method);

        String keyMethodName = JavaMethodUtil.getMethodName(methodKey);
        List<String> keyParams = JavaMethodUtil.getParamNames(methodKey);

        if (!methodName.equals(keyMethodName)) {
            return false;
        }

        if (params.size() != keyParams.size()) {
            return false;
        }

        int size = params.size();

        for (int i = 0; i < size; i++) {
            String param = params.get(i).trim();
            String keyParam = keyParams.get(i).trim();
            if (!param.equals(keyParam) && !param.endsWith(keyParam) && !keyParam.endsWith(param)) {
                return false;
            }
        }

        return true;

    }

    public static String getVariableName(String method) {
        int lastDotIndex = method.lastIndexOf(".");
        return method.substring(lastDotIndex + 1);
    }

    /**
     * 根据JDK文档判断是否是JDK方法
     *
     * @param invokedMethod
     * @return
     */
    public static boolean isJDKMethod2(String invokedMethod) throws FileNotFoundException {

        File jdkDocDir = new File(JDK_DOC_PATH);
        if (!jdkDocDir.exists()) {
            throw new FileNotFoundException();
        }
        File[] jdkDocs = jdkDocDir.listFiles();


        String classPath = getMethodClass(invokedMethod);
        //把 java.lang.StringBuilder 转变为 java/lang/StringBuilder
        String className = classPath.replaceAll("\\.", "/");
        className = className.replaceAll("\\$", ".");


        for (File jdkDoc : jdkDocs) {
            String fileName = jdkDoc.getAbsolutePath() + "/" + "docs/api/" + className + ".html";
            if (new File(fileName).exists()) {
                return true;
            }
        }
        return false;
    }
}