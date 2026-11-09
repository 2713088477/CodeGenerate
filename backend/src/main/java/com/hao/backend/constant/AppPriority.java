package com.hao.backend.constant;

/**
 * 应用优先级约定：数值越小越靠前。
 * <ul>
 *   <li>普通应用：默认 {@link #DEFAULT}</li>
 *   <li>精选应用：优先级小于等于 {@link #FEATURED_MAX}，精选应用之间按数值升序排序</li>
 * </ul>
 * 这样「精选在前、普通在后」只需要一个 ORDER BY priority ASC，精选范围的判断统一用 {@link #isFeatured(Integer)}。
 */
public final class AppPriority {

    /** 普通应用的默认优先级 */
    public static final int DEFAULT = 99;

    /** 精选应用的默认优先级 */
    public static final int FEATURED_DEFAULT = 10;

    /** 精选分界线：优先级 <= 该值即为精选应用 */
    public static final int FEATURED_MAX = 10;

    /** 允许设置的最小值 */
    public static final int MIN = 1;

    /** 允许设置的最大值 */
    public static final int MAX = 99;

    private AppPriority() {
    }

    public static boolean isFeatured(Integer priority) {
        return priority != null && priority <= FEATURED_MAX;
    }
}
