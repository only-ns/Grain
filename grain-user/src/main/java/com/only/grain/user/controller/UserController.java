package com.only.grain.user.controller;

import com.only.grain.bean.UmsMember;
import com.only.grain.service.UmsMemberServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    private UmsMemberServer umsMemberServer;
    @RequestMapping("/getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){
        List<UmsMember> allUser = umsMemberServer.getAllUser();
        return allUser;

    }
}
