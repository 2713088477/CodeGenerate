package com.hao.backend.service;

import com.hao.backend.common.PageResult;
import com.hao.backend.model.dto.app.AdminAppQueryRequest;
import com.hao.backend.model.dto.app.AdminAppUpdateRequest;
import com.hao.backend.model.dto.app.AppChatRequest;
import com.hao.backend.model.dto.app.AppCreateRequest;
import com.hao.backend.model.dto.app.AppQueryRequest;
import com.hao.backend.model.dto.app.AppUpdateRequest;
import com.hao.backend.model.entity.App;
import com.hao.backend.model.vo.AdminAppVO;
import com.hao.backend.model.vo.AppVO;
import com.mybatisflex.core.service.IService;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

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

    /**
     * 用户分页查询自己的应用，可按应用名称（模糊）、部署标识（精确）筛选，默认按创建时间倒序
     * @param query 查询条件
     * @param userId 当前用户 id
     */
    PageResult<AppVO> pageMyApps(AppQueryRequest query, Long userId);

    /**
     * 管理员分页查询所有应用，可按创建者账号（模糊）、应用名称（模糊）、代码生成类型（精确）筛选，
     * 默认按优先级升序（精选在前）、创建时间倒序
     * @param query 查询条件
     */
    PageResult<AdminAppVO> pageAllApps(AdminAppQueryRequest query);

    /**
     * 用户删除自己的应用（逻辑删除）
     * @param id 应用 id
     * @param userId 当前用户 id，必须是应用的创建者
     */
    void deleteMyApp(Long id, Long userId);

    /**
     * 管理员删除任意应用（逻辑删除）
     * @param id 应用 id
     */
    void adminDeleteApp(Long id);

    /**
     * 创建者通过对话流式生成代码。应用不存在或不是当前用户创建的，直接拒绝。
     * @param request 应用 id 和用户消息
     * @param userId 当前用户 id，必须是应用的创建者
     * @return SSE 事件流：
     *         先发一个 event: ready（data 为 {"appId":"..."}），
     *         然后是若干没有 event 名的片段（data 为 {"v":"模型输出"}，用 JSON 包一层避免开头空格丢失），
     *         成功结束发 event: done，生成过程中出错发 event: error（data 为 {"message":"..."}）
     */
    Flux<ServerSentEvent<String>> chatToGenCode(AppChatRequest request, Long userId);

    /**
     * 部署应用：把代码生成目录复制到部署目录，由 Nginx 对外提供访问。
     * 首次部署生成 deployKey 写库，重新部署复用原 deployKey，并刷新部署时间。
     * @param id 应用 id
     * @param userId 当前用户 id，必须是应用的创建者
     * @return 应用的访问 URL
     */
    String deployApp(Long id, Long userId);
}
