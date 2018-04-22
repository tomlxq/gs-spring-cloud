package com.example.entity;

import javax.validation.constraints.NotNull;

public class LoginUserReq {

    @NotNull
    private String username;

    @NotNull
    private String password;

    public LoginUserReq() {
    }

    public String getUsername() {
        return username;
    }

    public LoginUserReq setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LoginUserReq setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String toString() {
        return "LoginUserReq{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}