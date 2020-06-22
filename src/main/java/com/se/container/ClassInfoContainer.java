package com.se.container;

import com.se.entity.ClassInfo;

import java.util.ArrayList;
import java.util.List;

public class ClassInfoContainer {
    private List<ClassInfo> classInfoList;
    private static ClassInfoContainer container;

    private ClassInfoContainer(){
        classInfoList = new ArrayList<>();
    }

    public synchronized static ClassInfoContainer getContainer(){
        if(container == null){
            container = new ClassInfoContainer();
        }
        return container;
    }

    public void addClassInfo(ClassInfo classInfo){
        classInfoList.add(classInfo);
    }

    public List<ClassInfo> getClassInfoList(){
        return classInfoList;
    }

    public void clear(){
        classInfoList.clear();
    }

}
