package com.markerhub.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author StarZhong
 * @since 2021-05-24
 */
@Data
public class SysRoleMenu {

    private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long menuId;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
}
