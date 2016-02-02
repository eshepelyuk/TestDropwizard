package com.infopulse.dropwizard.news;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class NewsItem {

    private Long id;

    @Length(max = 255)
    @NotBlank
    private String title;

    @Length(max = 255)
    @NotBlank
    private String author;

    @NotBlank
    @Length(max = 2048)
    private String content;

    @NotNull
    private Date publishDate = new Date();

    public NewsItem() {
    }

    public NewsItem(Long id, String title, String author, String content, Date publishDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.content = content;
        this.publishDate = publishDate;
    }

    public NewsItem(String title, String author, String content, Date publishDate) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.publishDate = publishDate;
    }

    @JsonProperty
    public Long getId() {
        return id;
    }

    @JsonProperty
    public String getTitle() {
        return title;
    }

    @JsonProperty
    public String getContent() {
        return content;
    }

    @JsonProperty
    public String getAuthor() {
        return author;
    }

    @JsonProperty
    public Date getPublishDate() {
        return publishDate;
    }
}

