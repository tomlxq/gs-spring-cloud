package com.example.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class EntryPointUnauthorizedHandler implements AuthenticationEntryPoint {

    /**
     * 未登录或无权限时触发的操作
     * 返回  {"code":401,"message":"小弟弟，你没有携带 token 或者 token 无效！","data":""}
     * @param request
     * @param response
     * @param e
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        //返回json形式的错误信息
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setStatus(401);
        response.getWriter().println("{\"code\":401,\"message\":\"小弟弟，你没有携带 token 或者 token 无效！\",\"data\":\"\"}");
        response.getWriter().flush();


    }

}