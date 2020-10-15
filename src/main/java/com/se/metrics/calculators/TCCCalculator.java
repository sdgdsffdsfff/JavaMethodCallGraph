package com.se.metrics.calculators;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.Type;
import com.se.metrics.MetricCalculator;
import com.se.metrics.exceptions.TCCException;
import com.se.metrics.visitors.TCCConnectionsVisitor;
import com.se.metrics.visitors.TCCVariablesMethodsVisitor;
import com.se.utils.Pair;
import com.se.utils.TreeNode;

import java.util.*;

public class TCCCalculator extends MetricCalculator
{
    protected TCCVariablesMethodsVisitor tccVariablesMethodsVisitor;
    protected TCCConnectionsVisitor tccConnectionsVisitor;

    // 当前访问的方法名以及局部变量
    protected String currentMethod;
    protected Map<String, Type> localVariables;

    // 保存类内的所有成员变量以及类型
    protected Map<String, Type> memberVariables;
    // 保存所有的成员方法以及参数 格式： "name type1 type2"
    protected Set<String> visibleMethods;

    protected Map<String, Set<String>> calledMethodsInMethods;
    protected Map<String, Set<String>> connectedMethodsToVariables;

    protected int NDC;
    protected int NP;

    protected Set<String> calledMethodSet;

    public TCCCalculator() {
        super();

        this.tccVariablesMethodsVisitor = new TCCVariablesMethodsVisitor();
        this.tccConnectionsVisitor = new TCCConnectionsVisitor();

        this.currentMethod = "";
        this.localVariables = new HashMap<>();

        this.memberVariables = new HashMap<>();
        this.visibleMethods = new HashSet<>();

        this.calledMethodsInMethods = new HashMap<>();
        this.connectedMethodsToVariables = new HashMap<>();

        this.calledMethodSet = new HashSet<>();

        this.NDC = 0;
        this.NP  = 0;
    }

    private TreeNode<String> buildCallTree(String method) {
        calledMethodSet.add(method);

        // 创建一个节点来表示当前方法，并将该方法调用的方法作为孩子节点
        TreeNode<String> node = new TreeNode<>(method);

        // 如果当前方法没有调用其他方法，那么只返回单独一个节点
        if (!this.calledMethodsInMethods.containsKey(method))
            return node;

        // 获取被当前method调用的方法list
        Set<String> calledMethods = this.calledMethodsInMethods.get(method);

        for(String calledMethod : calledMethods){
            if(!calledMethodSet.contains(calledMethod)){
                node.addChildNode(this.buildCallTree(calledMethod));
            }
        }


        return node;
    }

    /**
     * 计算每个成员方法间接访问的成员变量，然后将它们加入到connectedMethodsToVariables中
     */
    private void addIndirectConnectedMethodsToVariables() {
        // 此map包含了所有间接访问某一成员变量的成员方法
        Map<String, Set<String>> indirectConnectedMethodsToVariables = new HashMap<>();
        for(String variable : this.connectedMethodsToVariables.keySet())
            indirectConnectedMethodsToVariables.put(variable, new HashSet<String>());

        // 获取所有调用了其他方法的成员方法
        for(String method : this.calledMethodsInMethods.keySet()) {
            // 为当前方法构造 call tree
            TreeNode<String> callTree = this.buildCallTree(method);

            // 获取当前方法call tree的叶子结点
            List<TreeNode<String>> leaves = callTree.getLeaves();

            // 对于每一个叶子结点
            for(TreeNode<String> leaf : leaves) {
                // 对每一个成员变量
                for (String variable : this.connectedMethodsToVariables.keySet()) {

                    /* 查看 这个叶子结点所代表的成员方法 是否直接访问了 当前的成员变量
                     * - 是：则此叶子结点的所有祖先节点 间接访问了 这个成员变量， 需要加入到indirectConnectedMethodsToVariables中
                     * - 否：将leafCopy切换为此叶子节点的父亲节点
                     */

                    TreeNode<String> leafCopy = leaf;
                    Set<String> directAccessMethods = this.connectedMethodsToVariables.get(variable);

                    // 从当前叶子结点一直循环到根节点时结束
                    while(leafCopy != null) {
                        // 如果叶子结点直接访问了当前成员变量
                        if (directAccessMethods.contains(leafCopy.getValue())) {
                            Set<String> indirectAccessMethods = indirectConnectedMethodsToVariables.get(variable);

                            // 把当前叶子结点的所有祖先节点（一直到根节点）都加入到indirectConnectedMethodsToVariables中
                            for(leafCopy = leafCopy.getParent();
                                leafCopy != null;
                                leafCopy = leafCopy.getParent())
                                indirectAccessMethods.add(leafCopy.getValue());
                        }
                        // 否则，将leafCopy切换为此叶子节点的父亲节点
                        else
                            leafCopy = leafCopy.getParent();
                    }
                }
            }
        }

        // 将所有非直接访问的成员方法也都加入到connectedMethodsToVariables中
        for(String variable : indirectConnectedMethodsToVariables.keySet())
            this.connectedMethodsToVariables.get(variable).addAll(indirectConnectedMethodsToVariables.get(variable));
    }

