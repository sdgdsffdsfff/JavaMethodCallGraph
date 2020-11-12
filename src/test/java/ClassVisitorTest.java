import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.se.visitors.ClassVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Created by zhangyue on 2020/11/11 4:04 下午
 */
public class ClassVisitorTest {

    public static void main(String[] args) {
        String path="ssm2";
        String path1="/Users/zhangyue/Downloads/ssm2/src/main/java/com/ncu/dao/CarMapper.java";
        File file=new File(path1);
        ClassVisitor visitor=new ClassVisitor(path,path1);
        CompilationUnit cu;

        {
            try {
                cu = StaticJavaParser.parse(file);
                visitor.visit(cu,null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        for (Map.Entry entry:visitor.getFieldMap().entrySet()){
            System.out.println(entry.getKey()+"   "+entry.getValue());
        }
    }



}
