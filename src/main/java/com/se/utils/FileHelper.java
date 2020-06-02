package com.se.utils;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class FileHelper
{
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


	public static void createFile(String fileName) {
		File file = new File(fileName);
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//MicroLog.log(Level.INFO, "create file error");
		}
		
	}
	

	public static void deleteFile(String filename)
	{
		File f = new File(filename);
		while(f.exists())
			f.delete();
	}

	
	public static void createFile(String fileName, String content)
	{
		
		PrintWriter pw;
		try
		{
			pw = new PrintWriter(fileName);
			pw.write(content);
			
			pw.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void appendContentToFile(String fileName, String content)
	{
		
		try
		{			
			FileWriter fw = new FileWriter(fileName, true);
			fw.append(content);
			fw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}

	
	public static byte[] transforPNGFiletoByteStream(String pngFileName) 
	{

		File pngfile=new File(pngFileName);
		
		
		int len=(int) pngfile.length();
		byte[] pngfileBytes=new byte[len];
		
		FileInputStream filestream;
		try
		{
			filestream = new FileInputStream(pngfile);
			filestream.read(pngfileBytes);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
        return pngfileBytes;
	
		//TODO  LIHONGWEI
	}
	
	public static void readByteStreamtoPNGFile(String pngFileName, byte[] imageByteStream)
	{
		
		DataOutputStream out;
		try
		{
			out = new DataOutputStream(new FileOutputStream(pngFileName));
			out.write(imageByteStream);
			out.close();
			
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		
		//TODO LIHONGWEI
	}
	
	public static ImageIcon getScaledImage(ImageIcon icon, int w, int h)
	{
		Image image = icon.getImage();                         
		Image smallImage = image.getScaledInstance(w,h,Image.SCALE_FAST);
		ImageIcon result = new ImageIcon(smallImage);
		return result;
	}
	
	public static void scaleImageFile(String imageSrc, int w)
	{
		 try
		{
			BufferedImage bufferedImage = ImageIO.read( new File(imageSrc));
			int h = (int) (w * 1.0 * bufferedImage.getHeight() / bufferedImage.getWidth());
			Image scaledImage = bufferedImage.getScaledInstance(w, h, BufferedImage.TYPE_INT_RGB);
			bufferedImage.getGraphics().drawImage(scaledImage, 0, 0, w,h,null);
			ImageIO.write(bufferedImage, PNG, new File(imageSrc));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void scaleImageFile(String imageSrc, int w, int h)
	{
		 try
		{
			BufferedImage bufferedImage = ImageIO.read( new File(imageSrc));
			Image scaledImage = bufferedImage.getScaledInstance(w, h, BufferedImage.TYPE_INT_RGB);
			bufferedImage.getGraphics().drawImage(scaledImage, 0, 0, w,h,null);
			ImageIO.write(bufferedImage, PNG, new File(imageSrc));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    public static int getSqrt(int value)
    {
    	int result;
    	result = (int) Math.sqrt(value);
    	if(result > 0)
    		result --;
    	return result;
    }
	
	public static Image getScaledImage(Image srcImg, int w, int h)
	{
		
		BufferedImage resizedImg = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();
		
		return resizedImg;

	}



	public static String[] getContentArray(String path)
	{
		List<String> list = new ArrayList<String>();
		File file = new File(path);
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			while (null != (line = in.readLine()))
			{
				list.add(line);
			}
			in.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] result;
		result = list.toArray(new String[0]);
		return result;

	}

	public static String getContent(String path)
	{
		String content = "";
		try
		{
			File file = new File(path);
			BufferedReader in = new BufferedReader(new FileReader(file));
			StringBuilder buffer = new StringBuilder();
			String line = null;

			while (null != (line = in.readLine()))
			{
				buffer.append("\t" + line);
				buffer.append("\n");

			}

			content = buffer.toString();
			in.close();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return content;
	}

	public static List<String> getContentAsList(String instanceFile)
	{
		List<String> result = new ArrayList<String>();
		try
		{
			File file = new File(instanceFile);
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			while(null != (line = in.readLine()))
			{
				result.add(line);
			}
		}
		catch (IOException e)
		{
			// TODO: handle exception
		}
		return result;
	}




	public static void deleteFile(File relationFile)
	{
		relationFile.delete();
		
	}




	public static void vacuumFile(String relationfileName)
	{
		File file = new File(relationfileName);
		if(file.exists())
		{
			FileWriter fw;
			try
			{				
				fw = new FileWriter(relationfileName, false);
				fw.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}









}

class ExtensionFileFilter implements FileFilter
{
	String acceptedExtension;

	public ExtensionFileFilter(String acceptedExtension)
	{
		this.acceptedExtension = acceptedExtension;
	}

	public boolean accept(File pathname)
	{
		return pathname.getName().endsWith(acceptedExtension)
				|| pathname.isDirectory();
	}

}
