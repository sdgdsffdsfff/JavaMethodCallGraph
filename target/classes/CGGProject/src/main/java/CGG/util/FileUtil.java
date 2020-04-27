package CGG.util;

import java.io.*;
/**
 * 文件读写
 */
public class FileUtil {

    /**
     * 使用FileWriter类写文本文件
     */
    public static void writeFile(String fileName, String content) {

        try {
            FileWriter writer = new FileWriter(fileName);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在文件末尾添加一行
     */
    public static void appendLine(String path, String content) {
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件     
            writer = new FileWriter(path, true);
            writer.write(content + "\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读文件
     *
     * @param path
     * @return
     */
    public static String read(String path) {
        File file = new File(path);
        BufferedReader reader = null;
        StringBuilder result = new StringBuilder();
        try {
            // System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));

            String tempString = "";
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                result.append(tempString).append("\r\n");
                line++;
            }
            result = new StringBuilder(result.substring(0, result.length() - 2));
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    return result.toString();
                } catch (IOException e1) {
                }
            }
        }

        return null;
    }

    /**
     * 检测指定路径的文件是否存在
     *
     * @param path
     */
    public static boolean checkFileExists(String path) {
        return new File(path).exists();
    }

    /**
     * 创建路径
     *
     * @param path
     */
    public static void makePath(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
    }

    /**
     * 将InputStream转成String形式返回
     */
    public static String inputStream2String(InputStream is) {
        // 转成string形式返回
        BufferedReader bf = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            while ((line = bf.readLine()) != null) {
                buffer.append(line);
            }
            bf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return buffer.toString();
    }

    /**
     * 将InputStream存到文件中
     */
    public static void inputStream2File(InputStream is, String filePath) {
        File file = new File(filePath);
        OutputStream os;
        try {
            os = new FileOutputStream(file);
            int size = 0;
            int lent = 0;
            byte[] buf = new byte[1024];
            while ((size = is.read(buf)) != -1) {
                lent += size;
                os.write(buf, 0, size);
            }
            is.close();
            os.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * delete Dir
     *
     * @param unZipDir
     */
    public static void removeDir(String unZipDir) {
        System.out.println(unZipDir + "remove start");
        delFolder(unZipDir); //删除完里面所有内容
        System.out.println(unZipDir + "remove end");
    }

    //param folderPath 文件夹完整绝对路径
    private static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //删除指定文件夹下所有文件
//param path 文件夹完整绝对路径
    private static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }
}