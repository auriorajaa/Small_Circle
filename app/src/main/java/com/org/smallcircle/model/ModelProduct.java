package com.org.smallcircle.model;

import androidx.annotation.Keep;

public class ModelProduct {

    private String address;
    private String brand;
    private String category;
    private String condition;
    private String description;
    private String id;
    private double latitude;
    private double longitude;
    private String price;
    private String status;
    private long timestamp;
    private String title;
    private String uid;
    private boolean favorite;

    // Constructor
    public ModelProduct(String address, String brand, String category, String condition, String description, String id, double latitude, double longitude, String price, String status, long timestamp, String title, String uid, boolean favorite) {
        this.address = address;
        this.brand = brand;
        this.category = category;
        this.condition = condition;
        this.description = description;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.price = price;
        this.status = status;
        this.timestamp = timestamp;
        this.title = title;
        this.uid = uid;
        this.favorite = favorite;
    }

    @Keep
    public ModelProduct() {}

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}