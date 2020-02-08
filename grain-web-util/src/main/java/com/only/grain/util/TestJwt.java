package com.only.grain.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestJwt {

    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("memberId","1");
        map.put("nickname","zhangsan");
        String ip = "127.0.0.1";
        String key = "faknvaklvjoiehiog";
        String encode = JwtUtil.encode(key, map, ip);
        Map<String, Object> decode = JwtUtil.decode(encode, key, ip);
    }
}
