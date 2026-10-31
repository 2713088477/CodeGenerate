package com.hao.backend.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data 
@Description("生成HTML代码的结果")
public class HtmlCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("描述")
    private String description;
}
