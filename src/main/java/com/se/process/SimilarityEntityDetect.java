package com.se.process;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.Parameter;
import com.se.config.DataConfig;
import com.se.utils.FileUtils;
import com.se.visitors.ClassVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangyue on 2020/11/13
 */
public class SimilarityEntityDetect {

    public static Map<String,String> javaPathOfDao = new HashMap<>();
    public static Map<String,String> javaPathOfVo = null;

    public static void main(String[] args) throws FileNotFoundException {
        FileUtils.clearInfoForFile(DataConfig.SimilarityEntityDetectResult);

        init();
        List<ClassVisitor> visitorListOfDao = getAllVisitor(javaPathOfDao);
        int lenOfDao = visitorListOfDao.size();
        for (int i=0;i<lenOfDao;i++){
            for (int j=i+1;j<lenOfDao;j++){
                judge(visitorListOfDao.get(i),visitorListOfDao.get(j));
            }
        }

        List<ClassVisitor> visitorListOfVo = getAllVisitor(javaPathOfVo);
        int lenOfVo = visitorListOfDao.size();
        for (int i=0;i<lenOfVo;i++){
            for (int j=i+1;j<lenOfDao;j++){
                judge(visitorListOfVo.get(i),visitorListOfVo.get(j));
            }
        }
    }

    /**
     * 判断两个visitor中的内容是否重复，也就是判断对应的两个类是否重复
     * @param visitor
     * @param visitor1
     */
    public static void judge(ClassVisitor visitor,ClassVisitor visitor1){
        List<Integer> fieldList = findSimilarityOfField(visitor.getFieldMap(), visitor1.getFieldMap());
        double threshold = DataConfig.threshold;
        if (fieldList.get(0)!=0&&fieldList.get(1)!=0){
            if ((double)fieldList.get(2)/fieldList.get(0)>=threshold&&(double)fieldList.get(2)/fieldList.get(1)>=threshold){
                List<Integer> methodList = findSimilarityOfMethod(visitor.getMethodAndType(),visitor.getMethodAndParameter(), visitor1.getMethodAndType(),visitor1.getMethodAndParameter());
                judge1(methodList,fieldList,threshold,visitor,visitor1);
            }else {
                //System.out.println("两个类不重复");
            }
        }else if (fieldList.get(0)==0&&fieldList.get(1)==0){
            List<Integer> methodList = findSimilarityOfMethod(visitor.getMethodAndType(),visitor.getMethodAndParameter(), visitor1.getMethodAndType(),visitor1.getMethodAndParameter());
            judge1(methodList,fieldList,threshold,visitor,visitor1);
        }else {
            //System.out.println("一个类中有字段，一个类中无字段，两个类不重复");
        }
    }

    public static void judge1(List<Integer> list,List<Integer> fieldList,double threshold,ClassVisitor visitor,ClassVisitor visitor1){
        List<String> list1 = new ArrayList<>();
        if (list.get(0)!=0&&list.get(1)!=0){
            if ((double)list.get(2)/list.get(0)>=threshold&&(double)list.get(2)/list.get(1)>=threshold){
                String out1 = "类"+visitor.getClazz()+"和"+visitor1.getClazz()+"两个类可能重复";
                String out2 = visitor.getClazz()+"中有"+fieldList.get(0)+"个字段"+","+list.get(0)+"个方法";
                String out3 = visitor1.getClazz()+"中有"+fieldList.get(1)+"个字段"+","+list.get(1)+"个方法";
                String out4 = "他俩一共有"+fieldList.get(2)+"个类型相同的字段"+"和"+list.get(2)+"个相似方法";
                String out5 = "\n";
                list1.add(out1);
                list1.add(out2);
                list1.add(out3);
                list1.add(out4);
                list1.add(out5);
//                System.out.println("类"+visitor.getClazz()+"和"+visitor1.getClazz()+"两个类可能重复");
//                System.out.println(visitor.getClazz()+"中有"+fieldList.get(0)+"个字段"+","+list.get(0)+"个方法");
//                System.out.println(visitor1.getClazz()+"中有"+fieldList.get(1)+"个字段"+","+list.get(1)+"个方法");
//                System.out.println("他俩一共有"+fieldList.get(2)+"个类型相同的字段"+"和"+list.get(2)+"个相似方法");
//                System.out.println("");
            }
        }else if (list.get(0)==0&&list.get(1)==0){
            String out1 = "类"+visitor.getClazz()+"和"+visitor1.getClazz()+"两个类可能重复";
            String out2 = visitor.getClazz()+"中有"+fieldList.get(0)+"个字段"+","+"没有方法";
            String out3 = visitor1.getClazz()+"中有"+fieldList.get(1)+"个字段"+","+list.get(1)+"个方法";
            String out4 = "他俩一共有"+fieldList.get(2)+"个类型相同的字段";
            String out5 = "\n";
            list1.add(out1);
            list1.add(out2);
            list1.add(out3);
            list1.add(out4);
            list1.add(out5);
//            System.out.println("类"+visitor.getClazz()+"和"+visitor1.getClazz()+"两个类可能重复");
//            System.out.println(visitor.getClazz()+"中有"+fieldList.get(0)+"个字段"+","+"没有方法");
//            System.out.println(visitor1.getClazz()+"中有"+fieldList.get(1)+"个字段"+","+list.get(1)+"个方法");
//            System.out.println("他俩一共有"+fieldList.get(2)+"个类型相同的字段");
//            System.out.println("");
        }else {
            String out1 = "类"+visitor.getClazz()+"和"+visitor1.getClazz()+"中，一个类中无方法，所以两个类不重复";
            String out2 = "\n";
            list1.add(out1);
            list1.add(out2);
//            System.out.println("类"+visitor.getClazz()+"和"+visitor1.getClazz()+"中，一个类中无方法，所以两个类不重复");
//            System.out.println("");
        }
        FileUtils.write(DataConfig.SimilarityEntityDetectResult,list1);
    }


