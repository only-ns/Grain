package com.only.grain.interceptors;

import com.alibaba.fastjson.JSON;
import com.only.grain.annotations.LoginRequired;
import com.only.grain.util.CookieUtil;
import com.only.grain.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 拦截代码
        // 判断被拦截的请求的访问的方法的注解(是否时需要拦截的)
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        String ip = request.getHeader(" X-Forwarded-For");
        if (StringUtils.isBlank(ip)){
            ip=request.getRemoteAddr();
        }
        // 是否拦截
        if (methodAnnotation == null) {
            return true;
        }

        String token = "";
        //获取有效token
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }

        String newToken = request.getParameter("token");
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }

        // 是否必须登录
        boolean loginSuccess = methodAnnotation.loginSuccess();// 获得该请求是否必需登录成功
        Map<String,Object> member= null;
        // 调用认证中心进行验证
        Map<String,Object> anthe =null;
        if(StringUtils.isNotBlank(token)){
            String success  = HttpclientUtil.doGet("http://passport.grain.com:8085/verify?token=" + token+"&ip="+ip);
            anthe = JSON.parseObject(success,Map.class);
            member = (Map<String, Object>) anthe.get("data");
        }

        if (loginSuccess) {
            // 必须登录成功才能使用
            if (anthe==null||!"success".equals(anthe.get("status"))) {
                //认证失败 重定向passport登录
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://passport.grain.com:8085/index?originUrl="+requestURL);
                return false;
            }

            //认证成功 需要将token携带的用户信息写入
            request.setAttribute("memberId", member.get("memberId"));
            request.setAttribute("nickname", member.get("nickname"));
            //验证通过，覆盖cookie中的token
            if(StringUtils.isNotBlank(token)){
                CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
            }
        }
        //没有登录也能用，但是必须验证
        if (anthe!=null&&"success".equals(anthe.get("status"))) {
            // 需要将token携带的用户信息写入
            request.setAttribute("memberId", member.get("memberId"));
            request.setAttribute("nickname", member.get("nickname"));
            //验证通过，覆盖cookie中的token
            if(StringUtils.isNotBlank(token)){
                CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
            }
        }
        return true;
    }
}
