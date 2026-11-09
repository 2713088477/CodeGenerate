package com.hao.backend.service.impl;

import com.hao.backend.common.PageQuery;
import com.hao.backend.common.PageResult;
import com.hao.backend.common.ResponseCode;
import com.hao.backend.common.ThrowUtil;
import com.hao.backend.constant.AppPriority;
import com.hao.backend.mapper.AppMapper;
import com.hao.backend.model.dto.app.AdminAppQueryRequest;
import com.hao.backend.model.dto.app.AdminAppUpdateRequest;
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
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.util.LambdaGetter;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用表 服务层实现。
 *
 * @author <a href="https://github.com/2713088477">艺艺生辉</a>
 */
@Service
@RequiredArgsConstructor
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    private final UserService userService;

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
                    .select(User::getId)
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

    private App getExistingApp(Long id) {
        App app = getById(id);
        ThrowUtil.throwIf(app == null, ResponseCode.NOT_FOUND, "应用不存在");
        return app;
    }
}
