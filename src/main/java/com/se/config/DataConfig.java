package com.se.config;


//所有项目的配置信息
public class DataConfig {

    //连接数据库的url
    public static final String url = "jdbc:mysql://localhost:3306/methodinvocation?serverTimezone=UTC&useSSL=false&rewriteBatchedStatements=true";
    //数据库驱动
    public static final String driver = "com.mysql.jdbc.Driver";
    //连接数据库的用户名
    public static final String user = "root";
    //连接数据库的密码
    public static final String password = "15927029790";
    //所有待分析项目的父目录
    public static final String sourceProjectParentPath = "C:\\Users\\Zero\\IdeaProjects\\MethodCallVisual";
    //单个待分析项目的目录
    public static final String sourceProjectPath = "C:\\Users\\Zero\\IdeaProjects\\MethodCallVisual";
    //API文档的路径
    public static final String API_DOC_PATH = "C:/Users/Zero/IdeaProjects/CGG/src/main/resources/callgraph/jdk_doc/jdk-8u181-docs-all/docs/api/";
    //java.lang文档的路径
    public static final String JAVA_LANG_DOC_PATH = "C:/Users/Zero/IdeaProjects/CGG/src/main/resources/callgraph/jdk_doc/jdk-8u181-docs-all/docs/api/java/lang/";
    //是否对单个项目进行分析，为true则分析单个项目，为false则对父目录中所有的项目进行分析
    public static final boolean analyseSingleProject = true;
    //是否需要对于方法调用链的调用深度和每个类的被调用次数进行统计
    public static final boolean analyseInvocationCounts = true;


}
