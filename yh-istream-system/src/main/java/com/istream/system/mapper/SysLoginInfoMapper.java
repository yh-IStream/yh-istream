package com.istream.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.istream.system.entity.SysLoginInfo;
import org.apache.ibatis.annotations.Update;

public interface SysLoginInfoMapper extends BaseMapper<SysLoginInfo> {

    @Update("TRUNCATE TABLE sys_login_info")
    void truncate();
}