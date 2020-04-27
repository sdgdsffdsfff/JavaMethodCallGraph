package com.se.struct;

import java.util.LinkedList;
import java.util.List;

//MethodCallStruct2
public class MethodCallStruct {
    private String calledMethod;

    private List<String> callArgs = new LinkedList<>();

    private String returnType;

    public String getReturnType()
    {
        return returnType;
    }

    public void setReturnType(String returnType)
    {
        this.returnType = returnType;
    }

    public String getCalledMethod()
    {
        return calledMethod;
    }

    public void setCalledMethod(String calledMethod)
    {
        this.calledMethod = calledMethod;
    }

    public List<String> getCallArgs()
    {
        return callArgs;
    }

    public void setCallArgs(List<String> callArgs)
    {
        this.callArgs = callArgs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( callArgs == null ) ? 0 : callArgs.hashCode() );
        result = prime * result + ( ( calledMethod == null ) ? 0 : calledMethod.hashCode() );
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        MethodCallStruct other = (MethodCallStruct)obj;
        if ( callArgs == null ) {
            if ( other.callArgs != null )
                return false;
        }
        else if ( !callArgs.equals(other.callArgs) )
            return false;
        if ( calledMethod == null ) {
            if ( other.calledMethod != null )
                return false;
        }
        else if ( !calledMethod.equals(other.calledMethod) )
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MethodCallStruct [calledMethod=" + calledMethod + ", callArgs=" + callArgs.toString() + "]";
    }


}
