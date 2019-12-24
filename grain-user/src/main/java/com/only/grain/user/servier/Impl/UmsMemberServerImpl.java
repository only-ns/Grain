package com.only.grain.user.servier.Impl;

import com.only.grain.bean.UmsMember;
import com.only.grain.service.UmsMemberServer;
import com.only.grain.user.mapper.UmsMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UmsMemberServerImpl implements UmsMemberServer {
    @Autowired
    private UmsMemberMapper umsMemberMapper;

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
}
