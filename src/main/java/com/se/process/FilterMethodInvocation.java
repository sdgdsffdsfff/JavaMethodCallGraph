package com.se.process;
import com.se.DAO.MethodInfoDAO;
import com.se.DAO.ClassInfoDAO;
import com.se.DAO.MethodInvocationDAO;
import com.se.DAO.MethodInvocationInViewDAO;
import com.se.entity.MethodInfo;
import com.se.entity.MethodInvocation;
import com.se.entity.MethodInvocationInView;
import org.apache.commons.lang.time.StopWatch;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FilterMethodInvocation implements Runnable {

    private List<String> projectNameList = new ArrayList<>();
    private Connection connection;

    public FilterMethodInvocation(List<String> projectNameList,Connection connection){
        this.projectNameList = projectNameList;
        this.connection = connection;
    }

    /**
     * 过滤方法调用
     * @param conn
     * @throws SQLException
     * 已完成用多线程的方式加快匹配速度
     * 但速度还是不够快，主要原因是数据库的select语句太多
     * todo:在进行方法调用匹配时将一个项目的信息都读入内存中，然后匹配
     */
    private void filterMethodInvocation(Connection conn,List<String> projectNameList) throws SQLException {
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
                MethodInfo callMethodInfo = MethodInfoDAO.getMethodInfoByNameClassReturnParameters(projectName,methodInvocation.getCallClassName(),methodInvocation.getCallMethodName(),methodInvocation.getCallMethodReturnType(),methodInvocation.getCallMethodParameters(),conn);
                List<MethodInfo> calledMethodInfoList = MethodInfoDAO.getMethodInfoByNameAndClass(projectName,methodInvocation.getCalledClassName(),methodInvocation.getCalledMethodName(),conn);
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

    @Override
    public void run() {
        try {
            filterMethodInvocation(connection,projectNameList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
