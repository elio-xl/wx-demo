package com.wx.service;

import com.wx.entity.vo.WxShareVO;

/**
 * 微信分享功能接口
 * @author: huang xiao lei
 * @create: 2022-11-02 10:50
 **/
public interface WxShareService {

    /**
     * 微信分享
     * @param urls  分享链接 注意：链接不能带有 /#
     * @return
     */
    WxShareVO getSecretKey(String urls);

    /**
     * 获取access_token
     * API的每天调用次数有限，默认2000次，每次请求有效期为2个小时
     *
     * @return
     */
    String getAccessToken();

    /**
     * 获取jsapi_ticket
     * API的每天调用次数有限，默认2000次，每次请求有效期为2个小时
     *
     * @param access_token 微信token
     * @return
     */
    String getTicket(String access_token);
}
