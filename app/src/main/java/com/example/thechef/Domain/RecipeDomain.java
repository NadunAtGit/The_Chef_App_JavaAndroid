package com.example.thechef.Domain;

import java.io.Serializable;

import java.util.Map;

public class RecipeDomain {
    private String foodName;
    private String description;
    private String imageUrl;
    private String time;
    private Double score;
    private Map<String, String> ingredients; // Store ingredients as key-value pairs

    public RecipeDomain(String foodName, String description, String imageUrl, String time, Double score, Map<String, String> ingredients) {
        this.foodName = foodName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.time = time;
        this.score = score;
        this.ingredients = ingredients;
    }

    // Getters and setters...
    public String getFoodName() {
        return foodName;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTime() {
        return time;
    }

    public Double getScore() {
        return score;
    }

    public Map<String, String> getIngredients() {
        return ingredients;
    }
}

