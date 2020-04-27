package com.se;

import com.se.container.MethodCallContainer;
import com.se.visitors.MethodVisitor;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MethodCallGenerator {
    //private static final String OUTPUT_DIR = "/Users/coldilock/Downloads/output";

    private static final String metodInvocationFile = "C:\\Users\\Zero\\IdeaProjects\\JavaMethodCallGraph\\JavaMethodCallGraph\\src\\main\\resources\\methodInvocation.txt";

    private static final String[] SRC_DIRS = {
        "D:\\java-source7"
    };

//    public static void main(String[] args) throws Exception{
//
//        MethodCallGenerator generator = new MethodCallGenerator();
//        File[] files = new File[SRC_DIRS.length];
//
//        int i = 0;
//
//        for(String srcDir : SRC_DIRS)
//            files[i++] = new File(srcDir);
//
//        generator.processSrcDir(files);
//
//        writeMethodInvocation(MethodCallContainer.getContainer().toString());
//    }

//    public void processSrcDir(File[] files) throws ParseException, IOException{
//        for(File file : files){
//            if(file.isDirectory()){
//                processSrcDir(file.listFiles());
//            } else if(file.getName().endsWith(".java")){
//                processMethodCallTree(file);
//            }
//        }
//    }

//    public void processMethodCallTree(File file) throws ParseException, IOException {
//        //System.out.println("Processing java File: " + file.getName());
//        MethodVisitor visitor = new MethodVisitor();
//        CompilationUnit cu = JavaParser.parse(file);
//        visitor.visit(cu, null);
//    }

    private static final void writeMethodInvocation(String content) throws IOException{
        File file = new File(metodInvocationFile);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(content);
        fileWriter.flush();
        fileWriter.close();
    }
}
