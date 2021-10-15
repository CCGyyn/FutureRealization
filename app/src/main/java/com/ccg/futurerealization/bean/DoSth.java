package com.ccg.futurerealization.bean;

import org.litepal.crud.LitePalSupport;

/**
 * 想要做的事情数据库
 * @author：cgaopeng on 2021/10/15 13:54
 */
public class DoSth extends LitePalSupport {

    private Long id;
    /**
     * 想做的内容
     */
    private String future_content;
    /**
     * 是否已实现
     */
    private Boolean state;
    /**
     * 0:默认，无特别注意
     * 1：长期实现
     * 2：短期实现
     */
    private Integer type;

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFuture_content() {
        return future_content;
    }

    public void setFuture_content(String future_content) {
        this.future_content = future_content;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
