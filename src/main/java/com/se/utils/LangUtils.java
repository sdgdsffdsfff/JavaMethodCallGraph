package com.se.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangUtils {
    private static Set<String> LANG_CLASSES = null;

    private static Set<String> prjSrcClasses = new HashSet<String>();

    private static Set<String> libJarClasses = new HashSet<String>();

    private static Set<String> PRIMITIVE_TYPES = new HashSet<String>();

    private static Map<String, Map<String, String>> asterickImportCacheMap = new HashMap<String, Map<String,String>>();

    static {
        PRIMITIVE_TYPES.add("boolean");
        PRIMITIVE_TYPES.add("char");
        PRIMITIVE_TYPES.add("byte");
        PRIMITIVE_TYPES.add("short");
        PRIMITIVE_TYPES.add("int");
        PRIMITIVE_TYPES.add("long");
        PRIMITIVE_TYPES.add("float");
        PRIMITIVE_TYPES.add("double");
    }

    public static void main(String[] args) throws IOException
    {

        // TODO Auto-generated method stub
        //System.out.println(getLangPkgClasses());
        //System.out.println(getClassesOfOptPkgFromJarFile("file:/C:/Program%20Files/Java/jdk1.7.0_72/jre/lib/rt.jar", "java.lang"));
        //System.out.println(getlibJarClasses());

        Set<String> srcsPath = new HashSet<String>();
        srcsPath.add("testinputDir");
        String[] SRC_DIRS = new String[] {
                "testinputdir"
        };
        System.out.println(getClassesFromProjectSrcDirs(SRC_DIRS));
        getLangPkgClasses();
    }

    public static Set<String> getlibJarClasses() {

        if(libJarClasses.isEmpty()) {
            try {
                List<String> libJars = FileUtils.readLines(new File("libjars.txt"));

                if(libJars != null && !libJars.isEmpty()) {
                    for(String jarFile : libJars) {
                        libJarClasses.addAll(getClassesOfOptPkgFromJarFile(jarFile, null));
                    }
                }
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
        return libJarClasses;
    }

    public static Set<String> getLangPkgClasses() {

        if(LANG_CLASSES == null || LANG_CLASSES.isEmpty()) {
            String path = Object.class.getResource("Object.class").getPath().replace("!/java/lang/Object.class", "");
            System.out.println(path);
            //ZipFile z = new ZipFile(new File(new URI(path)));
            LANG_CLASSES = new HashSet<String>();
            LANG_CLASSES.addAll(getClassesOfOptPkgFromJarFile(path, "java.lang"));
        }
        return LANG_CLASSES;
    }

    public static Set<String> getClassesOfOptPkgFromJarFile(String zipPath, String pkg) {
        Set<String> classes = new HashSet<String>();
        JarFile z = null;
        try {
            z = new JarFile(new File(new URI(zipPath)));
            Enumeration<?> e = z.entries();
            String pkgFilterregEx = null;
            if(pkg != null) {
                pkgFilterregEx = pkg +"/\\w+\\.class";
            }
            while(e.hasMoreElements()) {
                JarEntry entry = (JarEntry)e.nextElement();
                String n = entry.getName();

                if(pkgFilterregEx != null) {
                    if(n.matches(pkgFilterregEx)) {
                        classes.add(n);
                        ClassFileReader.parseClassFile(z, entry, false);
                    } else if(n.endsWith(".class")) {
                        //These classes are not part of java.lang package but still must be processed.
                        ClassFileReader.parseClassFile(z, entry, false);
                    }
                }
            }
            z.close();
        }
        catch ( Exception e1 ) {
            e1.printStackTrace();
        }
        return classes;
    }

    public static boolean isClassFromLangPackage(String classSimpleName) {
        String classQName = "java/lang/" + classSimpleName + ".class";
        return getLangPkgClasses().contains(classQName);
    }

    public static Set<String> getClassesFromProjectSrcDirs(String[] srcsPath) {
        if(prjSrcClasses == null || prjSrcClasses.isEmpty()) {
            File[] files = new File[srcsPath.length];
            int i = 0;
            for(String srcDir : srcsPath) {
                files[i++] = new File(srcDir);
            }
            searchForJavaFiles(files, null);
        }
        return prjSrcClasses;
    }

    public static void searchForJavaFiles(File[] files, File rootSrcDir) {
        for (File file : files) {
            if(rootSrcDir == null) {
                rootSrcDir = file;
            }
            if (file.isDirectory()) {
                searchForJavaFiles(file.listFiles(), rootSrcDir);
            } else if(file.getName().endsWith(".java")) {
                String projSrcRelFileName = rootSrcDir.toURI().relativize(file.toURI()).getPath();
                projSrcRelFileName = projSrcRelFileName.replaceAll(" ", "");
                prjSrcClasses.add(projSrcRelFileName.replace(".java", ".class"));
            }
        }
    }

    public static Map<String, String> resolveAsterickImport(String asterickImportLine) {

        Map<String, String> classesMap = asterickImportCacheMap.get(asterickImportLine);

        if(classesMap == null) {
            classesMap = new LinkedHashMap<String, String>();

            String pkgFromImportLine = asterickImportLine;
            pkgFromImportLine = pkgFromImportLine.replaceAll("\\.", "/");
            String pattern = pkgFromImportLine +"/\\w+\\.class";
            System.out.println("pattern  : " + pattern);
            Pattern pattenObj = Pattern.compile(pattern);
            //Search in project dir.
            for(String clazz : prjSrcClasses) {
                Matcher matcher = pattenObj.matcher(clazz);
                if(matcher.matches()) {
                    String className = clazz.substring(pkgFromImportLine.length() + 1, clazz.indexOf(".class"));
                    clazz = clazz.replaceAll("/", "\\.");
                    clazz = clazz.replaceAll(".class", "");
                    classesMap.put(className, clazz);
                }
            }

            //Search in lib dir.
            for(String clazz : libJarClasses) {
                Matcher matcher = pattenObj.matcher(clazz);
                if(matcher.matches()) {
                    String className = clazz.substring(pkgFromImportLine.length() + 1, clazz.indexOf(".class"));
                    clazz = clazz.replaceAll("/", "\\.");
                    clazz = clazz.replaceAll(".class", "");
                    classesMap.put(className, clazz);
                }
            }

            System.out.println(classesMap);
            asterickImportCacheMap.put(asterickImportLine, classesMap);
        }
        return classesMap;
    }

    public static boolean isPrimitiveType(String type) {
        return PRIMITIVE_TYPES.contains(type);
    }

    public static boolean isArray(String type) {
        return type.contains("[]");
    }

}
