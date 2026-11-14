package com.hao.backend.constant;

/**
 * 应用相关的路径约定。
 * <ul>
 *   <li>代码生成目录：{@link #CODE_OUTPUT_ROOT_DIR}/{type.value}_{appId}</li>
 *   <li>部署目录：{@link #CODE_DEPLOY_ROOT_DIR}/{deployKey}，由 Nginx 静态托管</li>
 * </ul>
 */
public final class AppConstant {

    /** 代码生成的根目录 */
    public static final String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /** 代码部署的根目录，Nginx 的 alias 要指向这里 */
    public static final String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/deploy";

    private AppConstant() {
    }
}
