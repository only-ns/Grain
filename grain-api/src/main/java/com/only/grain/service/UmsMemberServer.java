package com.only.grain.service;

import com.only.grain.bean.UmsMember;

import java.util.List;

public interface UmsMemberServer {
    public List<UmsMember> getAllUser();
    public int getCounUser(UmsMember umsMember);
    public boolean AddUser(UmsMember umsMember);
    public boolean delUserByPrimaryKey(String primaryKey);

}
