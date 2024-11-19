package com.org.smallcircle.model;

import android.net.Uri;

public class ModelImagePicked {

    String id = "";
    Uri imageUri = null;
    String imageUrl = null;
    Boolean fromInternet = false;
    private long timestamp;

    public ModelImagePicked(String id, Uri imageUri, String imageUrl, Boolean fromInternet) {
        this.id = id;
        this.imageUri = imageUri;
        this.imageUrl = imageUrl;
        this.fromInternet = fromInternet;
    }

    // Constructor untuk gambar baru
    public ModelImagePicked(Uri imageUri) {
        this.imageUri = imageUri;
        this.fromInternet = false;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getFromInternet() {
        return fromInternet;
    }

    public void setFromInternet(Boolean fromInternet) {
        this.fromInternet = fromInternet;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
