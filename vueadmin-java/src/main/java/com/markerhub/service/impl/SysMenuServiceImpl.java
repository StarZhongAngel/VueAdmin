package com.markerhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.markerhub.common.dto.SysMenuDto;
import com.markerhub.entity.SysMenu;
import com.markerhub.entity.SysUser;
import com.markerhub.mapper.SysMenuMapper;
import com.markerhub.mapper.SysUserMapper;
import com.markerhub.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.markerhub.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author StarZhong
 * @since 2021-05-24
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    SysUserService sysUserService;

    @Autowired
    SysUserMapper sysUserMapper;

    @Override
    public List<SysMenuDto> getCurrentNav() {

        // 获取当前用户导航栏列表
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser sysUser = sysUserService.getByUsername(username);

        List<Long> menuIds = sysUserMapper.getNavMenuIds(sysUser.getId());
        List<SysMenu> menus = this.listByIds(menuIds);

        // 转树状结构
        List<SysMenu> menuTree = buildTreeMenu(menus);

        // 实体转DTO
        return convert(menuTree);
    }

    @Override
    public List<SysMenu> tree() {

        List<SysMenu> menus = this.list(new QueryWrapper<SysMenu>().orderByAsc("orderNum"));
        return buildTreeMenu(menus);
    }

    private List<SysMenuDto> convert(List<SysMenu> menuTree) {

        List<SysMenuDto> menuDtos = new ArrayList<>();

        menuTree.forEach(
                m -> {
                    SysMenuDto dto = new SysMenuDto();

                    dto.setId(m.getId());
                    dto.setTitle(m.getName());
                    dto.setName(m.getPerms());
                    dto.setIcon(m.getIcon());
                    dto.setPath(m.getPath());
                    dto.setComponent(m.getComponent());

                    if (m.getChildren().size() > 0) {
                        // 子节点调用当前方法进行再次转换
                        dto.setChildren(convert(m.getChildren()));
                    }

                    menuDtos.add(dto);
                }
        );

        return menuDtos;
    }

    private List<SysMenu> buildTreeMenu(List<SysMenu> menus) {

        List<SysMenu> finalMenus = new ArrayList<>();

        // 先各自寻找到各自的孩子
        for (SysMenu menu : menus) {

            for (SysMenu m : menus) {

                if (menu.getId() == m.getParentId()) {
                    menu.getChildren().add(m);
                }

            }

            // 提取出父节点
            if (menu.getParentId() == 0L) {
                finalMenus.add(menu);
            }
        }

        return finalMenus;
    }
}
