import com.se.DAO.BuildConnection;
import com.se.DAO.MethodInfoDAO;
import com.se.DAO.MethodInvocationDAO;
import com.se.entity.MethodInfo;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MethodInfoDAOTest {

    @Test
    public void updateClassAssetTest() throws SQLException {
        BuildConnection buildConnection = new BuildConnection();
        Connection connection = buildConnection.buildConnect();
        List<MethodInfo> methodInfos = new ArrayList<>();
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setAsset(true);
        methodInfo.setID("1");
        methodInfo.setCloneGroupId(1);
        methodInfos.add(methodInfo);
        MethodInfoDAO methodInfoDAO = new MethodInfoDAO();
//        methodInfoDAO.updateAsset(methodInfos,connection);

//        List<MethodInfo> result = MethodInfoDAO.getMethodIDInClass("ProgramModel", "com.se.Tree", connection);
//        result.forEach(m -> System.out.println(m.getMethodName()));

//        List<MethodInvocation> methodInvocations = MethodInvocationDAO.getMethodInvocationIDsByClassName("ProgramModel", "com.se.util.FileUtil", connection);
//        methodInvocations.forEach(m -> System.out.println(m.getID()));
    }

//    @Test
//    public void getFilePathListTest() throws SQLException{
//        BuildConnection buildConnection = new BuildConnection();
//        Connection connection = buildConnection.buildConnect();
//        String projectName = "ProgramModel";
//        List<String> result = ClassInfoDAO.getFilePathListByProjectName(projectName, connection);
//        result.forEach(System.out::println);
//    }

    @Test
    public void dateTest(){
        Date date = new Date();
//        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//        java.sql.Date sqlDate = new java.sql.Date(myDate.getTime());
//        System.out.println(formatter.format(date));

        java.sql.Date sqld = new java.sql.Date(date.getTime());

        System.out.println(sqld);

//        java.sql.Time sqlt = new java.sql.Time(date.getTime());
//        System.out.println(sqlt);

//        java.sql.Timestamp sqlts = new java.sql.Timestamp(date.getTime());

//        System.out.println(sqlts);



    }

    @Test
    public void test() throws SQLException {
        BuildConnection buildConnection = new BuildConnection();
        Connection connection = buildConnection.buildConnect();

        String projectName = "struts-2.2.1";
        MethodInvocationDAO.updateCalledClassFilePath(projectName, connection);
    }

}
