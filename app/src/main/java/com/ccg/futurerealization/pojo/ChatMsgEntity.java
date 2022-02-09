package com.ccg.futurerealization.pojo;

import java.sql.Date;
import java.sql.Time;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 21-12-20 下午3:34
 * @Version: 1.0
 */
public class ChatMsgEntity {

    private Long accountId;

    private String name;

    private String msg;

    private ChatType chatType;

    private Date date;

    private Time time;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public void setChatType(ChatType chatType) {
        this.chatType = chatType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return "ChatMsgEntity{" +
                "accountId=" + accountId +
                ", name='" + name + '\'' +
                ", msg='" + msg + '\'' +
                ", chatType=" + chatType +
                ", date=" + date +
                ", time=" + time +
                '}';
    }
}
