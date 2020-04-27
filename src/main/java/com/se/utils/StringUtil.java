package com.se.utils;

import java.util.List;
import java.util.UUID;

public class StringUtil {
    public static boolean isEmpty(String s){
        return s == null || "".equals(s.trim());
    }

    public static String UUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    public static String trimFirstLastChar(String str){
        return str.substring(1,str.length() - 1);
    }

    /**
     *
     * @param name 可以是包含完整名称的类名、方法名、属性名
     * @return
     */
    public static String getNameWithoutPackage(String name){
        return name.substring(name.lastIndexOf(".") + 1);
    }

    /**
     * @param name
     * @return
     */
    public static String getClassNameWithoutMethodName(String name){
        return name.substring(0, name.lastIndexOf("."));
    }


    public static boolean isListEqual(List<String> list1, List<String> list2) {
        // 两个list引用相同（包括两者都为空指针的情况）
        if (list1 == list2) {
            return true;
        }

        // 两个list都为空（包括空指针、元素个数为0）
        if ((list1 == null && list2 != null && list2.size() == 0)
                || (list2 == null && list1 != null && list1.size() == 0)) {
            return true;
        }

        // 两个list元素个数不相同
        if (list1.size() != list2.size()) {
            return false;
        }

        // 两个list元素个数已经相同，再比较两者内容
        // 采用这种可以忽略list中的元素的顺序
        // 涉及到对象的比较是否相同时，确保实现了equals()方法
        if (!list1.containsAll(list2)) {
            return false;
        }

        return true;
    }

}
