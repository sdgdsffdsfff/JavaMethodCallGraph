package com.se.utils;

import com.se.config.DataConfig;
import java.io.File;

public class MethodUtils {

    private static final String API_DOC_PATH = DataConfig.API_DOC_PATH;
    private static final String JAVA_LANG_DOC_PATH = DataConfig.JAVA_LANG_DOC_PATH;
    private static String[] basicType = {"byte","short","int","long","float","double","boolean","char"};


    public static boolean isJDKMethod(String className) {
        //? java.lang.StringBuilder ??? java/lang/StringBuilder
        className = className.replaceAll("\\.", "/");
        className = className.replaceAll("\\$", ".");
        String fileName = API_DOC_PATH + className + ".html";
        File file = new File(fileName);
        return file.exists();
    }

    public static boolean isJavaLang(String className) {
        if(className.contains("[]"))className = className.substring(0,className.indexOf('['));
        String newName;
        if(className.contains(".")){
            newName = className.substring(className.lastIndexOf('.')+1);
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
        if(classname.contains("[]"))classname = classname.substring(0,classname.indexOf('['));
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

}
