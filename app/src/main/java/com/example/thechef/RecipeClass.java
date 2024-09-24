package com.example.thechef;

import java.util.ArrayList;
import java.util.List;

public class RecipeClass {
    private String foodName;
    private String description;
//    private String imageUrl;
    private List<Ingredient> ingredients;  // Store the list of ingredients
    private String time;  // Time attribute
    private double score;  // Score attribute

    // Constructor
    public RecipeClass(String foodName, String description, String time, double score) {
        this.foodName = foodName;
        this.description = description;
//        this.imageUrl = imageUrl;
        this.time = time;
        this.score = score;
        this.ingredients = new ArrayList<>();  // Initialize the ingredients list
    }

    // Getters and Setters
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



    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    // Add a method to add a new ingredient
    public void addIngredient(String ingredientName, String quantity) {
        ingredients.add(new Ingredient(ingredientName, quantity));
    }
}
