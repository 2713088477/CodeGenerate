package com.hao.backend.service;

import com.hao.backend.model.dto.app.AdminAppUpdateRequest;
import com.hao.backend.model.dto.app.AppCreateRequest;
import com.hao.backend.model.dto.app.AppUpdateRequest;
import com.hao.backend.model.entity.App;
import com.mybatisflex.core.service.IService;

/**
 * 应用表 服务层。
 *
 * @author <a href="https://github.com/2713088477">艺艺生辉</a>
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用
     * @param request 创建请求，initPrompt 必填
     * @param userId 创建用户 id
     * @return 新应用的 id
     */
    Long createApp(AppCreateRequest request, Long userId);

    /**
     * 用户修改自己的应用（只能改名称）
     * @param id 应用 id
     * @param request 修改请求
     * @param userId 当前用户 id，必须是应用的创建者
     */
    void updateMyApp(Long id, AppUpdateRequest request, Long userId);

    /**
     * 管理员修改应用（可改名称、优先级）
     * @param id 应用 id
     * @param request 修改请求，至少传一项
     */
    void adminUpdateApp(Long id, AdminAppUpdateRequest request);
}
