package com.hao.backend.service.impl;

import com.hao.backend.common.ResponseCode;
import com.hao.backend.common.ThrowUtil;
import com.hao.backend.constant.AppPriority;
import com.hao.backend.mapper.AppMapper;
import com.hao.backend.model.dto.app.AdminAppUpdateRequest;
import com.hao.backend.model.dto.app.AppCreateRequest;
import com.hao.backend.model.dto.app.AppUpdateRequest;
import com.hao.backend.model.entity.App;
import com.hao.backend.model.enums.CodeGenTypeEnum;
import com.hao.backend.service.AppService;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 应用表 服务层实现。
 *
 * @author <a href="https://github.com/2713088477">艺艺生辉</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

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

    private App getExistingApp(Long id) {
        App app = getById(id);
        ThrowUtil.throwIf(app == null, ResponseCode.NOT_FOUND, "应用不存在");
        return app;
    }
}
