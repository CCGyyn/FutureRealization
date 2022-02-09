package com.ccg.futurerealization.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.LitePalSupport;

import java.util.List;

/**
 * @Description: 记账分类表
 * @Author: cgaopeng
 * @CreateDate: 21-12-17 上午10:28
 * @Version: 1.0
 * @update: 22-02-09 与表Account建立关联, 多对一关系
 */
public class AccountCategory extends LitePalSupport implements Parcelable {

    private Long id;
    /**
     * 一级 如食 其子类就是早中晚三餐
     */
    private String category;
    /**
     * 父类id
     */
    private Long pid;

    private List<Account> accountList;

    public AccountCategory() {}

    protected AccountCategory(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        category = in.readString();
        if (in.readByte() == 0) {
            pid = null;
        } else {
            pid = in.readLong();
        }
    }

    public static final Creator<AccountCategory> CREATOR = new Creator<AccountCategory>() {
        @Override
        public AccountCategory createFromParcel(Parcel in) {
            return new AccountCategory(in);
        }

        @Override
        public AccountCategory[] newArray(int size) {
            return new AccountCategory[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(category);
        dest.writeLong(pid);
    }

    public List<Account> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
    }

}
