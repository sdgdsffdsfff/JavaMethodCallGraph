import com.se.DAO.BuildConnection;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;

public class FileProcessTest {

    @Test
    public void testProcessMethodCallTree() throws IOException {
        BuildConnection buildConnection = new BuildConnection();
        Connection conn = buildConnection.buildConnect();
        String filePath = "C:\\Users\\Zero\\Desktop\\Pro\\MethodClone01\\src\\main\\java\\com\\se\\service\\impl\\GraphServiceImpl.java";
        File file = new File(filePath);
        //FileProcess.processMethodCallTree(file,conn);
    }


}
