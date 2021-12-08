package com.ccg.futurerealization.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.LitePalSupport;

/**
 * 想要做的事情数据库
 * @author：cgaopeng on 2021/10/15 13:54
 */
public class DoSth extends LitePalSupport implements Parcelable {

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

    public DoSth() {
    }

    protected DoSth(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        future_content = in.readString();
        byte tmpState = in.readByte();
        state = tmpState == 0 ? null : tmpState == 1;
        if (in.readByte() == 0) {
            type = null;
        } else {
            type = in.readInt();
        }
    }

    public static final Creator<DoSth> CREATOR = new Creator<DoSth>() {
        @Override
        public DoSth createFromParcel(Parcel in) {
            return new DoSth(in);
        }

        @Override
        public DoSth[] newArray(int size) {
            return new DoSth[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(future_content);
        dest.writeByte((byte)(state ? 1 : 0));
        dest.writeInt(type);
    }

    @Override
    public String toString() {
        return "DoSth{" +
                "id=" + id +
                ", future_content='" + future_content + '\'' +
                ", state=" + state +
                ", type=" + type +
                '}';
    }
}
