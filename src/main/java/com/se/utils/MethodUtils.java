package com.se.utils;

import com.se.struct.MethodStruct;
import com.se.struct.VariableStruct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MethodUtils {

    private static final String API_DOC_PATH = "/Users/coldilock/Documents/Code/Java_Temp/MethodCallGraph/src/main/resources/jdk_doc/jdk-8u181-docs-all/docs/api";
    private static final String JAVA_LANG_DOC_PATH = "/Users/coldilock/Documents/Code/Java_Temp/MethodCallGraph/src/main/resources/jdk_doc/jdk-8u181-docs-all/docs/api/java/lang";
    private static final String JDK_DOC_PATH = "/Users/coldilock/Documents/Code/Java_Temp/MethodCallGraph/src/main/resources/jdk_doc";
    private static String[] basicType = {"byte","short","int","long","float","double","boolean","char"};

    private static Map<String, MethodStruct> methodMap = new HashMap<>();


    public synchronized static MethodStruct resolveMethodQualifiedName(String methodFullName){
        if(methodMap.containsKey(methodFullName)) {
            return methodMap.get(methodFullName);
        } else {
            MethodStruct m_struct = new MethodStruct();
            String pkg_clazz_Methodname = null;

            if(methodFullName.contains("<")) {
                pkg_clazz_Methodname = methodFullName.substring(0, methodFullName.indexOf("<"));
                String callArgsListStr = methodFullName.substring(methodFullName.indexOf("<") + 1, methodFullName.length() -1);
                String[] callArgsArr = callArgsListStr.split(",");

                for(String var : callArgsArr) {
                    m_struct.getCallArgs().put(var, resolveVariableQualifiedName(var)) ;
                }
            } else {
                pkg_clazz_Methodname = methodFullName;
            }

            String methodName =  pkg_clazz_Methodname.substring(pkg_clazz_Methodname.lastIndexOf(".") + 1, pkg_clazz_Methodname.length()).trim();
            String pkg_clazz = pkg_clazz_Methodname.substring(0, pkg_clazz_Methodname.lastIndexOf("."));
            String clazz = pkg_clazz.substring(pkg_clazz.lastIndexOf(".") + 1, pkg_clazz.length()).trim();
            String pkg = pkg_clazz.substring(0, pkg_clazz.lastIndexOf(".")).trim();

            m_struct.setPkg(pkg);
            m_struct.setClazz(clazz);
            m_struct.setName(methodName);
            methodMap.put(methodFullName, m_struct);
            return m_struct;
        }
    }

    public static VariableStruct resolveVariableQualifiedName(String variableFullName) {
        String varName = variableFullName.substring(variableFullName.lastIndexOf(".") + 1, variableFullName.length()).trim();
        String pkg_clazz = variableFullName.substring(0, variableFullName.lastIndexOf("."));
        String clazz = pkg_clazz.substring(pkg_clazz.lastIndexOf(".") + 1, pkg_clazz.length()).trim();
        String pkg = null;

        //Check if field type is primitive like int,boolaen etc.. if so ignore qualification.
        if(!LangUtils.isPrimitiveType(clazz)) {
            //Check if the class belong to java.lang package
            if(LangUtils.isClassFromLangPackage(clazz)) {
                pkg = "java.lang";
            }  else {
                //Default package.
                //TODO: handle inline package decl
                pkg = pkg_clazz.substring(0, pkg_clazz.lastIndexOf(".")).trim();
            }
        }
        VariableStruct varStruct = new VariableStruct();
        varStruct.setName(varName);
        varStruct.setType(clazz);
        varStruct.setTypePkg(pkg);
        return varStruct;
    }

    public static String qnameCollToCSSepString(Collection<String> qnames) {

        if(qnames != null && !qnames.isEmpty()) {
            StringBuffer buff = new StringBuffer();
            int i = 1;

            for(String qname : qnames) {
                buff.append(qname);

                if(i < qnames.size()) {
                    buff.append(",");
                }
            }
            return buff.toString();
        }
        return null;
    }

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

    public static boolean isJDKMethod2(String invokedMethod) throws FileNotFoundException {
        File jdkDocDir = new File(JDK_DOC_PATH);
        if (!jdkDocDir.exists()) {
            throw new FileNotFoundException();
        }
        File[] jdkDocs = jdkDocDir.listFiles();
        String classPath = getMethodClass(invokedMethod);
        //? java.lang.StringBuilder ??? java/lang/StringBuilder
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

    public static boolean isJDKMethod(String className) {
        //? java.lang.StringBuilder ??? java/lang/StringBuilder
        className = className.replaceAll("\\.", "/");
        className = className.replaceAll("\\$", ".");
        String fileName = API_DOC_PATH + className + ".html";
        File file = new File(fileName);
        return file.exists();
    }

    public static boolean isJavaLang(String className) {
        String newName;
        if(className.contains(".")){
            newName = className.substring(0,className.indexOf('.'));
        }else {
            newName = className;
        }
        //System.out.println(newName);
        String fileName1 = JAVA_LANG_DOC_PATH + newName + ".html";
        File file1 = new File(fileName1);
        if(file1.exists()){
            return true;
        }else {
            className = className.replaceAll("\\.", "/");
            className = className.replaceAll("\\$", ".");
            String fileName2 = JAVA_LANG_DOC_PATH + className + ".html";
            File file2 = new File(fileName2);
            return file2.exists();
        }
    }


    public static boolean isJavaBasicType(String classname){
        for(String type : basicType){
            if(classname.equals(type))return true;
        }
        return false;
    }

    public static String basicToLange(String name){
        switch (name){
            case "int":
                return "Integer";
            case "byte":
                return "Byte";
            case "short":
                return "Short";
            case "long":
                return "Long";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "char":
                return "Character";
            case "boolean"	:
                return "Boolean";
            default:
                return "False";
        }
    }



    public static void main(String[] args) throws IOException
    {
        System.out.println(isJavaLang("System.out.println"));
        //System.out.println(basicToLange("char"));
        //System.out.println(isJavaBasicType("yes"));
        //System.out.println(isJDKMethod("java.util.Map.put"));
//		MethodStruct2 m_struct = MethodUtils.resolveMethodQualifiedName("com.sample.Test.printHello<String.name, int.size>");
//		System.out.println(m_struct);
    }





}
