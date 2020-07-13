package com.se.config;


//所有项目的配置信息
public class DataConfig {

    //连接数据库的url
    public static final String url = "jdbc:mysql://localhost:3306/methodinvocation?serverTimezone=UTC&useSSL=false&rewriteBatchedStatements=true";
    //数据库驱动
    public static final String driver = "com.mysql.cj.jdbc.Driver";
    //连接数据库的用户名
    public static final String user = "root";
    //连接数据库的密码
    public static final String password = "15927029790";
    //所有待分析项目的父目录
    public static final String sourceProjectParentPath = "D:\\java-source";
    //单个待分析项目的目录
    public static final String sourceProjectPath = "D:\\java-source\\yuliskov";
    //API文档的路径
    public static final String API_DOC_PATH = "src/main/resources/docs/api/";
    //java.lang文档的路径
    public static final String JAVA_LANG_DOC_PATH = "src/main/resources/docs/api/java/lang/";
    //是否对单个项目进行分析，为true则分析单个项目，为false则对父目录中所有的项目进行分析
    public static final boolean analyseSingleProject = false;
    //是否需要对于方法调用链的调用深度和每个类的被调用次数进行统计
    public static final boolean analyseInvocationCounts = true;
    //方法粒度的measureIndex文件路径
    public static final String measureIndexFilePath = "C:\\Users\\Zero\\IdeaProjects\\JavaMethodCallGraph\\JavaMethodCallGraph\\src\\main\\resources\\clone_result\\MeasureIndex.csv";
    //克隆检测的克隆组检测结果文件路径
    public static final String cloneGroupFilePath = "C:\\Users\\Zero\\IdeaProjects\\JavaMethodCallGraph\\JavaMethodCallGraph\\src\\main\\resources\\clone_result\\type123_method_group_result.csv";


}
