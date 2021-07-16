package com.markerhub.controller;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markerhub.common.dto.PassDto;
import com.markerhub.common.lang.Const;
import com.markerhub.common.lang.Result;
import com.markerhub.entity.SysRole;
import com.markerhub.entity.SysUser;
import com.markerhub.entity.SysUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
@RequestMapping("/sys/users")
public class SysUserController extends BaseController {

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result info(@PathVariable("id") Long id) {

        SysUser sysUser = sysUserService.getById(id);
        Assert.notNull(sysUser, "找不到该管理员");

        // 获取用户相关联的角色列表
        List<SysRole> roleList = sysRoleService.listByUserId(id);
        sysUser.setRoles(roleList);

        return Result.succ(sysUser);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result list(String username) {

        Page<SysUser> pageData = sysUserService.page(getPage(), new QueryWrapper<SysUser>()
                        .like(StrUtil.isNotBlank(username), "username", username)
        );

        pageData.getRecords().forEach(u -> u.setRoles(sysRoleService.listByUserId(u.getId())));

        return Result.succ(pageData);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:save')")
    public Result save(@Validated @RequestBody SysUser sysUser) {

        sysUser.setCreated(LocalDateTime.now());
        sysUser.setStatu(Const.STATUS_ON);
        // 设置初始密码
        sysUser.setPassword(bCryptPasswordEncoder.encode(Const.DEFAULT_PASSWORD));
        // 设置初始头像
        sysUser.setAvatar(Const.DEFAULT_AVATAR);

        sysUserService.save(sysUser);

        return Result.succ(sysUser);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('sys:user:update')")
    public Result update(@Validated @RequestBody SysUser sysUser) {

        sysUser.setUpdated(LocalDateTime.now());

        sysUserService.updateById(sysUser);

        return Result.succ(sysUser);
    }


    @Transactional
    @DeleteMapping
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public Result delete(@RequestBody Long[] ids) {

        sysUserService.removeByIds(Arrays.asList(ids));

        // 删除中间表
        sysUserRoleService.remove(new QueryWrapper<SysUserRole>().in("user_id", ids));

        return Result.succ("");
    }

    @Transactional
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('sys:user:role')")
    public Result rolePerm(@PathVariable("userId") Long userId, @RequestBody Long[] roleIds) {

        List<SysUserRole> userRoles = new ArrayList<>();

        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();

            userRole.setUserId(userId);
            userRole.setRoleId(roleId);

            userRoles.add(userRole);
        }

        sysUserRoleService.remove(new QueryWrapper<SysUserRole>().eq("user_id", userId));
        sysUserRoleService.saveBatch(userRoles);

        // 删除缓存
        sysUserService.clearUserAuthorityInfo(userId);

        return Result.succ("");
    }

    /**
     * 重置用户密码
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:repass')")
    public Result repass(@PathVariable("id") Long id) {

        SysUser sysUser = sysUserService.getById(id);

        sysUser.setPassword(bCryptPasswordEncoder.encode(Const.DEFAULT_PASSWORD));
        sysUser.setUpdated(LocalDateTime.now());

        sysUserService.updateById(sysUser);

        return Result.succ("");
    }

    @PutMapping("/updatePass")
    public Result updatePass(@Validated @RequestBody PassDto passDto, Principal principal) {

        SysUser sysUser = sysUserService.getByUsername(principal.getName());

        boolean matches = bCryptPasswordEncoder.matches(passDto.getCurrentPass(), sysUser.getPassword());

        if (!matches) {
            return Result.fail("旧密码不正确");
        }

        sysUser.setPassword(bCryptPasswordEncoder.encode(passDto.getPassword()));
        sysUser.setUpdated(LocalDateTime.now());

        sysUserService.updateById(sysUser);

        return Result.succ("");
    }
}
