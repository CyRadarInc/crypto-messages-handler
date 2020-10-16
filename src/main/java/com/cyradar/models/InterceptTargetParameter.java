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
public class InterceptTargetParameter {

    private String paramName;
    private boolean usingRegex;
    private String matchPosition;
    private int matchGroup;
    private boolean urlDecodeBefore;
    private boolean urlEncodeAfter;

    public static InterceptTargetParameter getDefault() {
        InterceptTargetParameter ret = new InterceptTargetParameter();
        ret.paramName = "";
        ret.matchGroup = -1;
        ret.matchPosition = "";
        ret.usingRegex = false;
        ret.urlDecodeBefore = true;
        ret.urlEncodeAfter = true;
        return ret;
    }

    public InterceptTargetParameter() {
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public boolean isUsingRegex() {
        return usingRegex;
    }

    public void setUsingRegex(boolean usingRegex) {
        this.usingRegex = usingRegex;
    }

    public String getMatchPosition() {
        return matchPosition;
    }

    public void setMatchPosition(String matchPosition) {
        this.matchPosition = matchPosition;
    }

    public int getMatchGroup() {
        return matchGroup;
    }

    public void setMatchGroup(int matchGroup) {
        this.matchGroup = matchGroup;
    }

    public boolean isUrlDecodeBefore() {
        return urlDecodeBefore;
    }

    public void setUrlDecodeBefore(boolean urlDecodeBefore) {
        this.urlDecodeBefore = urlDecodeBefore;
    }

    public boolean isUrlEncodeAfter() {
        return urlEncodeAfter;
    }

    public void setUrlEncodeAfter(boolean urlEncodeAfter) {
        this.urlEncodeAfter = urlEncodeAfter;
    }

}
