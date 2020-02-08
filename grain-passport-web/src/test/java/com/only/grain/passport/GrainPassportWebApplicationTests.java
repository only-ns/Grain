package com.only.grain.passport;

import com.alibaba.fastjson.JSON;
import com.only.grain.util.HttpclientUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GrainPassportWebApplicationTests {

    //App Key：1144795056
    //App Secret：64486469a9d7df38b132022beb535cf1
    //REDIRECT_URL = "http://passport.grain.com:8085/vlogin";
    @Test
    public void contextLoads() {
        //get_access_token();
        get_User_Info();
    }

    //1.获取用户code=10741d38878d6d1ff3e5242440f87e7f
    public String get_Code(){
        String url="https://api.weibo.com/oauth2/authorize?client_id=1144795056&redirect_uri=http://passport.grain.com:8085/vlogin";
        String code = HttpclientUtil.doGet(url);
        System.out.println(code);
        return code;
    }

    //2.获取授权码access_token={"access_token":"2.0021UQSG5U8TPB1a9160b173k6o73D","remind_in":"157679999","expires_in":157679999,"uid":"5766700869","isRealName":"true"}
    public String get_access_token(){
        String url="https://api.weibo.com/oauth2/access_token" +
                "?client_id=1144795056" +
                "&client_secret=64486469a9d7df38b132022beb535cf1" +
                "&grant_type=authorization_code" +
                "&code=10741d38878d6d1ff3e5242440f87e7f" +
                "&redirect_uri=http://passport.grain.com:8085/vlogin";
        String access_token = HttpclientUtil.doPost(url,new HashMap<>());
        System.out.println(access_token);

        return access_token;
    }

    //3.获取用户信息User_Info=;
    public String get_User_Info(){

        String url = "https://api.weibo.com/2/users/show.json?access_token=2.0021UQSG5U8TPB1a9160b173k6o73D&uid=5766700869";
        String user_Info=HttpclientUtil.doGet(url);
        Map info = JSON.parseObject(user_Info,HashMap.class);
        System.out.println(user_Info);
        return user_Info;
    }


}
