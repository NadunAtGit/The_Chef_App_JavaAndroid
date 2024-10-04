package com.example.thechef;

public class User {
    private String userId;
    private String name;
    private String email;

    private String imageUrl;  // New attribute

    // Constructor with all fields, including imageUrl
    public User(String userId, String name, String email, String imageUrl) {
        this.userId = userId;
        this.name = name;
        this.email = email;

        this.imageUrl = imageUrl;  // Initialize imageUrl
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public String getImageUrl() {
        return imageUrl;  // Getter for imageUrl
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;  // Setter for imageUrl
    }

    // Optional: Add a method to update the password


    // Override the toString() method to display user information, including imageUrl
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +

                ", imageUrl='" + imageUrl + '\'' +  // Include imageUrl
                '}';
    }
}
