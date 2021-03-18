package com.timark.logsave;

public interface ExtractCall {

    int TYPE_UPLOAD = 1;
    int TYPE_COPY = 2;

    void zip(int type, boolean isEnd, String filePath);
}
