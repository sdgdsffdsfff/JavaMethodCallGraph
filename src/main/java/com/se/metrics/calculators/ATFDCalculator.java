package com.se.metrics.calculators;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.Type;
import com.se.metrics.MetricCalculator;
import com.se.metrics.visitors.ATFDVisitor;

import java.util.*;

public class ATFDCalculator extends MetricCalculator {
    protected ATFDVisitor visitor;

    // 变量的类型是内部类或者当前类
    protected Set<String> classesToRemove;
    // 变量的类型是外部类
    protected Set<String> externalClasses;
    // 变量的类型还无法确定
    protected Set<String> unknownClassesVariables;

    protected Map<String, Type> memberVariables;
    protected Map<String, Type> localVariables;

    public ATFDCalculator() {
        super();
        this.visitor = new ATFDVisitor();
        this.classesToRemove = new HashSet<>();
        this.externalClasses = new HashSet<>();
        this.unknownClassesVariables = new HashSet<>();
        this.memberVariables = new HashMap<>();
        this.localVariables = new HashMap<>();
    }

    @Override
    public void calculate(CompilationUnit cu) {
        this.visitor.visit(cu, this);

        // 对unknownClassesVariables中的变量重新判断类型（主要是针对放在了class末尾声明的成员变量）
        this.checkClassOfUnknownClassVariable();

        // 将内部类和当前类从externalClasses中移除
//        System.out.println("[ATFD] Remove the following classes from External Classes List: " + this.classesToRemove);
        this.externalClasses.removeAll(this.classesToRemove);

        // 计算当前类访问的外部类的个数
        this.metric = (double)this.externalClasses.size();
    }

    @Override
    public void reset() {
        super.reset();

        this.classesToRemove.clear();
        this.externalClasses.clear();
        this.unknownClassesVariables.clear();
        this.memberVariables.clear();
        this.localVariables.clear();
    }

    public Type getTypeOfVariable(String variable) {
        // 先从局部变量Map中找
        for(Map.Entry<String, Type> variableType : this.localVariables.entrySet())
            if (variableType.getKey().equals(variable))
                return variableType.getValue();

        // 再从成员变量Map中找
        for(Map.Entry<String, Type> variableType : this.memberVariables.entrySet())
            if (variableType.getKey().equals(variable))
                return variableType.getValue();

        // 变量未找到
        return null;
    }

    public void addClassToRemove(final String classeName) {
        this.classesToRemove.add(classeName);
    }

    public Set<String> getClassesToRemove()
    {
        return this.classesToRemove;
    }

    public boolean addExternalClass(final String className) {
        return this.externalClasses.add(className);
    }

    public Set<String> getExternalClasses()
    {
        return this.externalClasses;
    }

    public boolean addExternalClassOfVariable(final String variableName) {
        // 获取变量的类型（即Class）
        Type classType = this.getTypeOfVariable(variableName);

        // 如果获取到了变量的类型，将类型其加入到externalClasses
        if (classType != null)
            return this.addExternalClass(classType.toString());

        // 当成员变量的声明放在了class的末尾时，暂时将无法获取到变量的类型，所以先保存到unknownClassesVariables
        return this.addUnknownClassVariable(variableName);
    }

    public boolean addUnknownClassVariable(String variableName) {
        return this.unknownClassesVariables.add(variableName);
    }

    public void addMemberVariable(final String variable, final Type type)
    {
        this.memberVariables.put(variable, type);
    }

    public void addMemberVariables(final List<VariableDeclarator> variables, final Type type) {
        for(VariableDeclarator variable : variables)
            this.addMemberVariable(variable.toString(), type);
    }

    public void addLocalVariable(final String variable, final Type type)
    {
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

    public void clearLocalVariables()
    {
        this.localVariables.clear();
    }

    private void checkClassOfUnknownClassVariable() {
        for (String variable : this.unknownClassesVariables) {
            Type classType = this.getTypeOfVariable(variable);

            if (classType != null)
                this.addExternalClass(classType.toString());
        }
    }


}
