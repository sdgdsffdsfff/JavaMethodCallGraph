package com.se.utils;

import com.se.container.ClassStructContainer;
import com.se.struct.ClassOrInterfaceStruct;
import com.se.struct.MethodStruct;
import com.se.struct.VariableStruct;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ClassGen;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassFileReader {

    public static Map<String, ClassGen> classes = new HashMap<String, ClassGen>();

    public static void parseJarClasses()
    {
        try {
            List<String> libJars = FileUtils.readLines(new File("libjars.txt"));
            if ( libJars != null && !libJars.isEmpty() ) {
                for ( String jarFile : libJars ) {
                    parse(jarFile);
                }
            }
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public static void parse(String jarFilePath)
    {
        try {
            System.out.println("Parsing jar file " + jarFilePath);
            JarFile theJar = new JarFile(new File(new URI(jarFilePath)));

            // it's a jar file.
            Enumeration<?> en = theJar.entries();

            // This will enumerate over the files in the jar
            while ( en.hasMoreElements() ) {
                JarEntry entry = (JarEntry)en.nextElement();

                // get next entry
                if ( entry.getName().endsWith(".class") ) {
                    parseClassFile(theJar, entry, true);
                }
            }
            theJar.close();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static void parseClassFile(JarFile theJar, JarEntry entry, boolean storeCompiled) throws IOException {

        System.out.println("***********Parsing start for class " + entry.getName() + "**************");
        // check if entry is a class file and parse it using a class parser.
        ClassParser cp = new ClassParser(
                theJar.getInputStream(entry), entry.getName());
        JavaClass jc = cp.parse();

        // gets the bcel classgen of the class.
        ClassGen cg = new ClassGen(jc);
        parseClassGen(cg);

        // put our classes in a hashmap
        if(storeCompiled) {
            classes.put(cg.getClassName(), cg);
        }
        System.out.println("***********Parsing complete for class " + entry.getName() + "**************");

    }

    public static void parseClassGen(ClassGen cg)
    {
        ClassOrInterfaceStruct clazz = new ClassOrInterfaceStruct();
        String classFullName = cg.getClassName();
        String[] classNameParts = resolveClassFullNameToParts(classFullName);
        clazz.setPkg(classNameParts[0]);
        clazz.setName(classNameParts[1]);

        clazz.setAbstractClazz(cg.isAbstract());
        String superClazzFullName = cg.getSuperclassName();

        // Parse super class.
        if ( superClazzFullName != null ) {
            Map<String, String> superClassMap = new HashMap<String, String>();
            String[] superClassNameParts = resolveClassFullNameToParts(superClazzFullName);
            superClassMap.put(superClassNameParts[1], superClazzFullName);
            clazz.setSuperClasses(superClassMap);
        }

        // parse interfaces.
        String[] interfaceFullNamesArr = cg.getInterfaceNames();

        if ( interfaceFullNamesArr != null && interfaceFullNamesArr.length > 0 ) {
            Map<String, String> interfacesMap = new HashMap<String, String>();

            for ( String interfaceFullName : interfaceFullNamesArr ) {
                String[] interfaceNameParts = resolveClassFullNameToParts(interfaceFullName);
                interfacesMap.put(interfaceNameParts[1], interfaceFullName);
            }
            clazz.setInterfacesImplemented(interfacesMap);
        }

        // parse fields.
        Field[] fields = cg.getFields();
        if ( fields != null && fields.length > 0 ) {

            for ( Field field : fields ) {
                VariableStruct varStruct = new VariableStruct();
                varStruct.setName(field.getName());

                String signature = Utility.signatureToString(field.getSignature());
                String[] fieldNameParts = resolveClassFullNameToParts(signature);
                varStruct.setTypePkg(fieldNameParts[0]);
                varStruct.setType(fieldNameParts[1]);
                varStruct.setArrayVar(Boolean.valueOf(fieldNameParts[2]));
                clazz.getVariables().put(varStruct.getName(), varStruct);
            }
        }

        // Parse methods.
        Method[] methods = cg.getMethods();

        if ( methods != null && methods.length > 0 ) {

            for ( Method method : methods ) {
                MethodStruct m_struct = new MethodStruct();
                m_struct.setName(method.getName());
                m_struct.setPkg(clazz.getPkg());
                m_struct.setClazz(clazz.getName());
                m_struct.setParent(clazz);
                String m_sig = method.getSignature();
                m_struct.setReturnType(Utility.methodSignatureReturnType(m_sig));
                String[] m_args = Utility.methodSignatureArgumentTypes(m_sig);

                if ( m_args != null && m_args.length > 0 ) {

                    for ( String m_arg : m_args ) {
                        VariableStruct m_var_struct = new VariableStruct();
                        String[] mvarParts = resolveClassFullNameToParts(m_arg);
                        m_var_struct.setTypePkg(mvarParts[0]);
                        m_var_struct.setType(mvarParts[1]);
                        m_var_struct.setArrayVar(Boolean.valueOf(mvarParts[2]));
                        String randomVarName = RandomStringUtils.randomAlphanumeric(6);
                        m_var_struct.setName(randomVarName);
                        m_struct.getCallArgs().put(randomVarName, m_var_struct);
                    }
                }
                clazz.getMethods().put(m_struct.getQualifiedNameWithArgs(), m_struct);
            }
        }
        ClassStructContainer.getInstance().getClasses().put(clazz.getQualifiedName(), clazz);
    }

    public static String[] resolveClassFullNameToParts(String classFullName)
    {
        boolean isArray = false;
        System.out.println("Resolve classfull name " + classFullName + " into pkg, class name.");
        String className = "";
        String pkg = "";

        if ( LangUtils.isPrimitiveType(classFullName) ) {
            className = classFullName;
        } else if(LangUtils.isArray(classFullName)) {

            className = classFullName;
            isArray = true;
            //remove array symbol and resolve again
            String arrayElem = classFullName.replaceAll("\\[\\]", "");
            String temp[] = resolveClassFullNameToParts(arrayElem);
            pkg = temp[0];
            className = temp[1];
        } else if ( classFullName.indexOf(".") > 0 ) {
            className = classFullName.substring(classFullName.lastIndexOf(".") + 1, classFullName.length()).trim();
            pkg = classFullName.substring(0, classFullName.lastIndexOf("."));
        } else {
            className = classFullName;
            pkg = "java.lang";
        }
        return new String[] { pkg, className, String.valueOf(isArray) };
    }

    public static void printInfoOnClass(String className)
    {
        ClassGen javaClass = classes.get(className);

        System.out.println("*******Fields*********");
        System.out.println(Arrays.toString(javaClass.getFields()));
        System.out.println();

        System.out.println("*******Methods*********");
        System.out.println(Arrays.toString(javaClass.getMethods()));

        for ( Method method : javaClass.getMethods() ) {
            System.out.println(method);
            System.out.println(method.getCode());
        }

        System.out.println("******* Interfaces *********");
        for ( String interf : javaClass.getInterfaceNames() ) {
            System.out.println(interf);
        }

        System.out.println("******* Superclass *********");
        System.out.println(javaClass.getSuperclassName());

    }

    public static void main(String[] args)
    {

        parseJarClasses();
        //printInfoOnClass("japa.parser.ast.ImportDeclaration");
    }
}
