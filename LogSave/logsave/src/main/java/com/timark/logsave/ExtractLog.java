package com.timark.logsave;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ExtractLog {

    private final String ZIP_FILE_NAME = "log.zip";
    private String mZipPath = "";

    public ExtractLog(Context applicationContext){
        mZipPath = applicationContext.getFilesDir().getAbsolutePath() + "/zip/";
    }

    public void upload(String dirPath, ExtractCall extractCall){
        String filePath = mZipPath + ZIP_FILE_NAME;
        try {
            File dirFile = new File(mZipPath);
            if (!dirFile.exists()){
                dirFile.mkdirs();
            }
        }catch (Exception e){
            Log.e("Log-upload", e.getMessage());
        }
        if (extractCall != null){
            extractCall.zip(ExtractCall.TYPE_UPLOAD, false, filePath);
        }
        zip(dirPath, filePath);
        if (extractCall != null){
            extractCall.zip(ExtractCall.TYPE_UPLOAD, true, filePath);
        }
    }

    public void copy(String dirPath, String desDirPath, ExtractCall extractCall){
        if (!desDirPath.endsWith(File.separator)){
            desDirPath += File.separator;
        }
        try {
            File dirFile = new File(desDirPath);
            if (!dirFile.exists()){
                dirFile.mkdirs();
            }
        }catch (Exception e){
            Log.e("Log-upload", e.getMessage());
        }
        String filePath = desDirPath + ZIP_FILE_NAME;
        if (extractCall != null){
            extractCall.zip(ExtractCall.TYPE_COPY, false, filePath);
        }
        zip(dirPath, filePath);
        if (extractCall != null){
            extractCall.zip(ExtractCall.TYPE_COPY, false, filePath);
        }
    }

    private void zip(String srcDirPath, String desFilePath){
        File file = new File(desFilePath);
        if (file.exists()){
            file.delete();
        }
        try {
            ZipUtil.zip(srcDirPath, desFilePath);
        }catch (Exception e){
            Log.e("Log-upload", e.getMessage());
        }
    }

//    private void zipFolder(String srcFileString, String zipFileString) throws Exception {
//        //创建ZIP
//        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zipFileString));
//        //创建文件
//        File file = new File(srcFileString);
//        //压缩
//        zipFiles(file.getParent()+ File.separator, file.getName(), outZip);
//        //完成和关闭
//        outZip.finish();
//        outZip.close();
//    }
//
//    /**
//     * 压缩文件
//     *
//     * @param folderString
//     * @param fileString
//     * @param zipOutputSteam
//     * @throws Exception
//     */
//    private void zipFiles(String folderString, String fileString, ZipOutputStream zipOutputSteam) throws Exception {
//        if (zipOutputSteam == null) {
//            return;
//        }
//        File file = new File(folderString + fileString);
//        if (file.isFile()) {
//            ZipEntry zipEntry = new ZipEntry(fileString);
//            FileInputStream inputStream = new FileInputStream(file);
//            zipOutputSteam.putNextEntry(zipEntry);
//            int len;
//            byte[] buffer = new byte[4096];
//            while ((len = inputStream.read(buffer)) != -1) {
//                zipOutputSteam.write(buffer, 0, len);
//            }
//            zipOutputSteam.closeEntry();
//        } else {
//            //文件夹
//            String fileList[] = file.list();
//            //没有子文件和压缩
//            if (fileList.length <= 0) {
//                ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
//                zipOutputSteam.putNextEntry(zipEntry);
//                zipOutputSteam.closeEntry();
//            }
//            //子文件和递归
//            for (int i = 0; i < fileList.length; i++) {
//                zipFiles(folderString+fileString+"/",  fileList[i], zipOutputSteam);
//            }
//        }
//    }
}
