package com.hao.backend.ai.model;

import lombok.Data;
import dev.langchain4j.model.output.structured.Description;

@Data 
@Description("生成多个文件代码的结果")
public class MultiFileCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("CSS代码")
    private String cssCode;

    @Description("JS代码")
    private String jsCode;

    @Description("描述")
    private String description;
}
