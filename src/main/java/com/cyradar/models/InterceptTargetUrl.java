/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyradar.models;

/**
 *
 * @author phinc27
 */
public class InterceptTargetUrl {

    public static final int MESSAGE_TYPE_REQUEST = 0;
    public static final int MESSAGE_TYPE_RESPONSE = 1;
    public static final int MESSAGE_TYPE_BOTH = 2;
    public static final int URL_TYPE_ALL = 0;
    public static final int URL_TYPE_INSCOPE = 1;
    public static final int URL_TYPE_CUSTOM = 2;

    private int messageType;
    private int urlType;
    private String url;
    private boolean usingRegex;

    public static InterceptTargetUrl getDefault() {
        InterceptTargetUrl ret = new InterceptTargetUrl();
        ret.messageType = MESSAGE_TYPE_REQUEST;
        ret.urlType = URL_TYPE_ALL;
        return ret;
    }

    public InterceptTargetUrl() {
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getUrlType() {
        return urlType;
    }

    public void setUrlType(int urlType) {
        this.urlType = urlType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isUsingRegex() {
        return usingRegex;
    }

    public void setUsingRegex(boolean usingRegex) {
        this.usingRegex = usingRegex;
    }

}
