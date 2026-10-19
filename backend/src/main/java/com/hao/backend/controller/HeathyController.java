package com.hao.backend.controller;

import com.hao.backend.common.BaseResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/heathy")
public class HeathyController {

    public BaseResponse<String> ping(){
        return BaseResponse.success();
    }
}
