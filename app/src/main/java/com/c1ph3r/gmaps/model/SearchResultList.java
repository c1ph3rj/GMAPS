package com.c1ph3r.gmaps.model;

public class SearchResultList {
    String Title;
    String Body;
    String placeId;

    public SearchResultList(String title, String body, String placeId) {
        Title = title;
        Body = body;
        this.placeId = placeId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getBody() {
        return Body;
    }

    public void setBody(String body) {
        Body = body;
    }
}
