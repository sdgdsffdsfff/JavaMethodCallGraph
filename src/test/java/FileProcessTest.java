import com.se.DAO.BuildConnection;
import org.junit.Test;
import com.se.process.FileProcess;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;

public class FileProcessTest {

    @Test
    public void testProcessMethodCallTree() throws IOException {
        BuildConnection buildConnection = new BuildConnection();
        Connection conn = buildConnection.buildConnect();
        String filePath = "C:\\Users\\Zero\\Desktop\\Pro\\MethodClone01\\src\\main\\java\\com\\se\\util\\StringUtil.java";
        File file = new File(filePath);
        FileProcess.processMethodCallTree(file,conn);
    }
}
