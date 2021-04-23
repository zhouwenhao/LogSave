package com.timark.logsave;

import android.content.Context;
import android.util.Log;

/**
 *
 * 业务无感知，插桩方式实现
 * 设计方案
 *              LogSaveManager  总入口
 *              WriteSync   写入锁
 *      WriteLog  写文件和文件管理    ExtractLog  提取文件以上传/复制
 *
 */
public class LogSaveManager {

    private static final int FILE = 0x10;
    private static final int JSON = 0x20;
    private static final int XML  = 0x30;

    private volatile static LogSaveManager mSelf;
    private Context mAppContext;

    private WriteSync mWriteSync;

    public static LogSaveManager getInstance(){
        if (mSelf == null){
            synchronized (LogSaveManager.class){
                if (mSelf == null){
                    mSelf = new LogSaveManager();
                }
            }
        }
        return mSelf;
    }

    public void init(Context context){
        if (mAppContext != null){
            throw new RuntimeException("不支持重复init");
        }
        mAppContext = context.getApplicationContext();

        init("");
    }

    /**
     *
     * @param context
     * @param dirPath 日志存储路径
     */
    public void init(Context context, String dirPath){
        if (mAppContext != null){
            throw new RuntimeException("不支持重复init");
        }
        mAppContext = context.getApplicationContext();

        init(dirPath);
    }

    /**
     * 初始化
     * @param context
     * @param dirPath 日志存储路径
     * @param maxRecordDayNum 最大记录多少天
     * @param maxQueneSize 队列上限
     * @param maxLogFileLine 单个log文件最多记录多少行
     * @param zipPwd zip压缩包密码
     */
    public void init(Context context, String dirPath, int maxRecordDayNum, int maxQueneSize, int maxLogFileLine, String zipPwd){
        if (mAppContext != null){
            throw new RuntimeException("不支持重复init");
        }
        mAppContext = context.getApplicationContext();
        LogConfig.mMaxRecordDayNum = maxRecordDayNum;
        LogConfig.mMaxQueneSize = maxQueneSize;
        LogConfig.mMaxLogFileLine = maxLogFileLine;
        LogConfig.mZipPassword = zipPwd;

        init(dirPath);
    }

    private void init(String dirPath){
        mWriteSync = new WriteSync(new WriteLog(mAppContext, dirPath), new ExtractLog(mAppContext));
    }

    public void record(int type, String tag, String msg){
        if (mAppContext == null){
            return;
        }
        mWriteSync.addLog(generateType(type), tag, msg);
    }

    public void copy(String desDirPath, ExtractCall extractCall){
        if (mAppContext == null){
            return;
        }
        mWriteSync.switchCopyLog(desDirPath, extractCall);
    }

    public void upload(ExtractCall extractCall){
        if (mAppContext == null){
            return;
        }
        mWriteSync.switchUploadLog(extractCall);
    }

    private String generateType(int type){
        switch (type){
            case Log.VERBOSE:
                return "V";
            case Log.DEBUG:
                return "D";
            case Log.INFO:
                return "I";
            case Log.ERROR:
                return "E";
            case Log.ASSERT:
                return "A";
            case Log.WARN:
                return "W";
            case FILE:
                return "F";
            case JSON:
                return "J";
            case XML:
                return "X";

            default:
                return "N";
        }
    }
}
