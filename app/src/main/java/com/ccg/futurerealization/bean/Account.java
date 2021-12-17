package com.ccg.futurerealization.bean;

import org.litepal.crud.LitePalSupport;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

/**
 * @Description:记账表
 * @Author: cgaopeng
 * @CreateDate: 21-12-17 上午10:35
 * @Version: 1.0
 */
public class Account extends LitePalSupport {

    private Long id;
    /**
     * yyyy-MM-dd
     */
    private Date date;
    /**
     * hh-mm-ss
     */
    private Time time;
    /**
     * 1/0 支出/收入
     */
    private Integer type;
    /**
     * 金额
     */
    private BigDecimal amount;
    /**
     * 备注
     */
    private String remark;
    /**
     * 分类id，联表查询分类
     */
    private AccountCategory category;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public AccountCategory getCategory() {
        return category;
    }

    public void setCategory(AccountCategory category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", date=" + date +
                ", time=" + time +
                ", type=" + type +
                ", amount=" + amount +
                ", remark='" + remark + '\'' +
                ", category=" + category +
                '}';
    }
}
