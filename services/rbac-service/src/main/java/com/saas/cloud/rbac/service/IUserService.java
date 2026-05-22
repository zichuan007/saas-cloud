package com.saas.cloud.rbac.service;

import com.saas.cloud.common.core.result.PageResult;
import com.saas.cloud.rbac.api.dto.UserCreateDTO;
import com.saas.cloud.rbac.api.dto.UserUpdateDTO;
import com.saas.cloud.rbac.api.vo.UserInfoVO;
import com.saas.cloud.rbac.api.vo.UserPageVO;
import com.saas.cloud.rbac.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户表 服务类
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-18
 */
public interface IUserService extends IService<User> {

    /**
     * 分页查询用户，支持按用户名/真实姓名模糊搜索
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键字（用户名/真实姓名）
     * @return 用户分页结果
     */
    PageResult<UserPageVO> pageUsers(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 获取用户详情，包含部门名称和角色信息
     *
     * @param userId 用户ID
     * @return 用户详情VO
     */
    UserInfoVO getUserDetail(Long userId);

    /**
     * 创建用户，BCrypt加密密码，创建UserRole关联
     *
     * @param dto 用户创建请求
     */
    void createUser(UserCreateDTO dto);

    /**
     * 更新用户信息，先删后增UserRole
     *
     * @param dto 用户更新请求
     */
    void updateUser(UserUpdateDTO dto);

    /**
     * 逻辑删除用户（设delete_flag=1）
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 启用/禁用用户
     *
     * @param userId 用户ID
     * @param status 状态 0-禁用 1-启用
     */
    void updateStatus(Long userId, Byte status);

    /**
     * 重置密码
     *
     * @param userId      用户ID
     * @param newPassword 新密码（明文，方法内加密）
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 更新当前用户个人资料
     *
     * @param userId   用户ID
     * @param realName 真实姓名
     * @param phone    手机号
     */
    void updateProfile(Long userId, String realName, String phone);

    /**
     * 修改当前用户密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 查询用户列表（导出用）
     *
     * @param keyword 搜索关键字（可选）
     * @return 用户列表
     */
    List<User> listForExport(String keyword);
}
