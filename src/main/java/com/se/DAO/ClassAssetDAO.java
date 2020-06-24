package com.se.DAO;

import com.se.entity.ClassAsset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ClassAssetDAO {

    public static void saveClassAsset(List<ClassAsset> classAssets, Connection connection) throws SQLException {
        String sql = "insert into classasset (projectName,className,isInterface,fileName) values (?,?,?,?)";
        if(classAssets != null && !classAssets.isEmpty()){
            PreparedStatement pst = connection.prepareStatement(sql);
            for(ClassAsset classAsset : classAssets){

                pst.addBatch();
            }
            pst.executeBatch();
            pst.clearBatch();
        }
    }

}