    /**
     * @return 一个类中因直接或间接访问了同一个成员变量的成员方法而发生联系的成员方法对数的最大值
     */
    private int calculateNP() {
        int numberOfVisibleMethods = this.visibleMethods.size();
        return (numberOfVisibleMethods * (numberOfVisibleMethods - 1))/2;
    }

    /**
     * 将因 直接或间接访问了同一个成员变量的成员方法 两两组合成一对
     * @return 因直接或间接访问了同一个成员变量的成员方法而发生联系的成员方法对数
     */
    private int calculateNDC() {
        Set<Pair<String, String>> pairMethods = new HashSet<>();

        for(Set<String> methodSet : this.connectedMethodsToVariables.values()) {
            List<String> methodList = new ArrayList<>(methodSet);

            for (int index1 = 0; index1 < methodList.size() - 1; ++index1)
                for(int index2 = index1+1; index2 < methodList.size(); ++index2)
                    pairMethods.add(new Pair<>(methodList.get(index1), methodList.get(index2)));

        }

        return pairMethods.size();
    }

    @Override
    public void calculate(CompilationUnit cu) {
        this.tccVariablesMethodsVisitor.visit(cu, this);

        this.printVisibleMethods();
        this.printMemberVariables();

        this.tccConnectionsVisitor.visit(cu, this);

        this.printConnectedMethodsToVariables();
        this.printCalledMethodsInMethods();

        // 对每一个变量，查找所有间接访问它的方法
        this.addIndirectConnectedMethodsToVariables();

        this.printConnectedMethodsToVariables();

        this.NDC = calculateNDC();
        this.NP  = calculateNP();

        this.metric = (double)NDC/(double)NP;
    }

    @Override
    public void reset() {
        super.reset();

        this.currentMethod = "";
        this.localVariables.clear();

        this.memberVariables.clear();
        this.visibleMethods.clear();

        this.calledMethodsInMethods.clear();
        this.connectedMethodsToVariables.clear();

        this.NDC = 0;
        this.NP  = 0;
    }

    public void printVisibleMethods() {
//        System.out.println("[TCC] Number of visible methods = " + this.visibleMethods.size());
//        for(String method : this.visibleMethods)
//            System.out.println("          method = " + method);
    }

    public void printMemberVariables() {
//        System.out.println("[TCC] Number of member variables = " + this.memberVariables.size());
//        for(Map.Entry<String, Type> variable : this.memberVariables.entrySet())
//            System.out.println("          " + variable.getValue().toString() + " " + variable.getKey());
    }

    public void printConnectedMethodsToVariables() {
//        System.out.println("[TCC] Method that directly access a member variable :");
//        for(Map.Entry<String, Set<String>> entry : this.connectedMethodsToVariables.entrySet())
//            System.out.println("      Variable = " + entry.getKey() + " --> methods = " + entry.getValue());
    }

