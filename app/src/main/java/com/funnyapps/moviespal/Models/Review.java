
package com.funnyapps.moviespal.Models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

import java.io.Serializable;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Review implements Serializable{

    @Expose
    private String author;
    @Expose
    private String content;
    @Expose
    private String id;
    @Expose
    private String url;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
