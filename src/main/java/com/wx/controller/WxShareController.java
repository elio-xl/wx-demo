package com.wx.controller;

import com.wx.entity.vo.WxShareVO;
import com.wx.service.WxShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 微信公众号分享
 * @author: huang xiao lei
 * @create: 2022-11-02 10:42
 **/
@RestController
@RequestMapping("/wxShare")
@RequiredArgsConstructor
public class WxShareController {

    private final RedisTemplate<String, Object> redisTemplate;

    private final WxShareService wxShareService;

    /**
     * 获取分享秘钥
     * @author huang xiao lei
     * @create 2022/11/2 10:43
     * @param  urls 分享的连接
     * @return
     */
    @RequestMapping(value = "/getSecretKey", method = RequestMethod.POST)
    public WxShareVO getSecretKey(@RequestParam(name = "urls") String urls){
        return wxShareService.getSecretKey(urls);
    }

    /**
     * 描述
     * @author huang xiao lei
     * @create 2022/11/21 11:44
     * @param
     * @return
     */
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String method(){
        return "test";
    }
}
