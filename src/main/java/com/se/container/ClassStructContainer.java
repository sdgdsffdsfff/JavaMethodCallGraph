package com.se.container;
import com.se.resolvers.IMethodResolver;
import com.se.struct.ClassOrInterfaceStruct;
import com.se.struct.MethodStruct;
import com.se.struct.VariableStruct;
import com.se.utils.MethodUtils;
import com.se.utils.PrettyPrintingMap;
import com.se.utils.tree.GenericTreeNode;
import java.util.*;

//ClassStructContainer2
public class ClassStructContainer {
    private Map<String, ClassOrInterfaceStruct> classes = new HashMap<String, ClassOrInterfaceStruct>();

    private static ClassStructContainer instance;

    private ClassStructContainer() { }

    public Map<String, ClassOrInterfaceStruct> getClasses() {
        return classes;
    }

    public synchronized static ClassStructContainer getInstance() {
        if(instance == null) {
            instance = new ClassStructContainer();
        }
        return instance;
    }

    public void getAnchestors(ClassOrInterfaceStruct child, List<String> anchestors) {

        //Parse all the interfaces implemented by the class.
        if(!child.getInterfacesImplemented().isEmpty()) {
            Collection<String> interfaces = child.getInterfacesImplemented().values();

            //An interface can extend one or more interfaces. so recursively parse interfaces to construct complete tree.
            for(String interfaceStr : interfaces) {

                if(anchestors.contains(interfaceStr)) {
                    continue;
                }
                anchestors.add(interfaceStr);
                ClassOrInterfaceStruct interface_struct = this.getClassOrInterface(interfaceStr);

                if(interface_struct != null) {
                    getAnchestors(interface_struct, anchestors);
                }
            }
        }

        if(!child.getSuperClasses().isEmpty()) {
            Collection<String> superClasses = child.getSuperClasses().values();

            for(String parent : superClasses) {
                if(anchestors.contains(parent)) {
                    continue;
                }
                anchestors.add(parent);
                ClassOrInterfaceStruct parent_struct = this.getClassOrInterface(parent);

                if(parent_struct != null) {
                    //parse the super class to get the interfaces and other super classes.
                    getAnchestors(parent_struct, anchestors);
                }
            }
        }

    }

    public boolean isAnchestor(String childToCheck, String parentToCheckAgainst) {
        ClassOrInterfaceStruct child = this.getClassOrInterface(childToCheck);

        if(child != null) {
            List<String> anchestors = new LinkedList<String>();
            getAnchestors(child, anchestors);
            return anchestors.contains(parentToCheckAgainst);
        }
        return false;
    }

    public ClassOrInterfaceStruct getClassOrInterfaceContainingMethod(String methodName) {
        MethodStruct m_struct = MethodUtils.resolveMethodQualifiedName(methodName);
        return classes.get(m_struct.getPkg() + "." + m_struct.getClazz());
    }

    public ClassOrInterfaceStruct getClassOrInterface(String classQname) {
        for(ClassOrInterfaceStruct clazz : classes.values()) {
            if(clazz.getQualifiedName().equals(classQname)) {
                return clazz;
            }
        }
        return null;
    }

    public MethodStruct getMatchingMethod(String methodQNameWithoutArgs, List<String> args,
                                          GenericTreeNode<String> callTree, IMethodResolver m_resolver) {
        ClassOrInterfaceStruct classOrInterface = getClassOrInterfaceContainingMethod(methodQNameWithoutArgs);
        List<MethodStruct> matchingMethods = new LinkedList<>();

        if(classOrInterface != null && classOrInterface.getMethods() != null && !classOrInterface.getMethods().isEmpty()) {
            List<MethodStruct> m_list = classOrInterface.getMatchingMethods(methodQNameWithoutArgs);

            if(m_list != null && !m_list.isEmpty()) {

                if(m_list.size() == 1) {
                    return m_list.get(0);
                } else {
                    //polymorphic methods are present so callArgs need to be use to isolate the method.

                    for(MethodStruct m_struct : m_list) {

                        if(isPolymorphicMethodAsPerGivenArgs(m_struct, args)) {
                            matchingMethods.add(m_struct);
                        }
                    }
                }
            }
        }

        if(!matchingMethods.isEmpty()) {

            if(matchingMethods.size() == 1) {
                return matchingMethods.get(0);
            } else if(m_resolver != null) {
                //TODO: resolving does not seem to work. Need user strategy.
                //User to provide custom IMethodResolver impl to resolve method. could use spring xml to and get resolver impl class or properties file.
                return m_resolver.resolveMethod(methodQNameWithoutArgs, args, callTree);
            }
        }
        return null;
    }

    public boolean isPolymorphicMethodAsPerGivenArgs(MethodStruct m_struct2, List<String> argsTypes) {

        //if both of them are null or empty then they match.
        if((m_struct2.getCallArgs() == null || m_struct2.getCallArgs().isEmpty()) && (argsTypes == null || argsTypes.isEmpty())) {
            return true;
        }

        if(m_struct2.getCallArgs() != null && !m_struct2.getCallArgs().isEmpty()) {

            if(argsTypes != null && !argsTypes.isEmpty()) {

                //if the args list are of different size.
                if(m_struct2.getCallArgs().size() != argsTypes.size()) {
                    return false;
                }

                Collection<VariableStruct> v_list = m_struct2.getCallArgs().values();
                int i = 0;

                for(VariableStruct var : v_list) {
                    String varType = var.getQualifiedNameWithoutVarName();
                    String argType = argsTypes.get(i);

                    //If argType is null it may be the arg type cannot be determined using method call Expr.
                    if(argType == null) {
                        continue;

                        //If argument passed matches method signature variable class or if argument passed is a child of method sig variable then fine else return false.
                    } else if(! (varType.equals(argType) || this.isAnchestor(argType, varType))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public MethodStruct getMatchingMethod(String methodQNameWithArgs) {
        ClassOrInterfaceStruct classOrInterface = getClassOrInterfaceContainingMethod(methodQNameWithArgs);

        if(classOrInterface != null && classOrInterface.getMethods() != null && !classOrInterface.getMethods().isEmpty()) {
            for(MethodStruct m_struct : classOrInterface.getMethods().values()) {
                if(m_struct.getQualifiedNameWithArgs().equals(methodQNameWithArgs)) {
                    return m_struct;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ClassStructContainer [classes=" + new PrettyPrintingMap<String, ClassOrInterfaceStruct>(classes) + "]";
    }

}
