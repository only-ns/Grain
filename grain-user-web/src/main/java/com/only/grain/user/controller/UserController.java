package com.only.grain.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.only.grain.api.bean.UmsMember;

import com.only.grain.api.service.UserServer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {
    @Reference
    private UserServer userServer;
    @RequestMapping("/getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){
        List<UmsMember> allUser = userServer.getAllUser();
        return allUser;

    }
}