    /**
     * 初始化所有Java文件的访问器
     * @param javaPath
     * @throws FileNotFoundException
     */
    public static List<ClassVisitor> getAllVisitor(Map<String,String> javaPath) throws FileNotFoundException {
        List<ClassVisitor> list = new ArrayList<>();
        if (javaPath!=null){
            ClassVisitor visitor;
            for (Map.Entry entry : javaPath.entrySet()){
                visitor = new ClassVisitor(DataConfig.projectName,entry.getKey().toString());
                File file = new File(entry.getKey().toString());
                CompilationUnit cu = StaticJavaParser.parse(file);
                visitor.visit(cu,null);
                list.add(visitor);
            }
        }
        return list;
    }

    /**
     * 判断两个类中字段类型相同的次数
     * @param map1  类中字段名和类型map
     * @param map2
     * @return
     */
    public static List<Integer> findSimilarityOfField(Map<String,String> map1,Map<String,String> map2){
        List<Integer> list=new ArrayList<>();
        int count = 0;
        int len1 = map1.size();
        list.add(len1);
        int len2 = map2.size();
        list.add(len2);
        if (len1 < len2){
            for (Map.Entry entry1 : map1.entrySet()){
                for (Map.Entry entry2 : map2.entrySet()){
                    if (entry1.getValue().equals(entry2.getValue())){
                        ++count;
                        break;
                    }
                }
            }
        }else {
            for (Map.Entry entry2 : map2.entrySet()){
                for (Map.Entry entry1 : map1.entrySet()){
                    if (entry2.getValue().equals(entry1.getValue())){
                        ++count;
                        break;
                    }
                }
            }
        }
        list.add(count);
        return list;

    }

    public static List<Integer> findSimilarityOfMethod(Map<String,String> methodType1, Map<String,List<Parameter>> methodParam1,
                           Map<String,String> methodType2, Map<String,List<Parameter>> methodParam2)
    {
        List<Integer> list = new ArrayList<>();
        int len1=methodType1.size();
        list.add(len1);
        int len2=methodType2.size();
        list.add(len2);
        int count = 0;
        if (len1>0&&len2>0){
            if (len1<len2){
                for (Map.Entry entry1 : methodType1.entrySet()){
                    for (Map.Entry entry2 : methodType2.entrySet()){
                        if (entry1.getValue().toString().equals(entry2.getValue().toString())){
                            boolean flagOfParam = judgeSameOfList(methodParam1.get(entry1.getKey().toString()),methodParam2.get(entry2.getKey().toString()));
                            if (flagOfParam){
                                methodType2.remove(entry2);
                                ++count;
                                break;
                            }
                        }
                    }
                }
            }else {
                for (Map.Entry entry2 : methodType2.entrySet()){
                    for (Map.Entry entry1 : methodType1.entrySet()){
                        if (entry1.getValue().toString().equals(entry2.getValue().toString())){
                            //参数是否相同
//                            System.out.println("cc  "+methodParam2.get(entry2.getKey().toString())+"   ");
//                            System.out.println("cc  "+methodParam1.get(entry1.getKey().toString())+"   ");
                            boolean flagOfParam = judgeSameOfList(methodParam2.get(entry2.getKey()),methodParam1.get(entry1.getKey()));
                            if (flagOfParam){
                                methodType1.remove(entry1);
                                methodType1.replace(entry1.getKey().toString(),new String());
                                ++count;
                                break;
                            }
                        }
                    }
                }
            }
        }
        list.add(count);
        return list;
    }

    public  static boolean judgeSameOfList(List<Parameter> list1,List<Parameter> list2){
        if (list1==null&&list2==null){
            return true;
        }else if (list1!=null && list2!=null) {
            if (list1.size() != list2.size()) {
                return false;
            }else {
                if (list1.containsAll(list2)){
                    return true;
                }
                else {
                    return false;
                }
            }
        }else {
            return false;
        }
//            List<Parameter> listMax = new ArrayList<>();
//            List<Parameter> listMin = new ArrayList<>();
//            int len1 = list1.size();
//            int len2 = list2.size();
//            if (len1>len2){
//                listMax = list1;
//                listMin = list2;
//            }else {
//                listMax = list2;
//                listMin = list1;
//            }
//            for (int i=0; i < listMin.size(); i++){
//                for (int j=0; j < listMax.size(); j++){
//                    if (listMin.get(i).getNameAsString().equals(listMax.get(j).getNameAsString())){
//                        listMin.remove(i);
//                        listMax.remove(j);
//                        i--;
//                        j--;
//                        break;
//                    }
//                }
//            }
//            if (listMax.size()==0 && listMin.size()==0){
//                return true;
//            }else {
//                return false;
//            }
//        }else {
//            return false;
//        }
    }

    public static void init(){
        //这里项目路径后面加src是因为target等包中也有dao等文件夹，这样先遍历到target就会返回错误结果
        javaPathOfDao= FileUtils.getJavaFilePath(DataConfig.projectPath +"/src",DataConfig.layer_dao);
        javaPathOfVo= FileUtils.getJavaFilePath(DataConfig.projectPath+"/src",DataConfig.layer_vo);
    }
}
