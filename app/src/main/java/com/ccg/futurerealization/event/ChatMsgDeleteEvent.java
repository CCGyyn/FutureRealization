package com.ccg.futurerealization.event;

import com.ccg.futurerealization.pojo.ChatMsgEntity;

/**
 * @Description:
 * @Author: cgaopeng
 * @CreateDate: 22-2-9 下午4:05
 * @Version: 1.0
 */
public class ChatMsgDeleteEvent {

    private ChatMsgEntity mChatMsgEntity;

    public ChatMsgDeleteEvent(ChatMsgEntity chatMsgEntity) {
        mChatMsgEntity = chatMsgEntity;
    }

    public ChatMsgEntity getChatMsgEntity() {
        return mChatMsgEntity;
    }

    public static ChatMsgDeleteEvent getInstance(ChatMsgEntity chatMsgEntity) {
        return new ChatMsgDeleteEvent(chatMsgEntity);
    }
}
