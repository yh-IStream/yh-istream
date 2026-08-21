package com.istream.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.istream.system.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    @Update("TRUNCATE TABLE sys_oper_log")
    void truncate();
}