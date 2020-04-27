package com.se.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

public class FileHandler {

    public FileHandler() {
    }

    public static String getSourceCode(String filepath){
        StringBuilder sc = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            Scanner scanner = new Scanner(reader);
            while(scanner.hasNextLine()){
                sc.append(scanner.nextLine());
            }
            scanner.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sc.toString();
    }

    public static LinkedList<String> getFilePaths(String pathsDir) {
        LinkedList<String> paths = new LinkedList<>();
        try {
            BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(pathsDir));

            Scanner scanner = new Scanner(reader);
            while (scanner.hasNextLine()){
                paths.addLast(scanner.nextLine());
            }

            scanner.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return paths;
    }

    public static void getFilePaths(File file,LinkedList<String> files) {
        if(file.isDirectory() && file.listFiles() != null){
            File[] fs = file.listFiles();
            for (File f : fs){
                getFilePaths(f, files);
            }
        }
        else if(file.isFile() && file.getPath().endsWith(".java")){
            files.addLast(file.getPath());
        }
    }

    public static File getFile(String path) {
        return new File(path);
    }

    public static void getFolders(File file,LinkedList<String> files)
    {
        File[] fs = file.listFiles();
        for(File f:fs)
        {
            if(file.isDirectory()&&file.listFiles()!=null)
            {
                files.addLast(f.getPath());
            }
        }
    }
}
