package CGG;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class Test2 {
    public static void main(String[] args) {
        String code = "package com.coldilock.FirstPKG;\n" +
                "\n" +
                "public class FirstClass {\n" +
                "    private String x = \"hhh\";\n" +
                "    public String getFirstX(String aaa){\n" +
                "        String newX = String.format(\"result%s\",aaa);\n" +
                "        return newX;\n" +
                "    }\n" +
                "\n" +
                "    public String getSecondX(String bbb){\n" +
                "        return bbb.concat(getFirstX(bbb));\n" +
                "    }\n" +
                "}\n";
        JavaParser jp = new JavaParser();

        CompilationUnit compilationUnit = StaticJavaParser.parse(code);

        int a;
    }
}
