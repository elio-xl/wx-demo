package com.wx.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信分享返回参数
 * @author: huang xiao lei
 * @create: 2022-11-02 10:53
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxShareVO {

    /**
     * 微信公众号appId
     */
    private String appId;

    private String accessToken;

    private String jsapi_ticket;

    private String timestamp;

    private String nonceStr;

    private String signature;

}
