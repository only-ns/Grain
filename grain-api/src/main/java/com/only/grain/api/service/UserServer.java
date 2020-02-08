package com.only.grain.api.service;

import com.only.grain.api.bean.UmsMember;
import com.only.grain.api.bean.UmsMemberReceiveAddress;
import com.only.grain.api.bean.UmsMemberResource;

import java.util.List;

/**
 * 用户服务service接口
 */
public interface UserServer {
    public List<UmsMember> getAllUser();
    public int getCounUser(UmsMember umsMember);
    public boolean AddUser(UmsMember umsMember);
    public boolean delUserByPrimaryKey(String primaryKey);

    UmsMember login(UmsMember umsMember);

    UmsMember checkOauthUser(UmsMember umsCheck);

    void addUserToken(String token, String memberId);

    UmsMember addOauthUser(UmsMember umsMember);

    UmsMember getOauthUser(UmsMember umsMemberCheck);

    List<UmsMemberReceiveAddress> getReceiveAddressByUserId(String memberId);

    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}
