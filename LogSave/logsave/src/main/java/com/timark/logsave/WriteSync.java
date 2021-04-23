package com.timark.logsave;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class WriteSync {

    private final int QUENE_MAN_SIZE = 30000;

    private WriteLog mWriteLog;
    private ExtractLog mExtractLog;

    private Object mAddSync = new Object();
    private Object mFileSync = new Object();
    private Object mUploadSync = new Object();
    private Object mCopySync = new Object();
    private boolean mIsFileIng = false;
    private Queue<LogObject> mQuene;

    private volatile boolean mIsNeedUpload = false;
    private volatile boolean mIsNeedCopy = false;
    private String mCopyDestDirPath = "";

    private Thread mFileThread;
    private FileRun mFileRun;

    private ExtractCall mUploadCall;
    private ExtractCall mCopyCall;

    public WriteSync(WriteLog writeLog, ExtractLog extractLog){
        this.mWriteLog = writeLog;
        this.mExtractLog = extractLog;

        mQuene = new LinkedList<>();
        mFileRun = new FileRun();
        mFileThread = new Thread(mFileRun, "LogFileThread");
        mFileThread.start();
    }

    public void switchUploadLog(ExtractCall extractCall){
        synchronized (mUploadSync){
            mUploadCall = extractCall;
        }
        mIsNeedUpload = true;
        if (!mIsFileIng){
            synchronized (mFileSync){
                mFileSync.notify();
            }
        }
    }

    public void switchCopyLog(String destPathDir, ExtractCall extractCall){
        synchronized (mCopySync){
            mCopyDestDirPath = destPathDir;
            mCopyCall = extractCall;
        }
        mIsNeedCopy = true;
        if (!mIsFileIng){
            synchronized (mFileSync){
                mFileSync.notify();
            }
        }
    }

    public void addLog(String type, String tag, String msg){
        LogObject logObject = new LogObject(type, tag, msg);
        synchronized (mAddSync){
            if (mQuene.size() < QUENE_MAN_SIZE) {
                mQuene.offer(logObject);
            }
        }
        if (!mIsFileIng){
            synchronized (mFileSync){
                mFileSync.notify();
            }
        }
    }

    private LogObject popLog(){
        synchronized (mAddSync){
            if (mQuene.isEmpty()){
                return null;
            }else {
                return mQuene.poll();
            }
        }
    }

    private void doUploadLog(ExtractCall extractCall){
        mExtractLog.upload(mWriteLog.getRootDirPath(), extractCall);
    }

    private void doCopyLog(String destDirPath, ExtractCall extractCall){
        mExtractLog.copy(mWriteLog.getRootDirPath(), destDirPath, extractCall);
    }

    private void doWriteLog(List<LogObject> logObjectList){
        mWriteLog.writeLog(logObjectList);
    }

    private void doWriteLog(LogObject logObject){
        mWriteLog.writeLog(logObject);
    }

    private class FileRun implements Runnable{

        @Override
        public void run() {

            List<LogObject> mLogList = new ArrayList<>(0);

            while (true){
                synchronized (mFileSync){
                    mIsFileIng = true;
                    if (mIsNeedUpload){
                        //进行上传
                        doUploadLog(mUploadCall);
                        //上传成功后
                        mIsNeedUpload = false;
                    }else if (mIsNeedCopy){
                        synchronized (mCopySync){
//                            mCopyDestDirPath
                            //开始copy
                            doCopyLog(mCopyDestDirPath, mCopyCall);
                            //copy完成后
                            mIsNeedCopy = false;
                            mCopyDestDirPath = "";
                        }
                    }else {
                        LogObject logObject = popLog();
                        if (logObject == null){
                            if (mLogList.isEmpty()){
                                mIsFileIng = false;
                                try {
                                    mFileSync.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                //去写入
                                long startTime = System.currentTimeMillis();
                                doWriteLog(mLogList);
                                Log.d("Log-run", "saveSize=" + mLogList.size() + "saveTime=" + (System.currentTimeMillis() - startTime));
                                //写入完毕后
                                mLogList.clear();
                            }
                        }else {
                            mLogList.add(logObject);
                        }
                    }
                }
            }
        }
    }
}
