package com.example.tika.Util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {
    /**
     * 접속하는 사용자 IP 추출을 위한 메서드
     * @param request
     * @return
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
