package com.example.service;

import com.example.entity.LoginDetail;
import com.example.entity.TokenDetail;

public interface LoginService {

    LoginDetail getLoginDetail(String username);

    String generateToken(TokenDetail tokenDetail);

}