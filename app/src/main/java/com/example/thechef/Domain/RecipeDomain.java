package com.example.thechef.Domain;

import java.io.Serializable;

public class RecipeDomain implements Serializable {
    private String recipeId;
    private String foodName;
    private String description;
    private String imageUrl;
    private String videoUrl;
    private String time;
    private Double score;
    private int ratingCount; //added field to store the number of ratings
    private String ingredients; //changed from Map<String, String> to String
    private String steps; //changed from List<String> to String
    private String category; //new attribute for category
    private String userId; //new attribute for user ID

    //updated constructor to include the new userId attribute
    public RecipeDomain(String recipeId, String foodName, String description, String imageUrl,String videoUrl, String time, Double score, int ratingCount, String ingredients, String steps, String category, String userId) {
        this.recipeId = recipeId;
        this.foodName = foodName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.videoUrl=videoUrl;
        this.time = time;

        this.score = score;
        this.ratingCount = ratingCount;
        this.ingredients = ingredients; //initialize ingredients as a single string
        this.steps = steps;
        this.category = category; //initialize category
        this.userId = userId; //initialize user ID
    }

    // Getters and Setters
    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl(String videoUrl) {
        return imageUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }





    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getIngredients() { // Getter for ingredients
        return ingredients;
    }

    public void setIngredients(String ingredients) { // Setter for ingredients
        this.ingredients = ingredients;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getCategory() { // Getter for category
        return category;
    }

    public void setCategory(String category) { // Setter for category
        this.category = category;
    }

    public String getUserId() { // Getter for userId
        return userId;
    }

    public void setUserId(String userId) { // Setter for userId
        this.userId = userId;
    }
}
