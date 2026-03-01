package com.example.backend.dto;

public class PersonalInfoDTO {

    private String name;
    private String email;

    public PersonalInfoDTO() {
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
}