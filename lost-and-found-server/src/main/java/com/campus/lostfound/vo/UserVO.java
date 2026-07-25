package com.campus.lostfound.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserVO {

    private Long id;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private Integer role;
}
