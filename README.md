# LogSave
安卓日志存储框架：支持自动存储、压缩、可调最多保存多少天



使用方式



初始化

```java
LogSaveManager.getInstance().init(getApplicationContext());
```



记录

```java
LogSaveManager.getInstance().record(Log.DEBUG, "ceshiTag", "ceshiMsg=" + i);
```



上传压缩

```java
LogSaveManager.getInstance().upload(new ExtractCall() {
    @Override
    public void zip(int type, boolean isEnd, String filePath) {
        Log.d("ceshi", "type=" + type + "&&&isEnd=" + isEnd + "&&&filePath=" + filePath);
    }
});
```



复制压缩

```java
LogSaveManager.getInstance().copy("", new ExtractCall() {
    @Override
    public void zip(int type, boolean isEnd, String filePath) {
        
    }
});
```





设计思路：

```java
业务无感知，插桩方式实现
设计方案
             LogSaveManager  总入口
             WriteSync   写入锁
     WriteLog  写文件和文件管理    ExtractLog  提取文件以上传/复制
```



文件规则

```java
文件规则：
 1、目录以年月日为文件夹
 2、某个文件中的日志文件数目无上限
 3、某个日志文件的行数上限为一万行，超出则创建新文件
 4、仅保留5天的日志
 5、默认存放目录为 APP内部目录/log/xxxx-xx-xx/xxxxxx.log
```