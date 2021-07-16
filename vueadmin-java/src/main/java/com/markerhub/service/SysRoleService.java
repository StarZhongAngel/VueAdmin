package com.markerhub.service;

import com.markerhub.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author StarZhong
 * @since 2021-05-24
 */
public interface SysRoleService extends IService<SysRole> {

    List<SysRole> listByUserId(Long userId);
}
