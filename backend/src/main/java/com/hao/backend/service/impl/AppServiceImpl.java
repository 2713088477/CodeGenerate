package com.hao.backend.service.impl;

import com.hao.backend.common.BusinessException;
import com.hao.backend.core.AICodeGenFacade;
import com.hao.backend.core.CodeDeployer;
import com.hao.backend.core.CodeFileSaver;
import com.hao.backend.common.PageQuery;
import com.hao.backend.common.PageResult;
import com.hao.backend.common.ResponseCode;
import com.hao.backend.common.ThrowUtil;
import com.hao.backend.constant.AppPriority;
import com.hao.backend.mapper.AppMapper;
import com.hao.backend.model.dto.app.AdminAppQueryRequest;
import com.hao.backend.model.dto.app.AdminAppUpdateRequest;
import com.hao.backend.model.dto.app.AppChatRequest;
import com.hao.backend.model.dto.app.AppCreateRequest;
import com.hao.backend.model.dto.app.AppQueryRequest;
import com.hao.backend.model.dto.app.AppUpdateRequest;
import com.hao.backend.model.entity.App;
import com.hao.backend.model.entity.User;
import com.hao.backend.model.enums.CodeGenTypeEnum;
import com.hao.backend.model.vo.AdminAppVO;
import com.hao.backend.model.vo.AppVO;
import com.hao.backend.service.AppService;
import com.hao.backend.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.util.LambdaGetter;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用表 服务层实现。
 *
 * @author <a href="https://github.com/2713088477">艺艺生辉</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private final UserService userService;
    private final AICodeGenFacade aiCodeGenFacade;
    private final JsonMapper jsonMapper;

    /** 部署后的访问地址前缀，对应 Nginx 的 /deploy/ 路径，以 / 结尾 */
    @Value("${app.deploy-url-prefix:http://localhost/deploy/}")
    private String deployUrlPrefix;

    private static final int DEPLOY_KEY_LENGTH = 6;
    private static final String DEPLOY_KEY_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEPLOY_KEY_MAX_RETRY = 10;

    private static final Map<String, LambdaGetter<App>> SORT_FIELDS = Map.of(
            "id", App::getId,
            "appName", App::getAppName,
            "priority", App::getPriority,
            "createdAt", App::getCreatedAt,
            "updatedAt", App::getUpdatedAt
    );

    /**
     * 应用名称默认取 initPrompt 的前 N 个字符(未来可能会优化)
     */
    private static final int DEFAULT_APP_NAME_LENGTH = 12;

    @Override
    public Long createApp(AppCreateRequest request, Long userId) {
        ThrowUtil.throwIf(userId == null, ResponseCode.UNAUTHORIZED);
        String initPrompt = StringUtils.trimToNull(request.getInitPrompt());
        ThrowUtil.throwIf(initPrompt == null, ResponseCode.PARAMS_ERROR, "初始化提示词不能为空");

        App app = new App();
        app.setInitPrompt(initPrompt);
        app.setAppName(StringUtils.left(initPrompt, DEFAULT_APP_NAME_LENGTH));
        //todo: 这里未来会做一个只能选择，这里先写死
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        app.setUserId(userId);
        app.setPriority(AppPriority.DEFAULT);
        boolean saved = save(app);
        ThrowUtil.throwIf(!saved, ResponseCode.INTERNAL_SERVER_ERROR, "创建应用失败");
        return app.getId();
    }

    @Override
    public void updateMyApp(Long id, AppUpdateRequest request, Long userId) {
        App app = getExistingApp(id);
        // 只能改自己的应用
        ThrowUtil.throwIf(!app.getUserId().equals(userId), ResponseCode.FORBIDDEN, "无权修改该应用");
        String appName = StringUtils.trimToNull(request.getAppName());
        ThrowUtil.throwIf(appName == null, ResponseCode.PARAMS_ERROR, "应用名称不能为空");

        UpdateChain.of(App.class)
                .set(App::getAppName, appName)
                .where(App::getId).eq(id)
                .update();
    }

    @Override
    public void adminUpdateApp(Long id, AdminAppUpdateRequest request) {
        getExistingApp(id);
        boolean changeName = request.getAppName() != null;
        boolean changePriority = request.getPriority() != null;
        ThrowUtil.throwIf(!changeName && !changePriority, ResponseCode.PARAMS_ERROR, "至少需要修改一项");
        String appName = StringUtils.trimToNull(request.getAppName());
        ThrowUtil.throwIf(changeName && appName == null, ResponseCode.PARAMS_ERROR, "应用名称不能为空");

        UpdateChain.of(App.class)
                .set(App::getAppName, appName, changeName)
                .set(App::getPriority, request.getPriority(), changePriority)
                .where(App::getId).eq(id)
                .update();
    }

    @Override
    public PageResult<AppVO> pageMyApps(AppQueryRequest query, Long userId) {
        ThrowUtil.throwIf(userId == null, ResponseCode.UNAUTHORIZED);
        String appName = StringUtils.trimToNull(query.getAppName());
        String deployKey = StringUtils.trimToNull(query.getDeployKey());

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(App::getUserId, userId)
                .like(App::getAppName, appName, appName != null)
                .eq(App::getDeployKey, deployKey, deployKey != null);
        applySort(queryWrapper, query, false);

        Page<App> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), queryWrapper);
        return PageResult.of(page, AppVO::from);
    }

    @Override
    public PageResult<AdminAppVO> pageAllApps(AdminAppQueryRequest query) {
        String userAccount = StringUtils.trimToNull(query.getUserAccount());
        String appName = StringUtils.trimToNull(query.getAppName());
        String codeGenType = StringUtils.trimToNull(query.getCodeGenType());
        ThrowUtil.throwIf(codeGenType != null && CodeGenTypeEnum.fromValue(codeGenType) == null,
                ResponseCode.PARAMS_ERROR, "代码生成类型不正确");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .like(App::getAppName, appName, appName != null)
                .eq(App::getCodeGenType, codeGenType, codeGenType != null);
        if (userAccount != null) {
            // 按创建者账号模糊查：子查询找出匹配的用户 id，再按 userId 过滤
            queryWrapper.in(App::getUserId, QueryWrapper.create()
                    .select(QueryMethods.column(User::getId))
                    .from(User.class)
                    .like(User::getUserAccount, userAccount));
        }
        applySort(queryWrapper, query, true);

        Page<App> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), queryWrapper);

        // 一次性查出本页所有创建者的账号，避免逐条查询
        Set<Long> userIds = page.getRecords().stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, String> accountMap = userIds.isEmpty()
                ? Map.of()
                : userService.listByIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getUserAccount));
        return PageResult.of(page, app -> AdminAppVO.from(app, accountMap.get(app.getUserId())));
    }

    @Override
    public void deleteMyApp(Long id, Long userId) {
        App app = getExistingApp(id);
        // 只能删自己的应用
        ThrowUtil.throwIf(!app.getUserId().equals(userId), ResponseCode.FORBIDDEN, "无权删除该应用");
        boolean removed = removeById(id);
        ThrowUtil.throwIf(!removed, ResponseCode.INTERNAL_SERVER_ERROR, "删除应用失败");
    }

    @Override
    public void adminDeleteApp(Long id) {
        getExistingApp(id);
        boolean removed = removeById(id);
        ThrowUtil.throwIf(!removed, ResponseCode.INTERNAL_SERVER_ERROR, "删除应用失败");
    }

    /**
     * 排序：传了 sortField 就按它排；没传则用默认排序。
     * 默认排序：priorityFirst 为 true 时先按优先级升序（精选在前），再按创建时间倒序。
     * 最后追加 id 倒序，保证排序值相同时分页结果稳定。
     */
    private void applySort(QueryWrapper queryWrapper, PageQuery query, boolean priorityFirst) {
        String sortField = StringUtils.trimToNull(query.getSortField());
        if (sortField == null) {
            if (priorityFirst) {
                queryWrapper.orderBy(App::getPriority, true);
            }
            queryWrapper.orderBy(App::getCreatedAt, false);
        } else {
            LambdaGetter<App> column = SORT_FIELDS.get(sortField);
            ThrowUtil.throwIf(column == null, ResponseCode.PARAMS_ERROR, "排序字段不正确");
            queryWrapper.orderBy(column, Boolean.TRUE.equals(query.getAsc()));
        }
        queryWrapper.orderBy(App::getId, false);
    }

    @Override
    public Flux<ServerSentEvent<String>> chatToGenCode(AppChatRequest request, Long userId) {
        ThrowUtil.throwIf(userId == null, ResponseCode.UNAUTHORIZED);
        App app = getExistingApp(request.getAppId());
        ThrowUtil.throwIf(!app.getUserId().equals(userId), ResponseCode.FORBIDDEN, "无权访问该应用");
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.fromValue(app.getCodeGenType());
        ThrowUtil.throwIf(codeGenType == null, ResponseCode.SYSTEM_ERROR, "代码生成类型不正确");
        String message = StringUtils.trimToNull(request.getMessage());
        ThrowUtil.throwIf(message == null, ResponseCode.PARAMS_ERROR, "消息不能为空");
        Long appId = app.getId();

        // 开始事件：前端收到后表示请求已通过校验、开始生成
        Flux<ServerSentEvent<String>> ready = Flux.just(
                sseEvent("ready", Map.of("appId", String.valueOf(appId))));

        // 代码片段：包成 {"v":"..."} 再交给 SSE。浏览器会删掉 data 字段值开头的一个空格，模型片段经常以空格开头。
        // defer 保证订阅时才调用模型，ready 不会被前置的同步工作拖慢。
        Flux<ServerSentEvent<String>> chunks = Flux.defer(
                        () -> aiCodeGenFacade.generateAndSaveCodeStream(message, codeGenType, appId))
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(jsonMapper.writeValueAsString(new SseChunk(chunk)))
                        .build());

        // 结束事件：只有流正常完成才会发，前端据此确认生成并保存成功
        Flux<ServerSentEvent<String>> done = Flux.just(sseEvent("done", Map.of()));

        return ready.concatWith(chunks).concatWith(done)
                .onErrorResume(e -> {
                    log.error("流式生成代码失败, appId={}", appId, e);
                    String msg = e instanceof BusinessException ? e.getMessage() : "代码生成失败，请稍后重试";
                    return Flux.just(sseEvent("error", Map.of("message", msg)));
                });
    }

    /**
     * 构造带事件名的 SSE 事件，data 为 JSON。
     * 必须带 data：前端生成的 SSE 客户端不会把没有 data 的事件交给 stream。
     */
    private ServerSentEvent<String> sseEvent(String event, Object data) {
        return ServerSentEvent.<String>builder()
                .event(event)
                .data(jsonMapper.writeValueAsString(data))
                .build();
    }

    private record SseChunk(String v) {
    }

    @Override
    public String deployApp(Long id, Long userId) {
        ThrowUtil.throwIf(userId == null, ResponseCode.UNAUTHORIZED);
        App app = getExistingApp(id);
        ThrowUtil.throwIf(!app.getUserId().equals(userId), ResponseCode.FORBIDDEN, "无权部署该应用");
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.fromValue(app.getCodeGenType());
        ThrowUtil.throwIf(codeGenType == null, ResponseCode.SYSTEM_ERROR, "代码生成类型不正确");
        Path sourceDir = CodeFileSaver.getCodeDir(codeGenType, id);
        ThrowUtil.throwIf(!Files.isDirectory(sourceDir), ResponseCode.PARAMS_ERROR, "应用还没有生成代码，请先生成代码");

        // 已部署过就复用原来的 key，重新部署时访问链接不变
        String deployKey = app.getDeployKey();
        boolean newKey = deployKey == null;
        if (newKey) {
            deployKey = generateDeployKey();
        }
        CodeDeployer.deploy(sourceDir, deployKey);

        UpdateChain.of(App.class)
                .set(App::getDeployKey, deployKey, newKey)
                .set(App::getDeployedTime, LocalDateTime.now())
                .where(App::getId).eq(id)
                .update();
        return deployUrlPrefix + deployKey + "/";
    }

    /**
     * 生成唯一的部署标识：6 位小写字母加数字，和库里已有的重复就重新生成。
     */
    private String generateDeployKey() {
        for (int i = 0; i < DEPLOY_KEY_MAX_RETRY; i++) {
            String key = RandomStringUtils.secure().next(DEPLOY_KEY_LENGTH, DEPLOY_KEY_CHARS);
            boolean exists = queryChain().where(App::getDeployKey).eq(key).exists();
            if (!exists) {
                return key;
            }
        }
        throw new BusinessException(ResponseCode.INTERNAL_SERVER_ERROR, "生成部署标识失败，请重试");
    }

    private App getExistingApp(Long id) {
        App app = getById(id);
        ThrowUtil.throwIf(app == null, ResponseCode.NOT_FOUND, "应用不存在");
        return app;
    }
}
