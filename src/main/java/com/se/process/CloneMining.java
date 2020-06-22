package com.se.process;
import com.se.config.DataConfig;
import com.se.utils.FileHelper;

import java.io.IOException;
import java.util.List;

public class CloneMining {

    public static void main(String[] args) throws IOException {
        List<List<Integer>> cloneGroupList =  FileHelper.readCloneGroupToList(DataConfig.cloneGroupFilePath);

    }
}
