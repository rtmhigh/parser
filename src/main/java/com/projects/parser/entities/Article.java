package com.projects.parser.entities;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class Article {
    private Date date;
    private String title;
    private List<Hub> hubs;
    private String image;
    private String readMore;
    private List<String> content;
}
