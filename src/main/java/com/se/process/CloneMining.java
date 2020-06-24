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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloneMining {

    private static void linkCloneDataAndModel(Connection connection) throws IOException, SQLException {
        List<MeasureIndex> measureIndexList;
        measureIndexList = FileHelper.readMeasureIndex(DataConfig.measureIndexFilePath);
        //对于每个measureIndex中的方法，去匹配抽取的程序数据
        int count = 0;
        Map<Integer,Integer> cloneIdMap = new HashMap<>();
        System.out.println("正在进行克隆检测结果与数据库数据的匹配");
        for(MeasureIndex measureIndex:measureIndexList){
            String filePath = measureIndex.getFilePath();
            filePath = filePath.replace("\\","|");
            List<ClassInfo> classInfos = ClassInfoDAO.getClassInfoByFilePath(filePath,connection);
            for(ClassInfo classInfo:classInfos){
                List<MethodInfo> methodInfoList = MethodInfoDAO.getMethodIdListByClassName(classInfo.getClassName(),connection);
                for(MethodInfo methodInfo:methodInfoList){
                    //如果匹配成功，则进行更新
                    if(Math.abs(methodInfo.getBeginLine() - measureIndex.getBeginLine())<=2 && Math.abs(methodInfo.getEndLine()-measureIndex.getEndLine())<=2){
                        cloneIdMap.put(Integer.parseInt(methodInfo.getID()),measureIndex.getId());
                    }
                }
            }
            count++;
            //批次进行update
            if(count%1000 == 0){
                System.out.println("正在进行数据库的更新操作");
                MethodInfoDAO.updateCloneId(cloneIdMap,connection);
                cloneIdMap.clear();
            }
        }
        MethodInfoDAO.updateCloneId(cloneIdMap,connection);
    }


    public static void main(String[] args) throws IOException, SQLException {
        BuildConnection buildConnection = new BuildConnection();
        Connection connection = buildConnection.buildConnect();
        //linkCloneDataAndModel(connection);
        List<ClassInfo> classInfos = new ArrayList<>();
        List<List<Integer>> cloneGroupList =  FileHelper.readCloneGroupToList(DataConfig.cloneGroupFilePath);
        System.out.println("正在从克隆检测信息中挖掘代码资产");
        int count = 1;
        for(List<Integer> list:cloneGroupList){
            System.out.println("正在处理的克隆组是编号是：" + count++);
            for(Integer id:list){
                MethodInfo methodInfo = MethodInfoDAO.getMethodInfoByCloneId(id,connection);
                ClassInfo classInfo = ClassInfoDAO.getClassInfoByClassName(methodInfo.getClassName(),connection);
                if(CalculateUtil.CalCouplingRate(classInfo.getInvokeCounts(),classInfo.getInvokedCounts()))
                    classInfo.setAsset(true);
                else
                    classInfo.setAsset(false);
            }
            //每挖掘出500个资产进行一次数据库update操作
            if(classInfos.size()%500 == 0){
                ClassInfoDAO.updateAsset(classInfos,connection);
                classInfos.clear();
            }
        }
        ClassInfoDAO.updateAsset(classInfos,connection);
    }
}
