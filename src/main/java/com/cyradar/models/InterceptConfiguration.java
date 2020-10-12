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

    private InterceptTargetUrl target;
    private InterceptTargetParameter parameter;
    private List<InterceptStage> stages;

    public static InterceptConfiguration getDefault() {
        return new InterceptConfiguration();
    }

    public InterceptConfiguration() {
        target = InterceptTargetUrl.getDefault();
        parameter = InterceptTargetParameter.getDefault();
        stages = new ArrayList<>();
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
