package com.ccg.futurerealization.pojo;

/**
 * @Description:信息类型,发送/接收
 * @Author: cgaopeng
 * @CreateDate: 21-12-20 下午3:33
 * @Version: 1.0
 */
public enum ChatType {
    SEND_MSG(1),
    RECEIVE_MSG(0);

    private int code;

    ChatType(int i) {
        code = i;
    }

    public int getCode() {
        return code;
    }
}
