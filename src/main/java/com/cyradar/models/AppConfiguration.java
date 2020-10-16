/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyradar.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author phinc27
 */
public class AppConfiguration {

    /**
     * Flag used to identify Burp Suite as a whole.
     */
    public static final int TOOL_SUITE = 0x00000001;
    /**
     * Flag used to identify the Burp Target tool.
     */
    public static final int TOOL_TARGET = 0x00000002;
    /**
     * Flag used to identify the Burp Proxy tool.
     */
    public static final int TOOL_PROXY = 0x00000004;
    /**
     * Flag used to identify the Burp Spider tool.
     */
    public static final int TOOL_SPIDER = 0x00000008;
    /**
     * Flag used to identify the Burp Scanner tool.
     */
    public static final int TOOL_SCANNER = 0x00000010;
    /**
     * Flag used to identify the Burp Intruder tool.
     */
    public static final int TOOL_INTRUDER = 0x00000020;
    /**
     * Flag used to identify the Burp Repeater tool.
     */
    public static final int TOOL_REPEATER = 0x00000040;
    /**
     * Flag used to identify the Burp Sequencer tool.
     */
    public static final int TOOL_SEQUENCER = 0x00000080;
    /**
     * Flag used to identify the Burp Decoder tool.
     */
    public static final int TOOL_DECODER = 0x00000100;
    /**
     * Flag used to identify the Burp Comparer tool.
     */
    public static final int TOOL_COMPARER = 0x00000200;
    /**
     * Flag used to identify the Burp Extender tool.
     */
    public static final int TOOL_EXTENDER = 0x00000400;

    public static final int LOG_VERBOSITY_LEVEL_DEBUG = 3;
    public static final int LOG_VERBOSITY_LEVEL_INFO = 2;
    public static final int LOG_VERBOSITY_LEVEL_WARN = 1;
    public static final int LOG_VERBOSITY_LEVEL_ERROR = 0;

    private int verbosityLevel;
    private Map<Integer, Boolean> toolScopes;
    private List<InterceptConfiguration> interceptConfigurations;

    public static AppConfiguration getDefault() {
        AppConfiguration ret = new AppConfiguration();
        ret.toolScopes.put(TOOL_SUITE, Boolean.FALSE);
        ret.toolScopes.put(TOOL_TARGET, Boolean.FALSE);
        ret.toolScopes.put(TOOL_PROXY, Boolean.TRUE);
        ret.toolScopes.put(TOOL_SPIDER, Boolean.FALSE);
        ret.toolScopes.put(TOOL_SCANNER, Boolean.FALSE);
        ret.toolScopes.put(TOOL_INTRUDER, Boolean.TRUE);
        ret.toolScopes.put(TOOL_REPEATER, Boolean.TRUE);
        ret.toolScopes.put(TOOL_SEQUENCER, Boolean.FALSE);
        ret.toolScopes.put(TOOL_DECODER, Boolean.FALSE);
        ret.toolScopes.put(TOOL_COMPARER, Boolean.FALSE);
        ret.toolScopes.put(TOOL_EXTENDER, Boolean.FALSE);
        ret.verbosityLevel = LOG_VERBOSITY_LEVEL_INFO;
        return ret;
    }

    public AppConfiguration() {
        toolScopes = new HashMap<>();
        interceptConfigurations = new ArrayList<>();
    }

    public int getVerbosityLevel() {
        return verbosityLevel;
    }

    public void setVerbosityLevel(int verbosityLevel) {
        this.verbosityLevel = verbosityLevel;
    }

    public Map<Integer, Boolean> getToolScopes() {
        return toolScopes;
    }

    public void setToolScopes(Map<Integer, Boolean> toolScopes) {
        this.toolScopes = toolScopes;
    }

    public List<InterceptConfiguration> getInterceptConfigurations() {
        return interceptConfigurations;
    }

    public void setInterceptConfigurations(List<InterceptConfiguration> interceptConfigurations) {
        this.interceptConfigurations = interceptConfigurations;
    }

}
