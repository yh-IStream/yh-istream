package com.istream.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.istream.system.entity.SysDept;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {

    List<SysDept> listDeptTree();

    boolean hasChildren(Long deptId);
}