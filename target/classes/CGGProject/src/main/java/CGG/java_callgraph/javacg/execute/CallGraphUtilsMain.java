package CGG.java_callgraph.javacg.execute;


import CGG.util.JavaCallGraphUtils;

import java.util.List;
import java.util.Map;

public class CallGraphUtilsMain {
    public static void main(String[] args) {
        try {
            //Map<String, List<String>> a = JavaCallGraphUtils.generateInvokeMap("myGroupId", "myArtifactId", "1.1.3", "abdera-jcr-1.1.3.jar", "D:/callgraphTest/abdera-jcr-1.1.3.jar");
//            String methodName = "org.apache.abdera.protocol.server.adapters.jcr.JcrCollectionAdapter.dump(javax.jcr.Node)";


//            String methodName = "org.apache.abdera.protocol.server.adapters.jcr.JcrCollectionAdapter.Test.dump(javax.jcr.Node)";
//            Map<String, List<String>> maps = JavaCallGraphUtils.getInvokeMethodByMethodName("myGroupId", "myArtifactId", "1.1.4",
//                    "abdera-jcr-1.1.3.jar", "D:/callgraphTest/abdera-jcr-1.1.3.jar", methodName);

            String methodName = "analyzer.SyntaxAnalyzer.getSyntaxTree(java.util.List)";
            Map<String, List<String>> maps = JavaCallGraphUtils.getInvokeMethodByMethodName("myGroupId", "myArtifactId", "1.0.0",
                    "MyCMMComplier.jar", "/Users/coldilock/Downloads/callgraph/jar/MyCMMComplier.jar", methodName);

            int a = 1;


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
