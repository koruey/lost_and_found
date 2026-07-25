package com.campus.lostfound.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {

    private Long id;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String content;
    private LocalDateTime createdAt;

    /** 父评论ID */
    private Long parentId;

    /** 被回复用户昵称 */
    private String replyToNickname;

    /** 子回复列表 */
    private List<CommentVO> replies;
}
