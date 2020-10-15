package com.se.utils;

import com.se.config.DataConfig;
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

	public static Map<String,List<MeasureIndex>> readMeasureIndex(String measureIndexPath) throws IOException {
		Map<String,List<MeasureIndex>> measureMap = new HashMap<>();
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
			MeasureIndex measureIndex;
			if(DataConfig.analyseSingleProject){
				measureIndex = new MeasureIndex(id,filePath,beginLine,endLine,DataConfig.sourceProjectPath);
			}else {
				measureIndex = new MeasureIndex(id,filePath,beginLine,endLine,DataConfig.sourceProjectParentPath);
			}
			List<MeasureIndex> list = measureMap.getOrDefault(measureIndex.getProjectName(),new ArrayList<>());
			list.add(measureIndex);
			measureMap.put(measureIndex.getProjectName(),list);
		}
		return measureMap;
	}

	public static void writeClassPathToFile(List<String> universalClassList,String path) throws IOException {
		File file = new File(path);
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for(String filePath:universalClassList){
			bufferedWriter.write(filePath);
			bufferedWriter.newLine();
		}
		bufferedWriter.close();
		fileWriter.close();
	}

	public static void writeClassPathToFile(Map<Integer,String> universalClassMap,String path) throws IOException {
		File file = new File(path);
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		for(String filePath:universalClassMap.values()){
			bufferedWriter.write(filePath);
			bufferedWriter.newLine();
		}
		bufferedWriter.close();
		fileWriter.close();
	}

	public static List<String> readFile(String filePath){
		List<String> arrayList = new ArrayList<>();
		try{
			FileReader fr = new FileReader(filePath);
			BufferedReader bf = new BufferedReader(fr);
			String str;
			while((str = bf.readLine()) != null){
				arrayList.add(str);
			}
			bf.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return arrayList;
	}


	public static void getFolders(File file, LinkedList<String> files) {
		File[] fs = file.listFiles();
		for(File f: fs) {
			if(f.isDirectory()&&f.listFiles()!=null)
			{
				files.addLast(f.getPath());
			}
		}
	}


	public static void writeFile(String filePath, Map<String, List<String>> resultMap){

		try{
			File writename = new File(filePath);
			writename.createNewFile(); // 创建新文件

			BufferedWriter bw = new BufferedWriter(new FileWriter(writename));

			for (Map.Entry<String, List<String>> entry : resultMap.entrySet()) {
				//System.out.println(entry.getKey() + ":" + entry.getValue());
				String projectName = entry.getKey();
				List<String> godClassList = entry.getValue();
				bw.write(projectName +  "\n");
				for(String godClassPath : godClassList){
					bw.write(godClassPath + "\n");
				}
				bw.write("\n\n\n\n");
			}

			bw.flush();
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


