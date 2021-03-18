package com.timark.logsave;

class LogObject {

    public String type;
    public String tag;
    public String msg;
    public long curTime;

    public LogObject(String type, String tag, String msg) {
        this.type = type;
        this.tag = tag;
        this.msg = msg;
        this.curTime = System.currentTimeMillis();
    }

}