    public void printCalledMethodsInMethods() {
//        System.out.println("[TCC] List of member methods called by visible member methods :");
//        for(Map.Entry<String, Set<String>> entry : this.calledMethodsInMethods.entrySet())
//            System.out.println("      Calling Method = " + entry.getKey() +  " --> Called Methods  = " + entry.getValue());
    }

    public boolean containsMemberVariable(String variable) {
        return this.memberVariables.containsKey(variable);
    }

    public boolean containsLocalVariable(String variable) {
        return this.localVariables.containsKey(variable);
    }

    // 判断是否是成员变量
    public boolean isMemberVariable(String variable) {
        if (this.containsLocalVariable(variable))
            return false;

        return this.containsMemberVariable(variable);
    }

    public void addMemberVariable(final String variable, final Type type) {
        this.memberVariables.put(variable, type);
    }

    public void addMemberVariables(final List<VariableDeclarator> variables, final Type type) {
        for(VariableDeclarator variable : variables)
            this.addMemberVariable(variable.toString(), type);
    }

    public boolean addVisibleMethod(final String methodDeclaration) {
        return this.visibleMethods.add(methodDeclaration);
    }

    public void setCurrentMethod(final String currentMethodPrototype) throws TCCException {
        if (!this.visibleMethods.contains(currentMethodPrototype))
            throw new TCCException("Current method does not exist : " + currentMethodPrototype);

        this.currentMethod = currentMethodPrototype;
        this.localVariables.clear();
    }

    public String getCurrentMethod() {
        return this.currentMethod;
    }

    public void addLocalVariable(final String variable, final Type type) {
        this.localVariables.put(variable, type);
    }

    public void addLocalVariables(final List<Parameter> variables) {
        for(Parameter variable : variables)
            this.addLocalVariable(variable.getName().toString(), variable.getType());
    }

    public void addLocalVariables(final List<VariableDeclarator> variables, final Type type) {
        for(VariableDeclarator variable : variables)
            this.addLocalVariable(variable.getName().toString(), type);
    }

    public Type getTypeOfVariable(String variable) throws TCCException {
        // 判断是否为局部变量
        for(Map.Entry<String, Type> variableType : this.localVariables.entrySet())
            if (variableType.getKey().equals(variable))
                return variableType.getValue();

        // 判断是否为成员变量
        for(Map.Entry<String, Type> variableType : this.memberVariables.entrySet())
            if (variableType.getKey().equals(variable))
                return variableType.getValue();

        // 错误情况（不应该出现）
        throw new TCCException("Programming error: variable "
            + variable + " does not exist");
    }

    // 当前方法调用了另一个成员方法
    public void addMemberMethodCall(String calledMemberMethodPrototype) throws TCCException {
        if(this.currentMethod == null || this.currentMethod.isEmpty())
            throw new TCCException("Current method is not valid");

        if (!this.visibleMethods.contains(calledMemberMethodPrototype))
            throw new TCCException("Member method \"" + calledMemberMethodPrototype +"\" does not exist");

        // 跳过递归调用
        if (calledMemberMethodPrototype.equals(this.currentMethod))
            return;

        if (!this.calledMethodsInMethods.containsKey(this.currentMethod))
            this.calledMethodsInMethods.put(this.currentMethod, new HashSet<String>());

        // 将被调用方法加入到当前方法对应的list中
        this.calledMethodsInMethods.get(this.currentMethod).add(calledMemberMethodPrototype);
    }

    public void addMemberVariableAccess(String variable) throws TCCException {
        if(this.currentMethod == null || this.currentMethod.isEmpty())
            throw new TCCException("Current method is not valid");

        if (!this.isMemberVariable(variable))
            throw new TCCException("Member variable \"" + variable +"\" does not exist");

        if (!this.connectedMethodsToVariables.containsKey(variable))
            this.connectedMethodsToVariables.put(variable, new HashSet<String>());

        this.connectedMethodsToVariables.get(variable).add(this.currentMethod);
    }

    public int getNP() {
        return this.NP;
    }

    public int getNDC() {
        return this.NDC;
    }

}
