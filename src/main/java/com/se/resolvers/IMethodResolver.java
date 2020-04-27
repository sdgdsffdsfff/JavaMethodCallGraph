package com.se.resolvers;

import com.se.struct.MethodStruct;
import com.se.utils.tree.GenericTreeNode;

import java.util.List;

public interface IMethodResolver {
    MethodStruct resolveMethod(String methodQnameWithoutArgs, List<String> methodArgs, GenericTreeNode<String> callTree);
}
