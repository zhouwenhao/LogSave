package com.timark.logsave;

import android.text.TextUtils;
import android.util.Log;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipFile;
//import java.util.zip.ZipOutputStream;

public class ZipUtil {
    /**
     * 将指定路径下的文件压缩至指定zip文件，并以指定密码加密
     *
     * @param zipFileName   zip路径+文件名
     * @param compressionPw 加密密码
     * @param files         待压缩文件
     * @throws Exception 文件压缩异常
     */
    public static void zipFilesAndEncrypt(String zipFileName, String compressionPw, File... files) throws Exception {

        try {
            int fileLength = files.length;
            if (fileLength > 0) {
                ZipParameters parameters = new ZipParameters();
                //压缩方式
                parameters.setCompressionMethod(CompressionMethod.DEFLATE);
                //压缩级别
                parameters.setCompressionLevel(CompressionLevel.NORMAL);
                parameters.setEncryptFiles(true);
                //加密方式
                if (!(compressionPw == null || "".equals(compressionPw))) {
                    parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
                    ZipFile zipFile = new ZipFile(zipFileName);
                    zipFile.setPassword(compressionPw.toCharArray());
                    ArrayList<File> filesToAdd = new ArrayList<>(10);
                    filesToAdd.addAll(Arrays.asList(files));

                    zipFile.setCharset(Charset.forName("UTF-8"));
                    zipFile.addFiles(filesToAdd, parameters);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("文件压缩失败");
        }

    }


    /**
     * 根据给定密码压缩文件(s)到指定目录
     *
     * @param destFileName 压缩文件存放绝对路径 e.g.:D:/upload/zip/demo.zip
     * @param password     密码(可为null)
     * @param files        单个文件或文件数组
     * @return 最终的压缩文件存放的绝对路径, 如果为false则说明压缩失败.
     */
    public static boolean compress(String destFileName, String password, File... files) throws ZipException {
        try {
            ZipParameters parameters = new ZipParameters();
            // 压缩方式 8
            parameters.setCompressionMethod(CompressionMethod.DEFLATE);
            // 压缩级别 5
            parameters.setCompressionLevel(CompressionLevel.NORMAL);
            if (!(password == null || "".equals(password))) {
                parameters.setEncryptFiles(true);
                // 加密方式 0
                parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
            }
            ZipFile zipFile = new ZipFile(destFileName);
            if (!(password == null || "".equals(password))) {
                zipFile.setPassword(password.toCharArray());
            }
            for (File file : files) {
                Log.d("zip", "file=" + file.getAbsolutePath());
                zipFile.addFile(file, parameters);
            }
            return true;
        } catch (ZipException e) {
            e.printStackTrace();
            throw new ZipException(e);
        }
    }

    /**
     * 根据给定密码压缩文件(s)到指定位置
     *
     * @param destFileName 压缩文件存放绝对路径 e.g.:D:/upload/zip/demo.zip
     * @param password     密码(可为null)
     * @param filePaths    单个文件路径或文件路径数组
     * @return 最终的压缩文件存放的绝对路径, 如果为false则说明压缩失败.
     */
    public static boolean compress(String destFileName, String password, String... filePaths) throws ZipException {
        int size = filePaths.length;
        File[] files = new File[size];
        for (int i = 0; i < size; i++) {
            files[i] = new File(filePaths[i]);
        }
        return compress(destFileName, password, files);
    }

    /**
     * 根据给定密码压缩文件(s)到指定位置
     *
     * @param destFileName 压缩文件存放绝对路径 e.g.:D:/upload/zip/demo.zip
     * @param password     密码(可为null)
     * @param folder       文件夹路径
     * @return 最终的压缩文件存放的绝对路径, 如果为false则说明压缩失败.
     */
    public static boolean compressFolder(String destFileName, String password, String folder) throws ZipException {
        File folderParam = new File(folder);
        if (folderParam.isDirectory()) {
            try {
                ZipParameters parameters = new ZipParameters();
                // 压缩方式 8
                parameters.setCompressionMethod(CompressionMethod.DEFLATE);
                // 压缩级别 5
                parameters.setCompressionLevel(CompressionLevel.NORMAL);
                if (!TextUtils.isEmpty(password)) {
                    parameters.setEncryptFiles(true);
                    // 加密方式
                    parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
                }
                ZipFile zipFile = new ZipFile(destFileName);
                if (!TextUtils.isEmpty(password)) {
                    zipFile.setPassword(password.toCharArray());
                }
                zipFile.addFolder(folderParam, parameters);
                return true;
            } catch (ZipException e) {
                e.printStackTrace();
                throw new ZipException(e);
            }
        }
        return false;
    }

    /**
     * 根据所给密码解压zip压缩包到指定目录
     * <p>
     * 如果指定目录不存在,可以自动创建,不合法的路径将导致异常被抛出
     *
     * @param zipFile  zip压缩包绝对路径
     * @param dest     指定解压文件夹位置
     * @param password 密码(可为null)
     * @return 解压后的文件数组
     * @throws ZipException 异常
     */
    public static File[] deCompress(File zipFile, String dest, String password) throws ZipException {
        //1.判断指定目录是否存在
        File destDir = new File(dest);
        if (destDir.isDirectory() && !destDir.exists()) {
            boolean mkdir = destDir.mkdir();
            System.out.println(mkdir);
        }
        //2.初始化zip工具
        ZipFile zFile = new ZipFile(zipFile);
        zFile.setCharset(Charset.forName("UTF-8"));
        if (!zFile.isValidZipFile()) {
            throw new ZipException("压缩文件不合法,可能被损坏.");
        }
        //3.判断是否已加密
        if (zFile.isEncrypted()) {
            zFile.setPassword(password.toCharArray());
        }
        //4.解压所有文件
        zFile.extractAll(dest);
        List headerList = zFile.getFileHeaders();
        List<File> extractedFileList = new ArrayList<>();
        for (Object object : headerList) {
            FileHeader fileHeader = (FileHeader) object;
            if (!fileHeader.isDirectory()) {
                extractedFileList.add(new File(destDir, fileHeader.getFileName()));
            }
        }
        File[] extractedFiles = new File[extractedFileList.size()];
        extractedFileList.toArray(extractedFiles);
        return extractedFiles;
    }

    /**
     * 解压无密码的zip压缩包到指定目录
     *
     * @param zipFile zip压缩包
     * @param dest    指定解压文件夹位置
     * @return 解压后的文件数组
     */
    public static File[] deCompress(File zipFile, String dest) throws ZipException {
        try {
            return deCompress(zipFile, dest, null);
        } catch (ZipException e) {
            e.printStackTrace();
            throw new ZipException(e);
        }
    }

    /**
     * 根据所给密码解压zip压缩包到指定目录
     *
     * @param zipFilePath zip压缩包绝对路径
     * @param dest        指定解压文件夹位置
     * @param password    压缩包密码
     * @return 解压后的所有文件数组
     */
    public static File[] deCompress(String zipFilePath, String dest, String password) throws ZipException {
        try {
            return deCompress(new File(zipFilePath), dest, password);
        } catch (ZipException e) {
            e.printStackTrace();
            throw new ZipException(e);
        }
    }

    /**
     * 无密码解压压缩包到指定目录
     *
     * @param zipFilePath zip压缩包绝对路径
     * @param dest        指定解压文件夹位置
     * @return 解压后的所有文件数组
     */
    public static File[] deCompress(String zipFilePath, String dest) throws ZipException {
        try {
            return deCompress(new File(zipFilePath), dest, null);
        } catch (ZipException e) {
            e.printStackTrace();
            throw new ZipException(e);
        }
    }




//===========================================================================================
//    public static void zip(String src, String dest) throws IOException {
//        //提供了一个数据项压缩成一个ZIP归档输出流
//        ZipOutputStream out = null;
//        try {
//
//            File outFile = new File(dest);//源文件或者目录
//            File fileOrDirectory = new File(src);//压缩文件路径
//            out = new ZipOutputStream(new FileOutputStream(outFile));
//            //如果此文件是一个文件，否则为false。
//            if (fileOrDirectory.isFile()) {
//                zipFileOrDirectory(out, fileOrDirectory, "");
//            } else {
//                //返回一个文件或空阵列。
//                File[] entries = fileOrDirectory.listFiles();
//                for (int i = 0; i < entries.length; i++) {
//                    // 递归压缩，更新curPaths
//                    zipFileOrDirectory(out, entries[i], "");
//                }
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        } finally {
//            //关闭输出流
//            if (out != null) {
//                try {
//                    out.close();
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private static void zipFileOrDirectory(ZipOutputStream out,
//                                           File fileOrDirectory, String curPath) throws IOException {
//        //从文件中读取字节的输入流
//        FileInputStream in = null;
//        try {
//            //如果此文件是一个目录，否则返回false。
//            if (!fileOrDirectory.isDirectory()) {
//                // 压缩文件
//                byte[] buffer = new byte[4096];
//                int bytes_read;
//                in = new FileInputStream(fileOrDirectory);
//                //实例代表一个条目内的ZIP归档
//                ZipEntry entry = new ZipEntry(curPath
//                        + fileOrDirectory.getName());
//                //条目的信息写入底层流
//                out.putNextEntry(entry);
//                while ((bytes_read = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, bytes_read);
//                }
//                out.closeEntry();
//            } else {
//                // 压缩目录
//                File[] entries = fileOrDirectory.listFiles();
//                for (int i = 0; i < entries.length; i++) {
//                    // 递归压缩，更新curPaths
//                    zipFileOrDirectory(out, entries[i], curPath
//                            + fileOrDirectory.getName() + "/");
//                }
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            // throw ex;
//        } finally {
//            if (in != null) {
//                try {
//                    in.close();
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    public static void unzip(String zipFileName, String outputDirectory)
//            throws IOException {
//        ZipFile zipFile = null;
//        try {
//            zipFile = new ZipFile(zipFileName);
//            Enumeration e = zipFile.entries();
//            ZipEntry zipEntry = null;
//            File dest = new File(outputDirectory);
//            dest.mkdirs();
//            while (e.hasMoreElements()) {
//                zipEntry = (ZipEntry) e.nextElement();
//                String entryName = zipEntry.getName();
//                InputStream in = null;
//                FileOutputStream out = null;
//                try {
//                    if (zipEntry.isDirectory()) {
//                        String name = zipEntry.getName();
//                        name = name.substring(0, name.length() - 1);
//                        File f = new File(outputDirectory + File.separator
//                                + name);
//                        f.mkdirs();
//                    } else {
//                        int index = entryName.lastIndexOf("\\");
//                        if (index != -1) {
//                            File df = new File(outputDirectory + File.separator
//                                    + entryName.substring(0, index));
//                            df.mkdirs();
//                        }
//                        index = entryName.lastIndexOf("/");
//                        if (index != -1) {
//                            File df = new File(outputDirectory + File.separator
//                                    + entryName.substring(0, index));
//                            df.mkdirs();
//                        }
//                        File f = new File(outputDirectory + File.separator
//                                + zipEntry.getName());
//                        // f.createNewFile();
//                        in = zipFile.getInputStream(zipEntry);
//                        out = new FileOutputStream(f);
//                        int c;
//                        byte[] by = new byte[1024];
//                        while ((c = in.read(by)) != -1) {
//                            out.write(by, 0, c);
//                        }
//                        out.flush();
//                    }
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                    throw new IOException("解压失败：" + ex.toString());
//                } finally {
//                    if (in != null) {
//                        try {
//                            in.close();
//                        } catch (IOException ex) {
//                        }
//                    }
//                    if (out != null) {
//                        try {
//                            out.close();
//                        } catch (IOException ex) {
//                        }
//                    }
//                }
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            throw new IOException("解压失败：" + ex.toString());
//        } finally {
//            if (zipFile != null) {
//                try {
//                    zipFile.close();
//                } catch (IOException ex) {
//                }
//            }
//        }
//    }
}
