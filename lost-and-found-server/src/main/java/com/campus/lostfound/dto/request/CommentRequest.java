package com.campus.lostfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 200, message = "评论长度为1-200字")
    private String content;

    /** 父评论ID(回复时传), null表示顶级评论 */
    private Long parentId;
}
