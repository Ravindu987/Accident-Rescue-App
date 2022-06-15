package com.example.bluetooth_hc05;

public class ContactModel {

    int ID;
    String name, number;

    public ContactModel(int ID, String name, String number) {
        this.ID = ID;
        this.name = name;
        this.number = number;
    }

    @Override
    public String toString() {
        return "ContactModel{" +
                "ID=" + ID +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                '}';
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
