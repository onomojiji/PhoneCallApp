package com.proposeme.seven.phonecall.net;

/**
 * Describe: Stocker les données audio générées
 */
public class Message {

    public static final String MES_TYPE_AUDIO = "MES_TYPE_AUDIO"; //音频
    public static final String MES_TYPE_NORMAL = "MES_TYPE_NORMAL"; // 本
    private String msgtype;
    private String msgBody;
    private String msgIp;
    private long timestamp;
    private byte[] frame;
    private int sort;

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getFrame() {
        return frame;
    }

    public void setFrame(byte[] frame) {
        this.frame = frame;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getMsgIp() {
        return msgIp;
    }

    public void setMsgIp(String msgIp) {
        this.msgIp = msgIp;
    }
}
