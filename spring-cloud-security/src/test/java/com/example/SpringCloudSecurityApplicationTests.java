package com.example;

import com.example.dao.UserMapper;
import com.example.entity.LoginDetail;
import com.example.entity.TokenDetail;
import com.example.entity.User;
import com.example.service.LoginService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringCloudSecurityApplicationTests {
   Logger logger=LoggerFactory.getLogger(SpringCloudSecurityApplicationTests.class);
    @Test
    public void contextLoads() {
    }
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LoginService loginService;
    @Test
    public void testGetUser() throws UnsupportedEncodingException {
        //查询用户
        User user = userMapper.getUserFromDatabase("guest");
        logger.info("{}",user);
        //根据用户产生token
        LoginDetail loginDetail = loginService.getLoginDetail("guest");
        logger.info("{}",loginService.generateToken((TokenDetail) loginDetail));

        //根据用户产生token
        String secret="secret";
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("sub", "guest");
        claims.put("created", System.currentTimeMillis());
      String token=  Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis()+604800*1000))
                .signWith(SignatureAlgorithm.HS512, secret.getBytes("UTF-8"))
                .compact();
        logger.info("{}",token);
        //验证token
        Claims _claims = Jwts.parser()
                    .setSigningKey(secret.getBytes("UTF-8"))
                    .parseClaimsJws(token)
                    .getBody();
        logger.info("{}",_claims);
    }
}
