package org.le.bean;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class PipeSupport implements Pipe {

    //auto inject
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected ServletContext servletContext;

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public Cookie[] getCookies() {
        return request.getCookies();
    }

    protected String getCookie(String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0){
            return "";
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName()))
                return cookie.getValue();
        }
        return "";
    }

}
