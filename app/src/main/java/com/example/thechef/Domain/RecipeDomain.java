package com.example.thechef.Domain;

import java.io.Serializable;
import java.util.Map;

public class RecipeDomain implements Serializable {
    private String recipeId;
    private String foodName;
    private String description;
    private String imageUrl;
    private String time;
    private Double score;
    private int ratingCount; // Added field to store the number of ratings
    private Map<String, String> ingredients; // Store ingredients as key-value pairs
    private String steps; // Changed from List<String> to String

    public RecipeDomain(String recipeId, String foodName, String description, String imageUrl, String time, Double score, int ratingCount, Map<String, String> ingredients, String steps) {
        this.recipeId = recipeId;
        this.foodName = foodName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.time = time;
        this.score = score;
        this.ratingCount = ratingCount; // Initialize ratingCount
        this.ingredients = ingredients;
        this.steps = steps; // Updated constructor
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

    public int getRatingCount() { // Getter for ratingCount
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) { // Setter for ratingCount
        this.ratingCount = ratingCount;
    }

    public Map<String, String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Map<String, String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getSteps() {
        return steps; // Getter for steps
    }

    public void setSteps(String steps) {
        this.steps = steps; // Setter for steps
    }
}
