package com.se.process;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.se.DAO.BuildConnection;
import com.se.DAO.MethodInvocationDAO;
import com.se.container.MethodCallContainer;
import com.se.utils.*;
import com.se.visitors.MethodVisitor;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

import static java.lang.System.out;

public class FileProcess {

    private static String sourceProjectPath = "D:\\Java-source";
    private static String projectName;
    public static String getProjectNameFromProjectPath(String projectPath)
    {
        return new File(projectPath).getName();
    }

    public static void main(String[] args) throws IOException, SQLException {
        BuildConnection buildConnection = new BuildConnection();
        Connection conn = buildConnection.buildConnect();
        File dir = new File(sourceProjectPath);
        LinkedList<String> folders = new LinkedList<>();
        FileHandler.getFolders(dir,folders);
        System.out.println("项目数为："+folders.size());
        for(String f:folders)
        {
            System.out.println("正在处理的项目为：" + f);
            projectName = getProjectNameFromProjectPath(f);
            for (String filePath : FileHelper.getSubFile(f, "java"))
            {
                out.println("正在处理的文件为：" + filePath);
                File file = new File(filePath);
                processMethodCallTree(file,conn);
            }
            MethodInvocationDAO methodInvocationDAO = new MethodInvocationDAO();
            methodInvocationDAO.saveMethodInvocation(projectName,MethodCallContainer.getContainer().getMethodCalls(),conn);
        }
        System.out.println("数据处理完成...");
    }

    private static void processMethodCallTree(File file,Connection conn) throws IOException {
        MethodVisitor visitor = new MethodVisitor(projectName,file.getName(),conn);
        try{
            CompilationUnit cu = JavaParser.parse(file);
            visitor.visit(cu, null);
        } catch (Exception ex){
            //ex.printStackTrace();
        }
    }
}
