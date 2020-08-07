package com.se.process;

import com.se.DAO.BuildConnection;
import com.se.DAO.ClassInfoDAO;
import com.se.DAO.MethodInfoDAO;
import com.se.DAO.MethodInvocationInViewDAO;
import com.se.entity.MethodInvocationInView;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class CountInvocationInMethodGra {

    public static void countInvokeCounts(List<String> projectNameList, Connection conn) throws SQLException {
        System.out.println("正在进行调用次数统计");
        for(String projectName:projectNameList){
            MethodInfoDAO methodInfoDAO = new MethodInfoDAO();
            System.out.println("正在进行调用次数统计的项目名称为：" + projectName);
            Map<Integer,String> idMap = methodInfoDAO.getMethodInfoByProjectName(projectName,conn);
            List<List<Integer>> invokeInfoList = new ArrayList<>();
            List<MethodInvocationInView> methodInvocationInViewList = MethodInvocationInViewDAO.getMethodInvocationInViewByProjectName(projectName,conn);
            HashMap<String,Integer> callCountsMap = new HashMap<>();
            HashMap<String,Integer> calledCountsMap = new HashMap<>();
            for(MethodInvocationInView methodInvocationInView:methodInvocationInViewList){
                String callMethodName = methodInvocationInView.getQualifiedCallMethodName();
                String calledMethodName = methodInvocationInView.getQualifiedCalledMethodName();
                if(callCountsMap.containsKey(callMethodName)){
                    int count = callCountsMap.get(callMethodName);
                    callCountsMap.put(callMethodName,count+1);
                }else {
                    callCountsMap.put(callMethodName,1);
                }
                if(calledCountsMap.containsKey(calledMethodName)){
                    int count = calledCountsMap.get(calledMethodName);
                    calledCountsMap.put(calledMethodName,count+1);
                }else {
                    calledCountsMap.put(calledMethodName,1);
                }
            }
            for(Integer Id:idMap.keySet()){
                List<Integer> list = new ArrayList<>();
                String methodName = idMap.get(Id);
                int invokedCount = 0,invokeCount = 0;
                if(calledCountsMap.containsKey(methodName)){
                    invokedCount = calledCountsMap.get(methodName);
                }
                if(callCountsMap.containsKey(methodName)){
                    invokeCount = callCountsMap.get(methodName);
                }
                list.add(invokedCount);
                list.add(invokeCount);
                list.add(Id);
                invokeInfoList.add(list);
            }
            methodInfoDAO.updateInvokeCounts(invokeInfoList,conn);
        }
    }


    public static void countInvocationDept(List<String> projectNameList,Connection conn) throws SQLException {
        System.out.println("正在统计调用深度");
        MethodInfoDAO methodInfoDAO = new MethodInfoDAO();
        for(String projectName:projectNameList){
            System.out.println("正在统计调用深度的项目为：" + projectName);
            List<MethodInvocationInView> methodInvocationInViewList = MethodInvocationInViewDAO.getInvokeInfoByProjectName(projectName,conn);
            Set<String> callMethodSet = new HashSet<>();
            Set<String> calledMethodSet = new HashSet<>();
            Map<String,List<String>> callTree = new HashMap<>();
            for(MethodInvocationInView methodInvocationInView:methodInvocationInViewList){
                String callMethodName = methodInvocationInView.getQualifiedCallMethodName();
                String calledMethodName = methodInvocationInView.getQualifiedCalledMethodName();
//                String callClassName = methodInvocationInView.getCallClassName();
//                String calledClassName = methodInvocationInView.getCalledClassName();
                if(callMethodName.equals(calledMethodName))continue;
                callMethodSet.add(callMethodName);
                calledMethodSet.add(calledMethodName);
                if(callTree.containsKey(callMethodName)){
                    List<String> calledMethodList = callTree.get(callMethodName);
                    calledMethodList.add(calledMethodName);
                    callTree.put(callMethodName,calledMethodList);
                }else {
                    List<String> calledMethodList = new ArrayList<>();
                    calledMethodList.add(calledMethodName);
                    callTree.put(callMethodName,calledMethodList);
                }
            }
            Map<String,Integer> methodNodeMap = new HashMap<>();
            for(String name:callMethodSet){
                methodNodeMap.put(name,0);
            }
            callMethodSet.removeAll(calledMethodSet);
            Queue<String> rootNodeQueue = new LinkedList<>(callMethodSet);
            HashSet<String> nodeSet = new HashSet<>(callMethodSet);
            while(!rootNodeQueue.isEmpty()){
                String rootNodeName = rootNodeQueue.poll();
                int dept = methodNodeMap.getOrDefault(rootNodeName,0);
                List<String> calledClassList = callTree.get(rootNodeName);
                if(calledClassList == null||dept>10)continue;
                for(String name:calledClassList){
                    if(methodNodeMap.getOrDefault(name,0)<dept+1){
                        methodNodeMap.put(name,dept+1);
                    }
                    if(!nodeSet.contains(name)){
                        rootNodeQueue.add(name);
                        nodeSet.add(name);
                    }
                }
            }
            methodInfoDAO.updateInvocationDept(methodNodeMap,conn);
        }
        methodInfoDAO.updateDefaultInvokeDept(conn);
        System.out.println("调用深度统计完成");
    }


    public static void main(String[] args) throws SQLException {
        BuildConnection buildConnection = new BuildConnection();
        Connection connection = buildConnection.buildConnect();
        List<String> projectNameList = ClassInfoDAO.getAllProjectNameFromDB(connection);
        CountInvocationInMethodGra.countInvokeCounts(projectNameList,connection);
        CountInvocationInMethodGra.countInvocationDept(projectNameList,connection);
    }

}
