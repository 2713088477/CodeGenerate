package com.hao.backend.common;

import com.mybatisflex.core.paginate.Page;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

@Data
public class PageResult<T> {

    private List<T> records;

    private Long pageNum;

    private Long pageSize;

    private Long total;

    private Long totalPage;

    //从mybatisflex的Page对象转换为PageResult对象
    public static <T> PageResult<T> of(Page<T> page) {
        return of(page, item -> item);
    }


    public static <T, R> PageResult<R> of(Page<T> page, Function<T, R> converter) {
        PageResult<R> result = new PageResult<>();
        List<T> records = page.getRecords();
        result.setRecords(records == null ? List.of() : records.stream().map(converter).toList());
        result.setPageNum(page.getPageNumber());
        result.setPageSize(page.getPageSize());
        result.setTotal(page.getTotalRow());
        result.setTotalPage(page.getTotalPage());
        return result;
    }
}
