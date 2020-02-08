package com.only.grain.util;

import io.jsonwebtoken.*;

import java.util.Map;

public class JwtUtil {
    /**
     *加密
     * @param key  服务器认证
     * @param param 用户认证(需要加密的数据)
     * @param salt  自定义认证
     * @return token 加密后的字符串
     */
    public static String encode(String key, Map<String,Object> param, String salt){
        if(salt!=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256,key);

        jwtBuilder = jwtBuilder.setClaims(param);

        String token = jwtBuilder.compact();
        return token;

    }

    /**
     * 解密
     * @param token 加密的字符串
     * @param key   服务器认证
     * @param salt  自定义认证
     * @return claims 被加密的数据
     */
    public  static Map<String,Object>  decode(String token ,String key,String salt){
        Claims claims=null;
        if (salt!=null){
            key+=salt;
        }
        try {
            claims= Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch ( JwtException e) {
           return null;
        }
        return  claims;
    }
}
