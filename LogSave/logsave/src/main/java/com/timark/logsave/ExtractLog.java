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
        boolean isSuc = zip(dirPath, filePath);
        if (extractCall != null){
            extractCall.zip(ExtractCall.TYPE_UPLOAD, isSuc, filePath);
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
        boolean isSuc = zip(dirPath, filePath);
        if (extractCall != null){
            extractCall.zip(ExtractCall.TYPE_COPY, isSuc, filePath);
        }
    }

    private boolean zip(String srcDirPath, String desFilePath){
        Log.d("Log-upload", "src=" + srcDirPath);
        Log.d("Log-upload", "des=" + desFilePath);
        File file = new File(desFilePath);
        if (file.exists()){
            file.delete();
        }
        try {
            return ZipUtil.compressFolder(desFilePath, LogConfig.mZipPassword, srcDirPath);
        }catch (Exception e){
            Log.e("Log-upload", e.getMessage());
            return false;
        }
    }

//    private void zipFolder(String srcFileString, String zipFileString) throws Exception {
//        //??????ZIP
//        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zipFileString));
//        //????????????
//        File file = new File(srcFileString);
//        //??????
//        zipFiles(file.getParent()+ File.separator, file.getName(), outZip);
//        //???????????????
//        outZip.finish();
//        outZip.close();
//    }
//
//    /**
//     * ????????????
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
//            //?????????
//            String fileList[] = file.list();
//            //????????????????????????
//            if (fileList.length <= 0) {
//                ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
//                zipOutputSteam.putNextEntry(zipEntry);
//                zipOutputSteam.closeEntry();
//            }
//            //??????????????????
//            for (int i = 0; i < fileList.length; i++) {
//                zipFiles(folderString+fileString+"/",  fileList[i], zipOutputSteam);
//            }
//        }
//    }
}
