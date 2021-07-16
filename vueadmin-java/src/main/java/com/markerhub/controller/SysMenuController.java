package com.markerhub.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.markerhub.common.dto.SysMenuDto;
import com.markerhub.common.lang.Const;
import com.markerhub.common.lang.Result;
import com.markerhub.entity.SysMenu;
import com.markerhub.entity.SysRoleMenu;
import com.markerhub.entity.SysUser;
import com.markerhub.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author StarZhong
 * @since 2021-05-24
 */
@RestController
@RequestMapping("/sys/menus")
public class SysMenuController extends BaseController {

    @Autowired
    SysUserMapper sysUserMapper;

    /**
     * 获取当前用户的菜单和权限信息
     * @param principal
     * @return
     */
    @GetMapping("/nav")
    public Result nav(Principal principal) {

        String username = principal.getName();
        SysUser sysUser = sysUserService.getByUsername(username);

        // 获取权限列表
        String authorityInfo = sysUserService.getUserAuthorityInfo(sysUser.getId());
        String[] authorityInfoArray = StringUtils.tokenizeToStringArray(authorityInfo, ",");

        // 获取导航栏
        List<SysMenuDto> navs = sysMenuService.getCurrentNav();

        return Result.succ(MapUtil.builder()
                .put("authoritys", authorityInfoArray)
                .put("nav", navs)
                .map()
        );
    }

    /**
     * 查询某个菜单信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result info(@PathVariable Long id) {
        return Result.succ(sysMenuService.getById(id));
    }

    /**
     * 查询菜单列表
     * @return
     */
    @GetMapping
    @PreAuthorize("hasAuthority('sys:menu:list')")
    public Result list() {
        List<SysMenu> menus = sysMenuService.tree();
        return Result.succ(menus);
    }

    /**
     * 新建菜单信息
     * @param sysMenu
     * @return
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sys:menu:save')")
    public Result save(@Validated @RequestBody SysMenu sysMenu) {

        sysMenu.setCreated(LocalDateTime.now());
        sysMenu.setStatu(Const.STATUS_ON);

        sysMenuService.save(sysMenu);
        return Result.succ(sysMenu);
    }

    /**
     * 更新菜单信息
     * @param sysMenu
     * @return
     */
    @PutMapping
    @PreAuthorize("hasAuthority('sys:menu:update')")
    public Result update(@Validated @RequestBody SysMenu sysMenu) {

        sysMenu.setUpdated(LocalDateTime.now());

        sysMenuService.updateById(sysMenu);

        // 清除所有与该菜单相关的权限缓存
        sysUserService.clearUserAuthorityInfoByMenuId(sysMenu.getId());
        return Result.succ(sysMenu);
    }

    /**
     * 删除某个菜单
     * @param id
     * @return
     */
    @Transactional
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:menu:delete')")
    public Result delete(@PathVariable Long id) {

        int count = sysMenuService.count(new QueryWrapper<SysMenu>().eq("parent_id", id));

        if (count > 0) {
            return Result.fail("请先删除子菜单");
        }

        sysMenuService.removeById(id);
        // 同步删除中间关联表
        sysRoleMenuService.remove(new QueryWrapper<SysRoleMenu>().eq("menu_id", id));

        // 清除所有与该菜单相关的权限缓存
        sysUserService.clearUserAuthorityInfoByMenuId(id);

        return Result.succ("");
    }
}
