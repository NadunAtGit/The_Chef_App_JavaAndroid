package com.example.thechef.Domain;

import java.io.Serializable;

public class RecipeDomain implements Serializable {
    private String title;
    private String description;
    private String picurl;
    private String ingredients;
//    private String steps;
    private int time;
    private double score;

    public RecipeDomain(String title, String description, String picurl, String ingredients, String steps, int time, double score) {
        this.title = title;
        this.description = description;
        this.picurl = picurl;
        this.ingredients = ingredients;
//        this.steps = steps;
        this.time = time;
        this.score = score;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicurl() {
        return picurl;
    }

    public void setPicurl(String picurl) {
        this.picurl = picurl;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

//    public String getSteps() {
//        return steps;
//    }
//
//    public void setSteps(String steps) {
//        this.steps = steps;
//    }
//
    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
