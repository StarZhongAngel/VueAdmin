package com.markerhub.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author StarZhong
 * @since 2021-05-24
 */
@Data
public class SysUserRole {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long roleId;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
}
