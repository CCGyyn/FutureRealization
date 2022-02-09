package com.ccg.futurerealization.bean;

import org.litepal.crud.LitePalSupport;

/**
 * @Description:记账表
 * @Author: cgaopeng
 * @CreateDate: 21-12-17 上午10:35
 * @Version: 1.0
 * @update 22-02-09 重新设计表,修复没有date,amount column, date更改为string类型, amount更改为Integer类型 保存单位为分
 *      与表AccountCategory建立关联, 一对多
 */
public class Account extends LitePalSupport {

    private Long id;
    /**
     * yyyy-MM-dd
     */
    private String date;

    /**
     * 1/0 支出/收入
     */
    private Integer type;
    /**
     * 金额 单位 分,保存时需要乘以100存储
     */
    private Integer amount;
    /**
     * 备注
     */
    private String remark;
    /**
     * 分类id，联表查询分类
     */
    private AccountCategory accountCategory;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public AccountCategory getAccountCategory() {
        return accountCategory;
    }

    public void setAccountCategory(AccountCategory accountCategory) {
        this.accountCategory = accountCategory;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", date=" + date +
                ", type=" + type +
                ", amount=" + amount +
                ", remark='" + remark + '\'' +
                ", category=" + accountCategory +
                '}';
    }
}
