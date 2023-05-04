package com.wuav.client.be;

public class Customer {
    private int id;
    private String name;

    private String email;
    private String phoneNumber;

    private String type;

    private Address address; // one to one

//    public Customer(int id, String name, String email, String phoneNumber, String type, Address address) {
//        this.id = id;
//        this.name = name;
//        this.email = email;
//        this.phoneNumber = phoneNumber;
//        this.type = type;
//        this.address = address;
//    }

    public Customer(int id, String name, String email, String phoneNumber, String type) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.type = type;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", type='" + type + '\'' +
                ", address=" + address +
                '}';
    }
}
