package com.se.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

    //将一个list中的元素平均分为n组
    public static <T> List<List<T>> divideList(List<T> list,int n){
        if(list == null||list.size() == 0||n<=0)return null;
        int size = list.size();
        List<List<T>> result = new ArrayList<>();
        int remainder = size%n;
        int divide = size/n;
        int offset = 0;
        for(int i =0;i<n;i++){
            List<T> value;
            if(remainder>0){
                value = list.subList(i*divide + offset,(i+1)*divide+ offset +1);
                remainder--;
                offset++;
            }else {
                value = list.subList(i*divide + offset,(i+1)*divide+ offset);
            }
            result.add(value);
        }
        return result;
    }

}
