package com.ccg.futurerealization.bean;

import org.litepal.crud.LitePalSupport;

/**
 * @Description: 记账分类表
 * @Author: cgaopeng
 * @CreateDate: 21-12-17 上午10:28
 * @Version: 1.0
 */
public class AccountCategory extends LitePalSupport {

    private Long id;
    /**
     * 一级 如食 其子类就是早中晚三餐
     */
    private String category;
    /**
     * 父类id
     */
    private Long pid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    @Override
    public String toString() {
        return "AccountCategory{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", pid=" + pid +
                '}';
    }
}
