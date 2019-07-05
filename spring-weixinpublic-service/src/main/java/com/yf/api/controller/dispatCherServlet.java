package com.yf.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.yf.entity.TextMessage;
import com.yf.utils.CheckUtil;
import com.yf.utils.HttpClientUtil;
import com.yf.utils.XmlUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;

/**
 * @author: yaofeng
 * @create:2019-07-05-10:05
 **/
@Controller
public class dispatCherServlet {

    //(微信事件通知验签)
    @RequestMapping(value = "/dispatCherServlet", method = RequestMethod.GET)
    @ResponseBody
    public String getDispatCherServlet(String signature, String timestamp, String nonce, String echostr) {
        boolean checkSignature = CheckUtil.checkSignature(signature, timestamp, nonce);
        if (!checkSignature) {
            return null;
        }
        return echostr;
    }

    //(使用开发者模式回复内容)
    @RequestMapping(value = "/dispatCherServlet", method = RequestMethod.POST)
    @ResponseBody
    public void getDispatCherServlet(HttpServletRequest request, HttpServletResponse response,String signature, String timestamp, String nonce, String echostr) throws Exception{
        Map<String, String> result = XmlUtils.parseXml(request);
        String toUserName = result.get("ToUserName");
        String fromUserName = result.get("FromUserName");
        String msgType = result.get("MsgType");
        String content = result.get("Content");
        switch (msgType){
            case "text":
                String resultXml = null;
                PrintWriter writer = response.getWriter();
                TextMessage textMessage = new TextMessage();
                textMessage.setToUserName(fromUserName);
                textMessage.setFromUserName(toUserName);
                textMessage.setCreateTime(System.currentTimeMillis());
                textMessage.setMsgType("text");
                if (content.equals("验证码")){
                    textMessage.setContent(UUID.randomUUID().toString().replaceAll("-",""));
                }else {
                    String string = HttpClientUtil.doGet("http://api.qingyunke.com/api.php?key=free&appid=0&msg="+content);
                    JSONObject parse = new JSONObject().parseObject(string);
                    Integer state = parse.getInteger("result");
                    if (state!=null || state ==0){
                        String apiContent = parse.getString("content");
                        textMessage.setContent(apiContent);
                    }
                }
                resultXml =  XmlUtils.messageToXml(textMessage);
                writer.println(resultXml);
                writer.close();
                break;
                default:
                    break;
        }
    }


}
