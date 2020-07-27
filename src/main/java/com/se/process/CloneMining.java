package com.se.process;
import com.se.DAO.BuildConnection;
import com.se.DAO.ClassInfoDAO;
import com.se.DAO.MethodInfoDAO;
import com.se.config.DataConfig;
import com.se.entity.ClassInfo;
import com.se.entity.MeasureIndex;
import com.se.entity.MethodInfo;
import com.se.utils.CalculateUtil;
import com.se.utils.FileHelper;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class CloneMining {

    Set<Integer> measureIndexSet = new HashSet<>();

    private void linkCloneDataAndModel(Connection connection) throws IOException, SQLException {
        Map<String,List<MeasureIndex>> measureMap = FileHelper.readMeasureIndex(DataConfig.measureIndexFilePath);
        Set<String> projectNameSet = measureMap.keySet();
        //对于每个measureIndex中的方法，去匹配抽取的程序数据
        Map<Integer,Integer> cloneIdMap = new HashMap<>();
        System.out.println("正在进行克隆检测结果与数据库数据的匹配");
        MethodInfoDAO methodInfoDAO = new MethodInfoDAO();
        for(String projectName:projectNameSet){
            System.out.println("正在匹配的项目为：" + projectName);
            List<MeasureIndex> measureIndexList = measureMap.get(projectName);
            List<MethodInfo> methodInfoList = methodInfoDAO.getMethodIdListByProjectName(projectName,connection);
            List<ClassInfo> classInfoList = ClassInfoDAO.getClassListByProjectName(projectName,connection);
            Map<String,ClassInfo> classInfoMap = new HashMap<>();
            Map<String,List<MethodInfo>> methodInfoListMap = new HashMap<>();
            for(ClassInfo classInfo:classInfoList){
                classInfoMap.put(classInfo.getClassName(),classInfo);
            }
            for(MethodInfo methodInfo:methodInfoList){
                if(classInfoMap.get(methodInfo.getClassName())!=null){
                    methodInfo.setFilePath(classInfoMap.get(methodInfo.getClassName()).getFilePath());
                    List<MethodInfo> list = methodInfoListMap.getOrDefault(methodInfo.getFilePath(),new ArrayList<>());
                    list.add(methodInfo);
                    methodInfoListMap.put(methodInfo.getFilePath(),list);
                }
            }
            for(MeasureIndex measureIndex:measureIndexList){
                String filePath = measureIndex.getFilePath();
                filePath = filePath.replace("\\","|");
                List<MethodInfo> methodInfoList1 = methodInfoListMap.getOrDefault(filePath,new ArrayList<>());
                for(MethodInfo methodInfo:methodInfoList1){
                    //如果匹配成功，则进行更新
                    if(Math.abs(methodInfo.getBeginLine() - measureIndex.getBeginLine())<=2 && Math.abs(methodInfo.getEndLine()-measureIndex.getEndLine())<=2){
                        cloneIdMap.put(Integer.parseInt(methodInfo.getID()),measureIndex.getId());
                        measureIndexSet.add(measureIndex.getId());
                    }
                }
            }
            methodInfoDAO.updateCloneId(cloneIdMap,connection);
        }
    }


    public static void main(String[] args) throws IOException, SQLException {
        BuildConnection buildConnection = new BuildConnection();
        Connection connection = buildConnection.buildConnect();
        CloneMining cloneMining = new CloneMining();
        cloneMining.linkCloneDataAndModel(connection);
        List<MethodInfo> methodInfoList= new ArrayList<>();
        List<List<Integer>> cloneGroupList =  FileHelper.readCloneGroupToList(DataConfig.cloneGroupFilePath);
        System.out.println("正在从克隆检测信息中挖掘代码资产");
        int count = 1;
        MethodInfoDAO methodInfoDAO = new MethodInfoDAO();
        Map<String,List<Integer>> classInvokeMap = ClassInfoDAO.getAllClassInvokeInfo(connection);
        Map<Integer,MethodInfo> methodInfoMap = methodInfoDAO.getAllMethodInfo(connection);
        for(List<Integer> list:cloneGroupList){
            System.out.println("正在处理的克隆组的编号是：" + count++);
            Set<String> projectNameSet = new HashSet<>();
            List<MethodInfo> GroupMethodList = new ArrayList<>();
            for(Integer id:list){
                if(!cloneMining.measureIndexSet.contains(id))continue;
                MethodInfo methodInfo = methodInfoMap.get(id);
                projectNameSet.add(methodInfo.getProjectName());
                List<Integer> invokeList = classInvokeMap.get(methodInfo.getClassName());
                if(invokeList == null)continue;
                if(CalculateUtil.CalCouplingRate(invokeList.get(0),invokeList.get(1)))
                    methodInfo.setAsset(true);
                else
                    methodInfo.setAsset(false);
                methodInfo.setCloneGroupId(count);
                GroupMethodList.add(methodInfo);
            }
            if(projectNameSet.size()>1){
                for(MethodInfo methodInfo:GroupMethodList){
                    methodInfo.setIsSameProjectClone(0);
                }
            }else {
                for(MethodInfo methodInfo:GroupMethodList){
                    methodInfo.setIsSameProjectClone(1);
                }
            }
            methodInfoList.addAll(GroupMethodList);
            //每挖掘出500个资产进行一次数据库update操作
            if(methodInfoList.size()%500 == 0){
                methodInfoDAO.updateAsset(methodInfoList,connection);
                methodInfoList.clear();
            }
        }
        methodInfoDAO.updateAsset(methodInfoList,connection);
    }

}
