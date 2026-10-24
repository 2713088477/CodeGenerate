package com.hao.backend.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQuery {

    @Min(value = 1, message = "页码从 1 开始")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数至少为 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Integer pageSize = 10;

    private String sortField;

    private Boolean asc = true;
}
