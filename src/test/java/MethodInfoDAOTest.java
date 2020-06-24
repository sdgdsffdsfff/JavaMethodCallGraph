import com.se.DAO.BuildConnection;
import com.se.DAO.MethodInfoDAO;
import com.se.entity.MethodInfo;
import org.junit.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
        MethodInfoDAO.updateAsset(methodInfos,connection);
    }

}
