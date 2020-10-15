package com.se.process;
import com.github.javaparser.ParseException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.se.DAO.BuildConnection;
import com.se.DAO.ClassInfoDAO;
import com.se.config.DataConfig;
import com.se.godclass.GodClassCalculator;
import com.se.utils.FileHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class GodClassProcess {

    private static Map<String, List<String>> godClassInProjectMap = new HashMap<>();
    private static String currentProjectName;
    private static int count = 0;

    public static void calculate(String filePath) {

        GodClassCalculator calculator = new GodClassCalculator(
                GodClassCalculator.DEFAULT_WMC_LIMIT,
                GodClassCalculator.DEFAULT_MCC_LIMIT,
                GodClassCalculator.DEFAULT_TCC_LIMIT,
                GodClassCalculator.DEFAULT_ATFD_LIMIT);

//        System.out.println("* MCCCalculatorTest: testCalculate()");

        try {
            File fileMCC = new File(filePath);

            FileInputStream in = new FileInputStream(fileMCC);
            CompilationUnit cu;

            // Parse the file
            cu = StaticJavaParser.parse(in);

            calculator.calculate(cu);


            System.out.println("ATFD:" + calculator.getATFD());
            System.out.println("WMC:"  + calculator.getWMC());
            System.out.println("MCC:" + calculator.getMCC());
            System.out.println("TCC:" + calculator.getTCC());
            System.out.println("is God Class:" + calculator.isGodClass());

            if(calculator.isGodClass()) {
                godClassInProjectMap.computeIfAbsent(currentProjectName, k -> new ArrayList<>()).add(filePath);
                count++;
            }


        } catch (FileNotFoundException ex) {
            System.out.println(filePath);
            System.out.println("MCCCalculatorTest : " + ex.getMessage());
        }
    }

    public static void main(String[] args) throws SQLException {

        File dir = new File(DataConfig.sourceProjectParentPath);
        LinkedList<String> folders = new LinkedList<>();
        FileHelper.getFolders(dir,folders);

        for(String projectRootPath : folders){
            currentProjectName = new File(projectRootPath).getName();
            for (String filePath : FileHelper.getSubFile(projectRootPath, "java")) {
                calculate(filePath);
            }
        }

        FileHelper.writeFile(DataConfig.godClassOutputFilePath, godClassInProjectMap);
        BuildConnection buildConnection = new BuildConnection();
        Connection connection = buildConnection.buildConnect();
        for(List<String> classPathList:godClassInProjectMap.values()){
            ClassInfoDAO.updateGodClass(classPathList,connection);
        }
        System.out.println("god class count:"+count);

    }


}
