package com.se.utils;

import com.se.entity.MeasureIndex;

import java.io.*;
import java.util.*;

public class FileHelper {

	public static String NewLine = System.getProperty("line.separator");
	private static String PNG = "png"; 
	private  List<String> getSubFile(String path, FileFilter filter)
	{
		List<String> result = new ArrayList<String>();
		File file = new File(path);
		if( file.isDirectory())
		{			
			for(File subFile :file.listFiles(filter))
			{
				result.addAll(getSubFile(subFile.getAbsolutePath(), filter));
			}
		}
		else 
		{
			result.add(file.getAbsolutePath());
		}
		return result;
	
	}
	/**
	 * 在文件夹中，获取所有指定后缀后的文件
	 * @param path
	 * @param extension
	 * @return
	 */
	public static List<String> getSubFile(String path, String extension)
	{		 
		FileFilter fileFilter = new ExtensionFileFilter(extension);
		FileHelper fileHelper = new FileHelper();
		return fileHelper.getSubFile(path, fileFilter);
	}

	public static Map<String,String> readProjectClone(String filePath){
		Map<String,String> cloneMap = new HashMap<>();
		File csv = new File(filePath);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(csv));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = "";
		try {
			while ((line = br.readLine()) != null) // 读取到的内容给line变量
			{
				String[] content = line.split(",");
				for(int i =0;i<content.length;i++){

				}
			}
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return cloneMap;
	}

	public static List<List<Integer>> readCloneGroupToList(String cloneGroupFilePath) throws IOException {
		List<List<Integer>> cloneGroupList = new ArrayList<>();
		File file = new File(cloneGroupFilePath);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		while((line = bufferedReader.readLine())!=null){
			List<Integer> list = new ArrayList<>();
			String[] cloneGroup = line.split(",");
			for(String str:cloneGroup){
				list.add(Integer.valueOf(str));
			}
			cloneGroupList.add(list);
		}
		return cloneGroupList;
	}

	public static List<MeasureIndex> readMeasureIndex(String measureIndexPath) throws IOException {
		List<MeasureIndex> resultList = new ArrayList<>();
		File file = new File(measureIndexPath);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		while((line = bufferedReader.readLine())!=null){
			String[] strings = line.split(",");
			int id = Integer.parseInt(strings[0]);
			String filePath = strings[1];
			int beginLine = Integer.parseInt(strings[2]);
			int endLine = Integer.parseInt(strings[3]);
			MeasureIndex measureIndex = new MeasureIndex(id,filePath,beginLine,endLine);
			resultList.add(measureIndex);
		}
		return resultList;
	}
}


