import com.se.DAO.BuildConnection;
import com.se.DAO.ClassInfoDAO;
import com.se.entity.ClassInfo;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ClassInfoDAOTest {

    @Test
    public void selectClassInfoByClassPath() throws SQLException {
        BuildConnection buildConnection = new BuildConnection();
        Connection connection = buildConnection.buildConnect();
        String projectName = "D:|java-source7|TakahikoKawasaki|nv-websocket-client|src|main|java|com|neovisionaries|ws|client|Address.java";
        List<ClassInfo> classInfos = ClassInfoDAO.getClassInfoByFilePath(projectName,connection);
        if(classInfos.size() == 0){
            System.out.println("没有搜到");
        }else {
            System.out.println(classInfos.size());
        }
    }
}
