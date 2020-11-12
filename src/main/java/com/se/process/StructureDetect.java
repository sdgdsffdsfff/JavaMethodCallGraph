package com.se.process;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.se.utils.FileUtils;
import com.se.visitors.ClassVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangyue on 2020/11/11 4:47 下午
 */
public class StructureDetect {

    public static String projectName = "ssm2";

    public static String projectPath = "/Users/zhangyue/Downloads/ssm2";

    public static String layer_dao = "dao";
    public static String layer_serviceImpl = "impl";
    public static String layer_controller = "controller";
    public static String layer_service = "service";

    public static Map<String,String> javaPathOfDao = null;
    public static Map<String,String> javaPathOfServiceImpl = null;
    public static Map<String,String> javaPathOfController = null;
    public static Map<String,String> javaPathOfService = null;


    public static Map<String,String> classNameOfDao = null;
    public static Map<String,String> classNameOfServiceImpl = null;
    public static Map<String,String> classNameOfController = null;
    public static Map<String,String> classNameOfService = null;

    public static void main(String[] args) throws FileNotFoundException {
        init();
        int count=0;

        //dao中调用service、controller
        judge(javaPathOfDao,classNameOfService,classNameOfController,count);

        //service中调用controller
        judge(javaPathOfService,classNameOfController,new HashMap<>(),count);

        //serviceImpl中调用controller
        judge(javaPathOfServiceImpl,classNameOfController,new HashMap<>(),count);

        //controller中调用dao
        judge(javaPathOfController,classNameOfDao,new HashMap<>(),count);

        if (count>0){
            System.out.println("该项目不存在违背MVC设计模式的情况......");
        }
    }

    public static void init(){
        //这里项目路径后面加src是因为target等包中也有dao等文件夹，这样先遍历到target就会返回错误结果
        javaPathOfDao= FileUtils.getJavaFilePath(projectPath+"/src",layer_dao);
        javaPathOfServiceImpl= FileUtils.getJavaFilePath(projectPath+"/src",layer_serviceImpl);
        javaPathOfController= FileUtils.getJavaFilePath(projectPath+"/src",layer_controller);
        javaPathOfService= FileUtils.getJavaFilePath(projectPath+"/src",layer_service);

        classNameOfDao = FileUtils.getAllClassName(javaPathOfDao);
        classNameOfServiceImpl = FileUtils.getAllClassName(javaPathOfServiceImpl);
        classNameOfController = FileUtils.getAllClassName(javaPathOfController);
        classNameOfService = FileUtils.getAllClassName(javaPathOfService);
    }


    /**
     * 使用JavaParser获取一个类中的所有字段以及所属类型
     * @param path  Java文件路径
     * @return
     * @throws FileNotFoundException
     */
    public static Map<String,String> getClassNameBypath(String path) throws FileNotFoundException {
        ClassVisitor visitor=new ClassVisitor(projectName,path);
        File file = new File(path);
        CompilationUnit cu = StaticJavaParser.parse(file);
        visitor.visit(cu,null);
        return visitor.getFieldMap();
    }


    /**
     * 判断是否存在违规并输出.
     * eg:(pathMap->dao下的Java文件，classname1->service下的类，classname2->controller下的类)--->dao里调用service、controller的情况
     * @param pathMap   待检测的某一包下的Java文件列表
     * @param classname1    违规调用类1参考
     * @param classname2    违规调用类2参考
     * @param count         计数多少次违反MVC设计模式
     * @throws FileNotFoundException
     */
    public static void judge(Map<String,String> pathMap,Map<String,String> classname1,Map<String,String> classname2,int count) throws FileNotFoundException {
        for (Map.Entry entry : pathMap.entrySet()){
            Map<String,String> map = getClassNameBypath(entry.getKey().toString());
            for (Map.Entry entry1 : map.entrySet()){
                if (classname1.get(entry1.getValue())!=null||classname2.get(entry1.getValue())!=null){
                    ++count;
                    System.out.println("存在类违背MVC设计架构......");
                    if (classname1.get(entry1.getValue())!=null){
                        System.out.println("在" + entry.getValue() + "中调用了" + classname1.get(entry1.getValue()) + "中的" + entry1.getValue());
                    }
                    if (classname2.get(entry1.getValue())!=null){
                        System.out.println("在" + entry.getValue() + "中调用了" + classname2.get(entry1.getValue()) + "中的" + entry1.getValue());
                    }
                    System.out.println("该类的路径为：" + entry.getKey());
                    System.out.print("该语句为：" + entry1.getValue() + " " + entry1.getKey());
                    System.out.println("\n");
                }
            }
        }
    }
}
