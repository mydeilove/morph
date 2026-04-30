package com.example.themorph.DeafMap;

public class CollectPoiBean {
    private String poiId;
    private String title;
    private String address;

    public CollectPoiBean(String poiId, String title, String address) {
        this.poiId = poiId;
        this.title = title;
        this.address = address;
    }

    public String getPoiId() { return poiId; }
    public String getTitle() { return title; }
    public String getAddress() { return address; }
}