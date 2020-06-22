package com.se.utils;

import java.io.File;
import java.io.FileFilter;

class ExtensionFileFilter implements FileFilter {
    private String acceptedExtension;

    ExtensionFileFilter(String acceptedExtension)
    {
        this.acceptedExtension = acceptedExtension;
    }

    public boolean accept(File pathname)
    {
        return pathname.getName().endsWith(acceptedExtension)
                || pathname.isDirectory();
    }

}
