package com.se.process;

import com.se.DAO.BuildConnection;
import com.se.DAO.ClassInfoDAO;
import com.se.DAO.MethodInvocationInViewDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

//统计每个类的被调用次数
public class CountInvocation {

    public static void main(String[] args) throws SQLException {
        BuildConnection buildConnection = new BuildConnection();
        Connection conn = buildConnection.buildConnect();
        Map<Integer,String> idMap = ClassInfoDAO.getAllClassInfo(conn);
        for(Integer integer:idMap.keySet()){
            String className = idMap.get(integer);
            int count = MethodInvocationInViewDAO.selectCalledCountsByClassName(className,conn);
            ClassInfoDAO.updateInvocationCounts(integer,count,conn);
        }
    }
}
