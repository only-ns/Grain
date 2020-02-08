package com.only.grain.user.servier.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.only.grain.api.bean.UmsMember;
import com.only.grain.api.bean.UmsMemberReceiveAddress;
import com.only.grain.api.service.UserServer;
import com.only.grain.user.mapper.UmsMemberMapper;
import com.only.grain.user.mapper.UmsMemberReceiveAddressMapper;
import com.only.grain.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServerImpl implements UserServer {
    @Autowired
    private UmsMemberMapper umsMemberMapper;
    @Autowired
    private UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 查询所有
     * @return
     */
    public List<UmsMember> getAllUser(){
        List<UmsMember> list = umsMemberMapper.selectAll();
        return list;
    }



    /**
     * 根据条件查询总数
     * @return
     */
    public int getCounUser(UmsMember umsMember){
        return umsMemberMapper.selectCount(umsMember);
    }

    /**
     * 添加用户
     * @param umsMember
     * @return
     */

    public boolean AddUser(UmsMember umsMember){
        return umsMemberMapper.insert(umsMember)>0;
    }

    /**
     * 根据主键删除
     * @param primaryKey
     * @return
     */
    public boolean delUserByPrimaryKey(String primaryKey){
        return umsMemberMapper.deleteByPrimaryKey(primaryKey)>0;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis=redisUtil.getJedis();
        try {
            String key = "user:" + umsMember.getUsername()+umsMember.getPassword()+":info";
            if (jedis!=null) {
                jedis = redisUtil.getJedis();
                String userJSON = jedis.get(key);
                if (StringUtils.isNotBlank(userJSON)) {
                    //返回缓存中存在的用户信息
                    UmsMember loginUser = JSON.parseObject(userJSON, UmsMember.class);
                    return loginUser;
                }
            }
            //连接redis失败或缓存中没有或者密码错误访问DB
            UmsMember loginUser = loginFromDb(umsMember);
            if (loginUser!=null){
                jedis.setex(key,60*60*24*1,JSON.toJSONString(loginUser));
            }
            return loginUser;

        }finally {
            if (jedis!=null)
                jedis.close();
        }
    }


    private UmsMember loginFromDb(UmsMember umsMember){
        List<UmsMember> memberList = umsMemberMapper.select(umsMember);
        if (memberList!=null&&memberList.size()>0){
            return memberList.get(0);
        }
        return null;
    }

    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();

        jedis.setex("user:"+memberId+":token",60*60*2,token);

        jedis.close();
    }

    @Override
    public UmsMember addOauthUser(UmsMember umsMember) {
        umsMemberMapper.insertSelective(umsMember);
        return umsMember;
    }

    @Override
    public UmsMember checkOauthUser(UmsMember umsCheck) {
        UmsMember umsMember = umsMemberMapper.selectOne(umsCheck);
        return umsMember;
    }

    @Override
    public UmsMember getOauthUser(UmsMember umsMemberCheck) {


        UmsMember umsMember = umsMemberMapper.selectOne(umsMemberCheck);
        return umsMember;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByUserId(String memberId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> receiveAddressList = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return receiveAddressList;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = umsMemberReceiveAddressMapper.selectByPrimaryKey(receiveAddressId);

        return umsMemberReceiveAddress;
    }

}
