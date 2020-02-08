package com.only.grain.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.only.grain.api.bean.UmsMember;
import com.only.grain.api.bean.UmsMemberResource;
import com.only.grain.api.service.UserServer;
import com.only.grain.util.HttpclientUtil;
import com.only.grain.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

@Controller
@CrossOrigin
public class PassPortController {
    @Reference
    UserServer userServer;
    final static String key = "safasvlawjbjbzxviehGRAIN";
    /**
     * 微博登录授权回调
     * @param code 授权码
     * @param state 登录来源
     * @param modelMap
     * @return
     */
    @RequestMapping("vlogin")
    public String vlogin(String code, String state, ModelMap modelMap,HttpServletRequest request){


        //获取授权码
        String url_acc="https://api.weibo.com/oauth2/access_token";
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","1144795056");
        paramMap.put("client_secret","64486469a9d7df38b132022beb535cf1");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("code",code);
        paramMap.put("redirect_uri","http://passport.grain.com:8085/vlogin");
        String access_token = HttpclientUtil.doPost(url_acc,paramMap);

        Map accessMap = JSON.parseObject(access_token,HashMap.class);
        if (accessMap==null)
            return "index";
        //获取用户信息
        String url_info = "https://api.weibo.com/2/users/show.json?access_token="+accessMap.get("access_token")+"&uid="+accessMap.get("uid");
        String user_Info=HttpclientUtil.doGet(url_info);
        Map user_map = JSON.parseObject(user_Info,HashMap.class);
        System.out.println(user_Info);

        // 将用户信息保存数据库，用户类型设置为微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType(2);
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid((String)user_map.get("idstr"));
        umsMember.setCity((String)user_map.get("location"));
        umsMember.setNickname((String)user_map.get("screen_name"));
        String g = "0";
        String gender = (String)user_map.get("gender");
        if(gender.equals("m")){
            g = "1";
        }
        umsMember.setGender(g);

        //判断用户是否存在
        UmsMember umsCheck = new UmsMember();
        umsCheck.setSourceUid(umsMember.getSourceUid());
        UmsMember umsMemberCheck = userServer.checkOauthUser(umsCheck);

        if(umsMemberCheck==null){
            //不存在注册
            umsMember = userServer.addOauthUser(umsMember);
        }else{
            //存在登录
            umsMember = umsMemberCheck;
        }


        // 生成jwt的token，并且重定向到首页，携带该token
        String token = null;
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();
        Map<String,Object> userMap = new HashMap<>();
        userMap.put("memberId",memberId);
        userMap.put("nickname",nickname);


        String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();// 从request中获取ip
            if(StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }
        }

        // 按照设计的算法对参数进行加密后，生成token
        token = JwtUtil.encode(key, userMap, ip);

        // 将token存入redis一份
        userServer.addUserToken(token,memberId);

        if (StringUtils.isBlank(state)){
            return "redirect://search.grain.com:8083/index?token="+token;
        }else {
            return "redirect://"+state+"?token="+token;
        }

    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token,String ip){
        Map<String, Object> decode = JwtUtil.decode(token, key, ip);
        Map<String, Object> content = new HashMap<>();
       if (decode!=null) {
           content.put("status", "success");
           content.put("data", decode);
       }else {
           content.put("status", "fail");
       }
        return JSON.toJSONString(content);
    }

    @RequestMapping("login")
    @ResponseBody
    public String Login(UmsMember umsMember, HttpServletRequest request){
        UmsMember loginUser = userServer.login(umsMember);
        String token = "fail";
        if (loginUser!=null){
            //登录成功!
            //使用jwt工具生成token
            String ip = request.getHeader(" X-Forwarded-For");
            if (StringUtils.isBlank(ip)){
                ip=request.getRemoteAddr();
            }
            if (StringUtils.isBlank(ip)){
                //请求不合法
            }

            Map<String,Object> map = new HashMap<>();
            map.put("nickname",loginUser.getNickname());
            map.put("memberId",loginUser.getId());

            String salt=ip;
            token = JwtUtil.encode(key, map, salt);
        }else {
            //登录失败!
            return token;
        }
        return token;
    }

    @RequestMapping({"index"})
    public String index(String originUrl, ModelMap modelMap){
        modelMap.put("originUrl",originUrl);
        return "index";
    }
}
