/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyradar.models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author phinc27
 */
public class InterceptConfiguration {

    private boolean enabled;
    private String title;
    private InterceptTargetUrl target;
    private InterceptTargetParameter parameter;
    private List<InterceptStage> stages;

    /**
     * Initialize a configuration which is used as model for
     * InterceptConfigurationForm You might wonder why the stages is a list but
     * default and even GUI only supports one. Because when I first design this
     * app, I thought it would be cool if a parameter could be processed by
     * multiple parameter processors like a pipeline. The fact is, we do not
     * face this kind of problem frequently. Furthermore, it is easily solved by
     * creating multiple InterceptConfigurationForm tab with the same parameter
     * name. So i decided to make it easier. But I still keep the stages as a
     * list in case of I might change my mind at some point in the future.
     *
     * @return default InterceptConfiguration
     */
    public static InterceptConfiguration getDefault() {
        InterceptConfiguration configuration = new InterceptConfiguration();
        configuration.title = "";
        configuration.enabled = true;
        configuration.stages.add(InterceptStage.getDefault());
        return configuration;
    }

    public InterceptConfiguration() {
        target = InterceptTargetUrl.getDefault();
        parameter = InterceptTargetParameter.getDefault();
        stages = new ArrayList<>();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public InterceptTargetUrl getTarget() {
        return target;
    }

    public void setTarget(InterceptTargetUrl target) {
        this.target = target;
    }

    public InterceptTargetParameter getParameter() {
        return parameter;
    }

    public void setParameter(InterceptTargetParameter parameter) {
        this.parameter = parameter;
    }

    public List<InterceptStage> getStages() {
        return stages;
    }

    public void setStages(List<InterceptStage> stages) {
        this.stages = stages;
    }

}
