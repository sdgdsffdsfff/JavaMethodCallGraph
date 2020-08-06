package com.se.visitors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.se.entity.SetEnum;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author wangjiwen
 * @create 2020/8/4
 */
public class LayerVisitor {
    static boolean isMatch = false;

    static String level = "";

    public static String splitLayer(CompilationUnit cu){
        SetEnum setEnum = new SetEnum();
        Set<String> daoEnum = setEnum.daoEnum;
        /** 类上、方法上的注解 */
        List<MarkerAnnotationExpr> markerAnnotationExprs = cu.findAll(MarkerAnnotationExpr.class);
        List<String> markerAnnotationList = new ArrayList<>(markerAnnotationExprs.size());
        for (int i = 0; i < markerAnnotationExprs.size(); i++) {
            markerAnnotationList.add(markerAnnotationExprs.get(i).getNameAsString());
        }

        /** 方法参数上的注解 */
        List<SingleMemberAnnotationExpr> singleMemberAnnotationExprs = cu.findAll(SingleMemberAnnotationExpr.class);
        List<String> singleAnnotationList = new ArrayList<>();
        for (int i = 0; i < singleMemberAnnotationExprs.size(); i++) {
            singleAnnotationList.add(singleMemberAnnotationExprs.get(i).getNameAsString());
        }
        help(markerAnnotationList, setEnum);
        System.out.println(level);
        return level;
    }

    private static void help(List<String> annotationList, SetEnum setEnum) {
        if (annotationList == null || annotationList.size() == 0) {
            return;
        }
        Set<String> controlEnum = setEnum.controlEnum;
        Set<String> serviceEnum = setEnum.serviceEnum;
        Set<String> daoEnum = setEnum.daoEnum;
        Set<String> otherEnum = setEnum.otherEnum;
        for (String str : annotationList) {
            if (controlEnum.contains(str)) {
                level = "control";
                isMatch = true;
                return;
            }
            if (serviceEnum.contains(str)) {
                level = "service";
                isMatch = true;
                return;
            }
            if (daoEnum.contains(str)) {
                level = "dao";
                isMatch = true;
                return;
            }
            level = "others";
        }
    }

}

