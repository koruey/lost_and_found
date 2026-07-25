package com.campus.lostfound.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditRequest {

    @NotNull(message = "审核结果不能为空")
    private Integer status;  // 1-通过, 2-不通过

    private String reason;
}
