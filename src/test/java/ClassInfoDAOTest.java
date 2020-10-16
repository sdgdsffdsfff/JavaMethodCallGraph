import com.se.DAO.*;
import com.se.entity.ClassInfo;
import com.se.entity.MethodInfo;
import com.se.entity.MethodInvocation;
import com.se.entity.MethodInvocationInView;
import com.se.utils.FileHelper;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassInfoDAOTest {

    @Test
    public void selectClassInfoByClassPath() throws SQLException, IOException {
        BuildConnection buildConnection = new BuildConnection();
        Connection connection = buildConnection.buildConnect();
//        String projectName = "D:|java-source7|TakahikoKawasaki|nv-websocket-client|src|main|java|com|neovisionaries|ws|client|Address.java";
//        ClassInfo classInfo = ClassInfoDAO.getClassInfoByFilePath("ProgramModel", "/Users/coldilock/Downloads/JavaCodeCorpus/smallsmallproject/ProgramModel/src/test/com/se/Tree.java", connection);
//        if(classInfo == null){
//            System.out.println("没有搜到");
//        }else {
//            System.out.println(classInfo.getID());
//        }
        String projectName = "Tencent1";
        List<ClassInfo> classInfoList = ClassInfoDAO.getClassListByProjectName(projectName, connection);
        List<String> result = classInfoList.stream()
                .map(ClassInfo::getFilePath)
                .collect(Collectors.toList());

        List<String> test = FileHelper.readFile("/Users/coldilock/Downloads/test-tencent.txt");

        System.out.println(test.size());
        System.out.println(classInfoList.size());
        test.removeAll(result);
        System.out.println(test.size());

        test.forEach(System.out::println);

        FileHelper.writeClassPathToFile(test, "/Users/coldilock/Downloads/test-tencent-x.txt");
    }

    @Test
    public void runTimeTest() throws SQLException {



        BuildConnection buildConnection = new BuildConnection();
        Connection conn = buildConnection.buildConnect();

        String projectName = "Tencent1";

        List<String> deleteFilePaths = FileHelper.readFile("/Users/coldilock/Downloads/test-tencent2.txt");


//        List<ClassInfo> classInfoList = ClassInfoDAO.getClassListByProjectName(projectName, conn);
//        List<ClassInfo> deleteClassInfos = new ArrayList<>();
//
//        for(ClassInfo classInfo : classInfoList){
//            if(deleteFilePaths.contains(classInfo.getFilePath()))
//                deleteClassInfos.add(classInfo);
//
//        }

        List<ClassInfo> deleteClassInfos = new ArrayList<>();
        List<String> deleteClassNames = new ArrayList<>();
        List<Integer> delteClassIDs = new ArrayList<>();

        List<ClassInfo> allClassInfoList = ClassInfoDAO.getClassListByProjectName(projectName, conn);

        for(ClassInfo classInfo : allClassInfoList){
            if(deleteFilePaths.contains(classInfo.getFilePath())){
                deleteClassInfos.add(classInfo);
                deleteClassNames.add(classInfo.getClassName());
                delteClassIDs.add(classInfo.getID());
            }

        }


        List<String> deleteMethodIDs = new ArrayList<>();
        List<String> deleteMethodInvocationIDs = new ArrayList<>();
        Set<Integer> deleteMethodInvocationInViewIDs = new HashSet<>();

        List<MethodInfo> methodInfoList = MethodInfoDAO.getMethodInfoListByProjectName(projectName, conn);
        for(MethodInfo methodInfo : methodInfoList){
            if(deleteClassNames.contains(methodInfo.getClassName())){
                deleteMethodIDs.add(methodInfo.getID());
            }
        }

        List<MethodInvocation> methodInvocationList = MethodInvocationDAO.getMethodInvocationByProjectName(projectName,conn);
        for(MethodInvocation methodInvocation : methodInvocationList){
            if(deleteClassNames.contains(methodInvocation.getCallClassName())){
                deleteMethodInvocationIDs.add(methodInvocation.getID());
            }
        }

        long time0 = System.currentTimeMillis();

        List<MethodInvocationInView> methodInvocationInViewList = MethodInvocationInViewDAO.getMethodInvocationInViewByProjectName(projectName,conn);
        for(MethodInvocationInView methodInvocationInView : methodInvocationInViewList){
            if(deleteClassNames.contains(methodInvocationInView.getCallClassName())){
                deleteMethodInvocationInViewIDs.add(methodInvocationInView.getID());
            }
        }

        long time1 = System.currentTimeMillis();
        System.out.println("第一个耗时："+ (time1 - time0));

        List<Integer> deleteMethodInvocationInViewIDs2 = new ArrayList<>();
        for(Integer classID : delteClassIDs){
            List<Integer> methodInvocationInViewIDs = MethodInvocationInViewDAO.getMethodInvocationInViewByCallClassID(projectName, String.valueOf(classID), conn);
            deleteMethodInvocationInViewIDs2.addAll(methodInvocationInViewIDs);
        }

        long time2 = System.currentTimeMillis();
        System.out.println("第二个耗时："+ (time2 - time1));

        if(deleteMethodInvocationInViewIDs.size() == deleteMethodInvocationInViewIDs2.size()){
            System.out.println("两个结果相同");
        } else{

            System.out.println(deleteMethodInvocationInViewIDs.size() + " " + deleteMethodInvocationInViewIDs2.size());

            deleteMethodInvocationInViewIDs.removeAll(deleteMethodInvocationInViewIDs2);
            for(Integer i : deleteMethodInvocationInViewIDs){
                System.out.println(i);
            }
        }

    }


}
