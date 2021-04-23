package com.timark.logsave;

import android.content.Context;
import android.os.strictmode.NonSdkApiUsedViolation;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * 文件规则：
 *
 *  1、目录以年月日为文件夹
 *  2、某个文件中的日志文件数目无上限
 *  3、某个日志文件的行数上限为一万行，超出则创建新文件
 *  4、仅保留5天的日志
 *
 *  5、默认存放目录为 APP内部目录/log/xxxx-xx-xx/xxxxxx.log
 */
class WriteLog {
    private final String DIR_PATH = "/log/";
    private String mSaveLogDir = null;

    public WriteLog(Context applicationContext, String dirPath){
        if (TextUtils.isEmpty(dirPath)) {
            mSaveLogDir = applicationContext.getFilesDir().getAbsolutePath() + DIR_PATH;
        }else {
            if (dirPath.endsWith(File.separator)){
                mSaveLogDir = dirPath;
            }else {
                mSaveLogDir = dirPath + File.separator;
            }
        }
    }

    public String getRootDirPath(){
        return mSaveLogDir;
    }

    public void writeLog(List<LogObject> logObjectList){
        String yhdStr = "";
        String dirPath = "";
        String filePath = "";
        int[] line = new int[1];
        FileWriter writer = null;
        for (LogObject logObject : logObjectList) {
            String timeStr = new SimpleDateFormat("HH:mm:ss-SSS").format(new Date(logObject.curTime));
            String yyyyStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date(logObject.curTime));
            if (!yyyyStr.equals(yhdStr)) {
                if (writer != null){
                    try {
                        writer.close();
                    }catch (Exception e){

                    }
                }
                yhdStr = yyyyStr;
                dirPath = createDayDir(logObject.curTime);
                filePath = createLogFile(dirPath, line);
                try {
                    writer = new FileWriter(new File(filePath), true);
                } catch (Exception e) {

                }
            }
            line[0] ++;
            try {
                writer.write((timeStr + "——>" + logObject.type + "——>" + "pid=" + logObject.pid + "——>" + logObject.tag + ":" + logObject.msg) + "\n");
            }catch (Exception e){

            }
            if (line[0] >= LogConfig.mMaxLogFileLine){
                yhdStr = "";
            }
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {

            }
        }
    }

    public void writeLog(LogObject logObject){
        int[] line = new int[1];
        String timeStr = new SimpleDateFormat("HH:mm:ss-SSS").format(new Date(logObject.curTime));
        String dirPath = createDayDir(logObject.curTime);
        String filePath = createLogFile(dirPath, line);
        saveLog(new File(filePath), timeStr + "————>" + logObject.type + "————>" + logObject.tag + ":" + logObject.msg);
    }

    private String createDayDir(long time){
        List<String> dirNames = new ArrayList<>(LogConfig.mMaxRecordDayNum);
        for (int i = 0; i < LogConfig.mMaxRecordDayNum; ++i){
            dirNames.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date(time - i*(24 * 60 * 60 * 1000))));
        }

        File rootDir = new File(mSaveLogDir);
        if (!rootDir.exists()){
            rootDir.mkdirs();
        }

        String dirPath = mSaveLogDir + dirNames.get(0) + File.separator;
        File curDir = new File(dirPath);
        if (!curDir.exists()){
            curDir.mkdir();
        }

        File[] childFiles = rootDir.listFiles();
        if (childFiles != null){
            for (File f : childFiles){
                if (f.isDirectory()){
                    if (!dirNames.contains(f.getName())){
                        deleteFile(f);
                    }
                }else {
                    deleteFile(f);
                }
            }
        }
        return dirPath;
    }

    private String createLogFile(String dirPath, int[] line) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        String filePath = null;
        filePath = getNotFullFile(files, line);
        if (TextUtils.isEmpty(filePath)){
            filePath = createFile(dirPath);
            line[0] = 0;
        }
        return filePath;
    }

    private void saveLog(File file, String lineStr){
        lineStr += "\n";
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(file, true);
            writer.write(lineStr);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                Log.e("Log-saveLog", e.getMessage());
            }
        }
    }

    private void deleteFile(File file){
        if (!file.exists()){
            return;
        }
        if (!file.isDirectory()){
            file.delete();
        }else {
            File[] files = file.listFiles();
            if (files != null){
                for (File f : files){
                    deleteFile(f);
                }
            }
            file.delete();
        }
    }

    private String createFile(String dirPath) {
        String filePath = dirPath + System.currentTimeMillis() + ".log";
        File file = new File(filePath);
        try {
            file.createNewFile();
        }catch (Exception e){
            Log.e("Log-createFile", e.getMessage());
        }
        return filePath;
    }

    private String getNotFullFile(File[] files, int[] line){
        if (files == null || files.length == 0){
            return null;
        }
        File curFile = null;
        long fileNameLong = -1;
        for (File f : files){
            String fileP = f.getName();
            String fileName = fileP.substring(0, fileP.lastIndexOf("."));
            long curFileNameLong = 0;
            try {
                curFileNameLong = Long.parseLong(fileName);
            }catch (Exception e){

            }
            if (curFileNameLong > fileNameLong){
                curFile = f;
                fileNameLong = curFileNameLong;
            }
        }
        if (curFile != null) {
            if (checkFileIsFull(curFile, line)) {
                curFile = null;
            }
        }
        if (curFile != null){
            return curFile.getPath();
        }else {
            return null;
        }
    }

    private boolean checkFileIsFull(File f, int[] line){
        if (f.isDirectory()){
            return true;
        }
        if (!f.getPath().endsWith(".log")){
            return true;
        }

        int cnt = getFileLineCounts(f);
        if (cnt < LogConfig.mMaxLogFileLine){
            line[0] = cnt;
            return false;
        }else {
            return true;
        }
    }

    private int getFileLineCounts(File file) {
//        int cnt = 0;
//        InputStream is = null;
//        try {
//            is = new BufferedInputStream(new FileInputStream(file));
//            byte[] c = new byte[1024];
//            int readChars = 0;
//            while ((readChars = is.read(c)) != -1) {
//                for (int i = 0; i < readChars; ++i) {
//                    if (c[i] == '\n') {
//                        ++cnt;
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            cnt = -1;
//            ex.printStackTrace();
//        } finally {
//            try {
//                is.close();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//        return cnt;


        int cnt = 0;
        LineNumberReader reader = null;
        try {
            reader = new LineNumberReader(new FileReader(file));
            String lineRead = "";
            while ((lineRead = reader.readLine()) != null) {
            }
            cnt = reader.getLineNumber();
        } catch (Exception ex) {
            cnt = -1;
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return cnt;
    }
}
