package com.example.thechef;

public class User {
    private String name;
    private String email;
    private String password;
    private String imageUrl;  // Optional field for user's profile image

    // Constructor with all fields
    public User(String name, String email, String password, String imageUrl) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.imageUrl = imageUrl;
    }

    // Constructor without imageUrl (if the user doesn't have a profile image)
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.imageUrl = "";  // Default value for imageUrl
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Optional: Add a method to update the password
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    // Optional: Add a method to reset the imageUrl
    public void resetImageUrl() {
        this.imageUrl = "";
    }

    // Optional: Override the toString() method to display user information
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
