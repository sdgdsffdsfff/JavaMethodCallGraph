package com.se.process;

import com.alibaba.fastjson.JSONArray;
import com.se.DAO.ClassInfoDAO;
import com.se.DAO.MethodInfoDAO;
import com.se.DAO.MethodInvocationDAO;
import com.se.DAO.MethodInvocationInViewDAO;
import com.se.entity.ClassInfo;
import com.se.entity.MethodInfo;
import com.se.entity.MethodInvocation;
import com.se.entity.MethodInvocationInView;
import org.apache.commons.lang.time.StopWatch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterMethodInvocation{


    /**
     * 过滤方法调用
     * @param conn
     * @throws SQLException
     * 已完成用多线程的方式加快匹配速度
     */
    public static void filterMethodInvocation(Connection conn, List<String> projectNameList) throws SQLException {
        List<MethodInvocationInView> methodInvocationInViewList = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();
        for(String projectName : projectNameList){
            stopWatch.start();
            //数据库中已有的项目不进行检测
            //if(oldProjectNameList != null && oldProjectNameList.contains(projectName)) continue;
            System.out.println("正在进行方法调用匹配的项目为:" + projectName);
            //根据项目名获取该项目中的所有方法调用
            List<MethodInvocation> methodInvocationList = MethodInvocationDAO.getMethodInvocationByProjectName(projectName,conn);
            for(MethodInvocation methodInvocation:methodInvocationList){
                MethodInfoDAO methodInfoDAO = new MethodInfoDAO();
                MethodInfo callMethodInfo = methodInfoDAO.getMethodInfoByNameClassReturnParameters(projectName,methodInvocation.getCallClassName(),methodInvocation.getCallMethodName(),methodInvocation.getCallMethodReturnType(),methodInvocation.getCallMethodParameters(),conn);
                List<MethodInfo> calledMethodInfoList = methodInfoDAO.getMethodInfoByNameAndClass(projectName,methodInvocation.getCalledClassName(),methodInvocation.getCalledMethodName(),conn);
                if(calledMethodInfoList.size()==0) continue;
                String callClassID = ClassInfoDAO.getClassIDByProjectNameAndClassName(projectName,callMethodInfo.getClassName(),conn);
                String calledClassID = ClassInfoDAO.getClassIDByProjectNameAndClassName(projectName,calledMethodInfoList.get(0).getClassName(),conn);
                if(callClassID != null && calledClassID != null){
                    MethodInfo calledMethodInfo = calledMethodInfoList.get(0);
                    MethodInvocationInView methodInvocationInView = new MethodInvocationInView();
                    methodInvocationInView.setProjectName(projectName);
                    methodInvocationInView.setCallClassName(callMethodInfo.getClassName());
                    methodInvocationInView.setCallMethodName(callMethodInfo.getMethodName());
                    methodInvocationInView.setCallMethodID(callMethodInfo.getID());
                    methodInvocationInView.setCallMethodParameters(callMethodInfo.getMethodParameters());
                    methodInvocationInView.setCallMethodReturnType(callMethodInfo.getReturnType());
                    methodInvocationInView.setCalledClassName(calledMethodInfo.getClassName());
                    methodInvocationInView.setCalledMethodName(calledMethodInfo.getMethodName());
                    methodInvocationInView.setCalledMethodID(calledMethodInfo.getID());
                    methodInvocationInView.setCallClassID(callClassID);
                    methodInvocationInView.setCalledClassID(calledClassID);
                    methodInvocationInViewList.add(methodInvocationInView);
                }
            }
            MethodInvocationInViewDAO.insertMethodInvocationInView(methodInvocationInViewList,conn);
            MethodInvocationInViewDAO.updateIsRecursive(projectName,conn);
            stopWatch.stop();
            System.out.println("处理时间为：" + stopWatch.getTime() + "ms");
            stopWatch.reset();
        }
    }

    //在进行方法调用匹配时将一个项目的信息都读入内存中，然后匹配
    public static void doFilter(Connection conn,List<String> projectNameList) throws SQLException {
        StopWatch stopWatch = new StopWatch();
        for(String projectName : projectNameList){
            List<MethodInvocationInView> methodInvocationInViewList = new ArrayList<>();
            stopWatch.start();
            System.out.println("正在进行方法调用匹配的项目为:" + projectName);
            //根据项目名获取该项目中的所有方法调用
            List<MethodInvocation> methodInvocationList = MethodInvocationDAO.getMethodInvocationByProjectName(projectName,conn);
            MethodInfoDAO methodInfoDAO = new MethodInfoDAO();
            List<MethodInfo> methodInfoList = methodInfoDAO.getMethodInfoListByProjectName(projectName,conn);
            HashMap<String,MethodInfo> methodInfoHashMap = new HashMap<>();
            for(MethodInfo methodInfo:methodInfoList){
                methodInfoHashMap.put(methodInfo.getQualifiedName(),methodInfo);
            }
            Map<String,Integer> classMap = ClassInfoDAO.getClassInfoMapByProjectName(projectName,conn);
            for(MethodInvocation methodInvocation:methodInvocationList){
                MethodInfo callMethodInfo = null,calledMethodInfo = null;
                if(methodInfoHashMap.containsKey(methodInvocation.getQualifiedCallMethodName())){
                    callMethodInfo = methodInfoHashMap.get(methodInvocation.getQualifiedCallMethodName());
                }
                if(methodInfoHashMap.containsKey(methodInvocation.getQualifiedCalledMethodName())){
                    calledMethodInfo = methodInfoHashMap.get(methodInvocation.getQualifiedCalledMethodName());
                }
                if(callMethodInfo == null||calledMethodInfo == null)continue;
                String callClassID = String.valueOf(classMap.get(callMethodInfo.getClassName()));
                String calledClassID = String.valueOf(classMap.get(calledMethodInfo.getClassName()));
                if(callClassID != null && calledClassID != null){
                    MethodInvocationInView methodInvocationInView = new MethodInvocationInView();
                    methodInvocationInView.setProjectName(projectName);
                    methodInvocationInView.setCallClassName(callMethodInfo.getClassName());
                    methodInvocationInView.setCallMethodName(callMethodInfo.getMethodName());
                    methodInvocationInView.setCallMethodID(callMethodInfo.getID());
                    methodInvocationInView.setCallMethodParameters(callMethodInfo.getMethodParameters());
                    methodInvocationInView.setCallMethodReturnType(callMethodInfo.getReturnType());
                    methodInvocationInView.setCalledClassName(calledMethodInfo.getClassName());
                    methodInvocationInView.setCalledMethodName(calledMethodInfo.getMethodName());
                    methodInvocationInView.setCalledMethodID(calledMethodInfo.getID());
                    methodInvocationInView.setCallClassID(callClassID);
                    methodInvocationInView.setCalledClassID(calledClassID);
                    methodInvocationInViewList.add(methodInvocationInView);
                }
            }
            MethodInvocationInViewDAO.insertMethodInvocationInView(methodInvocationInViewList,conn);
            MethodInvocationInViewDAO.updateIsRecursive(projectName,conn);

            FilterMethodInvocation.linkMethodsOfSubclassAndSuperClass(projectName, conn);

            stopWatch.stop();
            System.out.println("处理时间为：" + stopWatch.getTime() + "ms");
            stopWatch.reset();
        }
    }

    /**
     *
     * @param projectName
     * @param methodInvocationList 如果是全量运行，methodInvocationList是MethodInvocationDAO.getMethodInvocationByProjectName(projectName,conn)
     *                             如果是增量更新，则是MethodInvocationDAO.getMethodInvocationByProjectName(projectName,conn)
     * @param conn
     * @throws SQLException
     */
    public static void doFilterPlus(String projectName, List<MethodInvocation> methodInvocationList, Connection conn) throws SQLException {
        StopWatch stopWatch = new StopWatch();
        List<MethodInvocationInView> methodInvocationInViewList = new ArrayList<>();
        stopWatch.start();
//        System.out.println("正在进行方法调用匹配的项目为:" + projectName);

        List<MethodInfo> methodInfoList = MethodInfoDAO.getMethodInfoListByProjectName(projectName,conn);
        HashMap<String,MethodInfo> methodInfoHashMap = new HashMap<>();
        for(MethodInfo methodInfo:methodInfoList){
            methodInfoHashMap.put(methodInfo.getQualifiedName(),methodInfo);
        }
        Map<String,Integer> classMap = ClassInfoDAO.getClassInfoMapByProjectName(projectName,conn);
        for(MethodInvocation methodInvocation:methodInvocationList){
            MethodInfo callMethodInfo = null,calledMethodInfo = null;
            if(methodInfoHashMap.containsKey(methodInvocation.getQualifiedCallMethodName())){
                callMethodInfo = methodInfoHashMap.get(methodInvocation.getQualifiedCallMethodName());
            }
            if(methodInfoHashMap.containsKey(methodInvocation.getQualifiedCalledMethodName())){
                calledMethodInfo = methodInfoHashMap.get(methodInvocation.getQualifiedCalledMethodName());
            }
            if(callMethodInfo == null||calledMethodInfo == null)continue;
            String callClassID = String.valueOf(classMap.get(callMethodInfo.getClassName()));
            String calledClassID = String.valueOf(classMap.get(calledMethodInfo.getClassName()));
            if(callClassID != null && calledClassID != null){
                MethodInvocationInView methodInvocationInView = new MethodInvocationInView();
                methodInvocationInView.setProjectName(projectName);
                methodInvocationInView.setCallClassName(callMethodInfo.getClassName());
                methodInvocationInView.setCallMethodName(callMethodInfo.getMethodName());
                methodInvocationInView.setCallMethodID(callMethodInfo.getID());
                methodInvocationInView.setCallMethodParameters(callMethodInfo.getMethodParameters());
                methodInvocationInView.setCallMethodReturnType(callMethodInfo.getReturnType());
                methodInvocationInView.setCalledClassName(calledMethodInfo.getClassName());
                methodInvocationInView.setCalledMethodName(calledMethodInfo.getMethodName());
                methodInvocationInView.setCalledMethodID(calledMethodInfo.getID());
                methodInvocationInView.setCallClassID(callClassID);
                methodInvocationInView.setCalledClassID(calledClassID);
                methodInvocationInViewList.add(methodInvocationInView);
            }
        }
        MethodInvocationInViewDAO.insertMethodInvocationInView(methodInvocationInViewList,conn);
        MethodInvocationInViewDAO.updateIsRecursive(projectName,conn);

        FilterMethodInvocation.linkMethodsOfSubclassAndSuperClass(projectName, conn);

        stopWatch.stop();
//        System.out.println("处理时间为：" + stopWatch.getTime() + "ms");
        stopWatch.reset();
    }

    /**
     * 将 父类/接口 的方法与 子类/实现类 的方法进行连接
     * @param projectName
     * @param connection
     * @throws SQLException
     */
    public static void linkMethodsOfSubclassAndSuperClass(String projectName, Connection connection) throws SQLException {

        Map<String, List<MethodInfo>> className2MethodMap = new HashMap<>();

        List<List<String>> subSuperTuples = new ArrayList<>();

        List<ClassInfo> subClassInfoList = ClassInfoDAO.getSubClassList(projectName, connection);

        for(ClassInfo subClassInfo : subClassInfoList){
            List<MethodInfo> subMethodInfoList = MethodInfoDAO.getMethodInfoByClassName(projectName, subClassInfo.getClassName(), connection);

            className2MethodMap.put(subClassInfo.getClassName(), subMethodInfoList);

            List<String> superClassList = new ArrayList();
            if(subClassInfo.getSuperClass() != null){
                superClassList.add(subClassInfo.getSuperClass());
            }

            if(subClassInfo.getInterfaces() != null){
                superClassList.addAll(JSONArray.parseArray(subClassInfo.getInterfaces(), String.class));
            }


            for(String superClassName : superClassList){
                List<MethodInfo> superMethodInfoList = MethodInfoDAO.getMethodInfoByClassName(projectName, subClassInfo.getClassName(), connection);
                className2MethodMap.put(superClassName, superMethodInfoList);


                subSuperTuples.add(new ArrayList<String>(){{
                    add(superClassName);
                    add(subClassInfo.getClassName());
                }});
            }

        }

        List<MethodInvocationInView> OverrideMethodList = new ArrayList<>();

        for(List<String> subSuperTuple : subSuperTuples){
            String superClassName = subSuperTuple.get(0);
            List<MethodInfo> superMethodList = className2MethodMap.get(superClassName);

            String subClassName = subSuperTuple.get(1);
            List<MethodInfo> subMethodList = className2MethodMap.get(subClassName);

            for(MethodInfo superMethodInfo : superMethodList){
                for(MethodInfo subMethodInfo : subMethodList){
                    if(superMethodInfo.getMethodName().equals(subMethodInfo.getMethodName()) &&
                            superMethodInfo.getReturnType().equals(subMethodInfo.getReturnType()) &&
                            superMethodInfo.getMethodParameters().equals(subMethodInfo.getMethodParameters())){

                        MethodInvocationInView overrideMethod = new MethodInvocationInView();
                        overrideMethod.setProjectName(projectName);
                        overrideMethod.setCallMethodName(superMethodInfo.getMethodName());
                        overrideMethod.setCalledMethodName(subMethodInfo.getMethodName());
                        overrideMethod.setCallClassName(superClassName);
                        overrideMethod.setCalledClassName(subClassName);
                        overrideMethod.setCallMethodParameters(superMethodInfo.getMethodParameters());
                        overrideMethod.setCallMethodReturnType(subMethodInfo.getMethodParameters());
                        overrideMethod.setCallMethodID(superMethodInfo.getID());
                        overrideMethod.setCalledMethodID(subMethodInfo.getID());
                        overrideMethod.setCallClassID(ClassInfoDAO.getClassIDByProjectNameAndClassName(projectName, superClassName, connection));
                        overrideMethod.setCalledClassID(ClassInfoDAO.getClassIDByProjectNameAndClassName(projectName, subClassName, connection));

                        OverrideMethodList.add(overrideMethod);

                    }
                }
            }
        }

        MethodInvocationInViewDAO.insertMethodInvocationInView(OverrideMethodList, connection);
    }

}
