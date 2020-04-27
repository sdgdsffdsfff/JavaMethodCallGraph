package com.se.entity;

public class QName {
    private String pkg;
    private String clazz;
    private String method;
    private String var;

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();

        if(this.pkg != null) {
            buff.append(this.pkg).append(".");
        }
        if(this.clazz != null) {
            buff.append(this.clazz).append(".");
        }
        if(this.method != null) {
            buff.append(this.method).append(".");
        }
        if(this.var != null) {
            buff.append(this.var).append(".");
        }
        //ignore last '.'
        return buff.substring(0, buff.length() - 1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( clazz == null ) ? 0 : clazz.hashCode() );
        result = prime * result + ( ( method == null ) ? 0 : method.hashCode() );
        result = prime * result + ( ( pkg == null ) ? 0 : pkg.hashCode() );
        result = prime * result + ( ( var == null ) ? 0 : var.hashCode() );
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
        QName other = (QName)obj;
        if ( clazz == null ) {
            if ( other.clazz != null )
                return false;
        }
        else if ( !clazz.equals(other.clazz) )
            return false;
        if ( method == null ) {
            if ( other.method != null )
                return false;
        }
        else if ( !method.equals(other.method) )
            return false;
        if ( pkg == null ) {
            if ( other.pkg != null )
                return false;
        }
        else if ( !pkg.equals(other.pkg) )
            return false;
        if ( var == null ) {
            if ( other.var != null )
                return false;
        }
        else if ( !var.equals(other.var) )
            return false;
        return true;
    }
}
