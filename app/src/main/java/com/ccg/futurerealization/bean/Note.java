package com.ccg.futurerealization.bean;

import org.litepal.crud.LitePalSupport;

/** A personal note stored only in the local database. */
public class Note extends LitePalSupport {
    private long id;
    private String title;
    private String content;
    private long updatedAt;
    public long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getContent() { return content; }
    public void setContent(String value) { content = value; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long value) { updatedAt = value; }
}
