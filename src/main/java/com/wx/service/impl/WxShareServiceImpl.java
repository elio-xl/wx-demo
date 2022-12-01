package com.wx.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wx.entity.vo.WxShareVO;
import com.wx.service.WxShareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 微信分享功能实现类
 *
 * @author: huang xiao lei
 * @create: 2022-11-02 10:50
 **/
@Service
public class WxShareServiceImpl implements WxShareService {

    private static final Logger log = LoggerFactory.getLogger(WxShareServiceImpl.class);

    /**
     * accessToken redis key
     */
    private static final String ACCESS_TOKEN = "accessToken";

    /**
     * jsapiTicket redis key
     */
    private static final String JSAPI_TICKET = "jsapiTicket";
    /**
     * 微信token redis key 过期时间
     */
    private static final Long TIMEOUT = Long.valueOf(1 * 60 * 60);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${wx.the-public.appId}")
    private String appId;

    @Value("${wx.the-public.appSecret}")
    private String appSecret;

    public static final String URL_SPLITTER="#";


    @Override
    public WxShareVO getSecretKey(String url) {
        if (url == null && url == "") {
            return null;
        }
        if (url.contains(URL_SPLITTER)){
            //url = StrUtil.sub(url,0,url.indexOf(URL_SPLITTER));
            url = url.substring(0,url.indexOf(URL_SPLITTER));
        }
        String accessToken = getAccessToken();
        String jsapiTicket = getTicket(accessToken);
        //随机字符串
        String noncestr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        //时间戳
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String params = "jsapi_ticket=" + jsapiTicket + "&noncestr=" + noncestr + "&timestamp=" + timestamp + "&url=" + url;
        //将字符串进行sha1加密
        String signature = getSHA1(params);
        WxShareVO wxShareVO = WxShareVO.builder()
                .appId(appId)
                .accessToken(accessToken)
                .jsapi_ticket(jsapiTicket)
                .nonceStr(noncestr)
                .timestamp(timestamp)
                .signature(signature)
                .build();
        log.info("微信分享秘钥", JSON.toJSONString(wxShareVO));
        return wxShareVO;
    }

    /**
     * 获取access_token
     * API的每天调用次数有限，默认2000次，每次请求有效期为2个小时
     *
     * @return
     */
    @Override
    public String getAccessToken() {
        String access_token = getCache(ACCESS_TOKEN);
        // 判断redis中是否有缓存
        if (!Objects.isNull(access_token) && access_token.length() > 0) {
            return access_token;
        }
        //获取access_token填写client_credential
        String grant_type = "client_credential";
        //这个url链接地址和参数皆不能变
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=" + grant_type + "&appid=" + appId + "&secret=" + appSecret;
        try {
            URL urlGet = new URL(url);
            HttpURLConnection http = (HttpURLConnection) urlGet.openConnection();
            // 必须是get方式请求
            http.setRequestMethod("GET");
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            http.setDoOutput(true);
            http.setDoInput(true);
            http.connect();
            InputStream is = http.getInputStream();
            int size = is.available();
            byte[] jsonBytes = new byte[size];
            is.read(jsonBytes);
            String message = new String(jsonBytes, "UTF-8");
            JSONObject demoJson = JSONObject.parseObject(message);
            log.info("getAccessToken", message);
            access_token = demoJson.getString("access_token");
            if (!Objects.isNull(access_token)) {
                redisTemplate.opsForHash().put(appId, ACCESS_TOKEN, access_token);
                redisTemplate.expire(appId, TIMEOUT, TimeUnit.SECONDS);
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return access_token;
    }

    /**
     * 获取hash缓存
     *
     * @param item
     * @return
     */
    private String getCache(String item) {
        HashOperations redisHash = redisTemplate.opsForHash();
        if (redisHash.hasKey(appId, item)) {
            String token = (String) redisHash.get(appId, item);
            if (Objects.isNull(token)) {
                return null;
            }
            return token;

        }
        return null;
    }


    /**
     * 获取jsapi_ticket
     * API的每天调用次数有限，默认2000次，每次请求有效期为2个小时
     *
     * @param access_token 微信token
     * @return
     */
    @Override
    public String getTicket(String access_token) {
        if (Objects.isNull(access_token)) {
            return null;
        }
        String ticket = getCache(JSAPI_TICKET);
        // 判断redis中是否有缓存
        if (!Objects.isNull(ticket) && ticket.length() > 0) {
            return ticket;
        }
        String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + access_token + "&type=jsapi";
        try {
            URL urlGet = new URL(url);
            HttpURLConnection http = (HttpURLConnection) urlGet.openConnection();
            http.setRequestMethod("GET");
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            http.setDoOutput(true);
            http.setDoInput(true);
            http.connect();
            InputStream is = http.getInputStream();
            int size = is.available();
            byte[] jsonBytes = new byte[size];
            is.read(jsonBytes);
            String message = new String(jsonBytes, "UTF-8");
            JSONObject demoJson = JSONObject.parseObject(message);
            System.out.println("getTicket：" + demoJson);
            ticket = demoJson.getString("ticket");
            if (!Objects.isNull(ticket)) {
                redisTemplate.opsForHash().put(appId, JSAPI_TICKET, ticket);
                Long expire = redisTemplate.getExpire(appId, TimeUnit.SECONDS);
                if (expire <= 0) {
                    redisTemplate.expire(access_token, TIMEOUT, TimeUnit.SECONDS);
                }
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ticket;
    }

    /**
     * SHA、SHA1加密
     *
     * @param： str：待加密字符串
     * @return： 加密串
     **/
    public static String getSHA1(String str) {
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(str.getBytes("UTF-8"));
            Formatter formatter = new Formatter();
            for (byte b : crypt.digest()) {
                formatter.format("%02x", b);
            }
            String result = formatter.toString();
            formatter.close();
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
