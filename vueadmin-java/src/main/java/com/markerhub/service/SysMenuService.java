package com.markerhub.service;

import com.markerhub.common.dto.SysMenuDto;
import com.markerhub.entity.SysMenu;
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
public interface SysMenuService extends IService<SysMenu> {

    List<SysMenuDto> getCurrentNav();

    List<SysMenu> tree();
}
