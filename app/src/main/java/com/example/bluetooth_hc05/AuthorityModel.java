package com.example.bluetooth_hc05;

public class AuthorityModel {

    int id;
    String name, lang, lat, contact;

    public AuthorityModel(int id, String name, String lat, String lang, String contact) {
        this.id = id;
        this.name = name;
        this.lang = lang;
        this.lat = lat;
        this.contact = contact;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
