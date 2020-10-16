/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyradar.ui.components;

import com.cyradar.models.InterceptConfiguration;
import com.cyradar.models.InterceptStage;
import com.cyradar.models.InterceptTargetParameter;
import com.cyradar.models.InterceptTargetUrl;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JRadioButton;

/**
 *
 * @author phinc27
 */
public final class InterceptConfigurationForm extends javax.swing.JPanel {

    private InterceptConfiguration configuration;

    private class RBTargetUrlTypeItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            JRadioButton rb = (JRadioButton) e.getSource();
            tfTargetURL.setEnabled(rb == rbTargetUrlTypeCustom);
            tfTargetURL.setEditable(rb == rbTargetUrlTypeCustom);
            cbUseRegex.setEnabled(rb == rbTargetUrlTypeCustom);
        }
    }

    /**
     * Creates new form InterceptConfigurationForm
     */
    public InterceptConfigurationForm() {
        initComponents();
        RBTargetUrlTypeItemListener urlTypeListener = new RBTargetUrlTypeItemListener();
        rbTargetUrlTypeAll.addItemListener(urlTypeListener);
        rbTargetUrlTypeInScope.addItemListener(urlTypeListener);
        rbTargetUrlTypeCustom.addItemListener(urlTypeListener);
        this.configuration = InterceptConfiguration.getDefault();
        setConfiguration(configuration);
    }

    public void setConfiguration(InterceptConfiguration configuration) {
        this.configuration = configuration;
        setTargetUrl(configuration.getTarget());
        setTargetParameter(configuration.getParameter());
        setStages(configuration.getStages());
        swEnable.setOnOff(configuration.isEnabled());
        setComponentEnabled(configuration.isEnabled());
    }

    private void setTargetUrl(InterceptTargetUrl targetUrl) {
        rbTargetMessageTypeRequest.setSelected(targetUrl.getMessageType() == InterceptTargetUrl.MESSAGE_TYPE_REQUEST);
        rbTargetMessageTypeResponse.setSelected(targetUrl.getMessageType() == InterceptTargetUrl.MESSAGE_TYPE_RESPONSE);
        rbTargetMessageTypeBoth.setSelected(targetUrl.getMessageType() == InterceptTargetUrl.MESSAGE_TYPE_BOTH);
        rbTargetUrlTypeAll.setSelected(targetUrl.getUrlType() == InterceptTargetUrl.URL_TYPE_ALL);
        rbTargetUrlTypeInScope.setSelected(targetUrl.getUrlType() == InterceptTargetUrl.URL_TYPE_INSCOPE);
        rbTargetUrlTypeCustom.setSelected(targetUrl.getUrlType() == InterceptTargetUrl.URL_TYPE_CUSTOM);
        if (rbTargetUrlTypeCustom.isSelected()) {
            tfTargetURL.setEnabled(true);
            tfTargetURL.setEditable(true);
            tfTargetURL.setText(targetUrl.getUrl());
            cbUseRegex.setEnabled(true);
            cbUseRegex.setSelected(targetUrl.isUsingRegex());
        } else {
            tfTargetURL.setText("");
            tfTargetURL.setEnabled(false);
            tfTargetURL.setEditable(false);
            cbUseRegex.setSelected(false);
            cbUseRegex.setEnabled(false);
        }
    }

    private void setTargetParameter(InterceptTargetParameter targetParameter) {
        // Currently find and replace paramteters by regex is not supported.
        // useRegex, matchGroup and matchPosition will be added in the future.
        tfParamName.setText(targetParameter.getParamName());
        cboxUrlDecodeBefore.setSelected(targetParameter.isUrlDecodeBefore());
        cboxUrlEncodeAfter.setSelected(targetParameter.isUrlEncodeAfter());
    }

    private void setStages(List<InterceptStage> stages) {
        if (stages == null || stages.size() != 1) {
            // This happens only if the end-user tries to load a configuration file edited by their own.
            // In this case, the app does not work as it is expected.
            // As the developer for this app, I am supposed to handle the logic to make things work.
            // But no, I believe people use this tool know exactly what they are doing
            return;
        }
        InterceptStage stage = stages.get(0);
        interceptStageForm.setInterceptStage(stage);
    }

    private void setComponentEnabled(boolean enabled) {
        btnApply.setEnabled(enabled);
        btnDiscard.setEnabled(enabled);
        rbTargetMessageTypeBoth.setEnabled(enabled);
        rbTargetMessageTypeRequest.setEnabled(enabled);
        rbTargetMessageTypeResponse.setEnabled(enabled);
        rbTargetUrlTypeAll.setEnabled(enabled);
        rbTargetUrlTypeInScope.setEnabled(enabled);
        rbTargetUrlTypeCustom.setEnabled(enabled);
        tfTargetURL.setEnabled(enabled && rbTargetUrlTypeCustom.isSelected());
        tfTargetURL.setEditable(enabled && rbTargetUrlTypeCustom.isSelected());
        cbUseRegex.setEnabled(enabled && rbTargetUrlTypeCustom.isSelected());
        tfParamName.setEnabled(enabled);
        cboxUrlDecodeBefore.setEnabled(enabled);
        cboxUrlEncodeAfter.setEnabled(enabled);
        interceptStageForm.setComponentEnabled(enabled);
    }

    public InterceptConfiguration getConfiguration() {
        InterceptConfiguration config = InterceptConfiguration.getDefault();
        config.setTitle(configuration.getTitle());
        config.setEnabled(swEnable.isOnOff());
        config.setTarget(getTargetUrl());
        config.setParameter(getTargetParameter());
        // See InterceptConfiguration.getDefault() to better understanding why there is only one stage here
        List<InterceptStage> stages = new ArrayList<>();
        stages.add(interceptStageForm.getInterceptStage());
        config.setStages(stages);
        return config;
    }

    private InterceptTargetUrl getTargetUrl() {
        InterceptTargetUrl target = InterceptTargetUrl.getDefault();
        target.setMessageType(
                rbTargetMessageTypeBoth.isSelected() ? InterceptTargetUrl.MESSAGE_TYPE_BOTH
                : rbTargetMessageTypeResponse.isSelected() ? InterceptTargetUrl.MESSAGE_TYPE_RESPONSE : InterceptTargetUrl.MESSAGE_TYPE_REQUEST
        );
        target.setUrlType(
                rbTargetUrlTypeCustom.isSelected() ? InterceptTargetUrl.URL_TYPE_CUSTOM
                : rbTargetUrlTypeInScope.isSelected() ? InterceptTargetUrl.URL_TYPE_INSCOPE : InterceptTargetUrl.URL_TYPE_ALL
        );
        // Save both url string and useRegex even when selected type is not custom
        target.setUrl(tfTargetURL.getText());
        target.setUsingRegex(cbUseRegex.isSelected());
        return target;
    }

    private InterceptTargetParameter getTargetParameter() {
        InterceptTargetParameter parameter = InterceptTargetParameter.getDefault();
        parameter.setParamName(tfParamName.getText());
        parameter.setUrlDecodeBefore(cboxUrlDecodeBefore.isSelected());
        parameter.setUrlEncodeAfter(cboxUrlEncodeAfter.isSelected());
        // TODO currently not supported
        parameter.setUsingRegex(false);
        parameter.setMatchGroup(-1);
        parameter.setMatchPosition("");
        return parameter;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgTargetMessageType = new javax.swing.ButtonGroup();
        bgTargetUrlType = new javax.swing.ButtonGroup();
        targetPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rbTargetMessageTypeRequest = new javax.swing.JRadioButton();
        rbTargetMessageTypeResponse = new javax.swing.JRadioButton();
        rbTargetMessageTypeBoth = new javax.swing.JRadioButton();
        rbTargetUrlTypeAll = new javax.swing.JRadioButton();
        rbTargetUrlTypeInScope = new javax.swing.JRadioButton();
        rbTargetUrlTypeCustom = new javax.swing.JRadioButton();
        tfTargetURL = new javax.swing.JTextField();
        cbUseRegex = new javax.swing.JCheckBox();
        swEnable = new com.cyradar.ui.components.Switch();
        btnApply = new javax.swing.JButton();
        btnDiscard = new javax.swing.JButton();
        stagePanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        tfParamName = new javax.swing.JTextField();
        cboxUrlDecodeBefore = new javax.swing.JCheckBox();
        cboxUrlEncodeAfter = new javax.swing.JCheckBox();
        interceptStageForm = new com.cyradar.ui.components.InterceptStageForm();
        jLabel4 = new javax.swing.JLabel();

        targetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Targets"));

        jLabel1.setFont(new java.awt.Font("Tahoma", 3, 12)); // NOI18N
        jLabel1.setText("Choose which target urls will be handled by this tab");

        bgTargetMessageType.add(rbTargetMessageTypeRequest);
        rbTargetMessageTypeRequest.setSelected(true);
        rbTargetMessageTypeRequest.setText("Requests");

        bgTargetMessageType.add(rbTargetMessageTypeResponse);
        rbTargetMessageTypeResponse.setText("Responses");

        bgTargetMessageType.add(rbTargetMessageTypeBoth);
        rbTargetMessageTypeBoth.setText("Both");

        bgTargetUrlType.add(rbTargetUrlTypeAll);
        rbTargetUrlTypeAll.setSelected(true);
        rbTargetUrlTypeAll.setText("All");

        bgTargetUrlType.add(rbTargetUrlTypeInScope);
        rbTargetUrlTypeInScope.setText("In Burp scope");

        bgTargetUrlType.add(rbTargetUrlTypeCustom);
        rbTargetUrlTypeCustom.setText("Custom");

        tfTargetURL.setEditable(false);

        cbUseRegex.setText("Use regex");
        cbUseRegex.setEnabled(false);

        swEnable.setText("switch1");
        swEnable.setOnOff(false);
        swEnable.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                swEnableItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout targetPanelLayout = new javax.swing.GroupLayout(targetPanel);
        targetPanel.setLayout(targetPanelLayout);
        targetPanelLayout.setHorizontalGroup(
            targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(targetPanelLayout.createSequentialGroup()
                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(targetPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(swEnable, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(targetPanelLayout.createSequentialGroup()
                        .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbTargetMessageTypeRequest)
                            .addComponent(rbTargetMessageTypeResponse)
                            .addComponent(rbTargetMessageTypeBoth))
                        .addGap(119, 119, 119)
                        .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbTargetUrlTypeInScope)
                            .addComponent(rbTargetUrlTypeAll)
                            .addGroup(targetPanelLayout.createSequentialGroup()
                                .addComponent(rbTargetUrlTypeCustom)
                                .addGap(18, 18, 18)
                                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbUseRegex)
                                    .addComponent(tfTargetURL, javax.swing.GroupLayout.PREFERRED_SIZE, 555, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        targetPanelLayout.setVerticalGroup(
            targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(targetPanelLayout.createSequentialGroup()
                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(targetPanelLayout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel1))
                    .addComponent(swEnable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbTargetMessageTypeRequest)
                    .addComponent(rbTargetUrlTypeAll))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbTargetMessageTypeResponse)
                    .addComponent(rbTargetUrlTypeInScope))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbTargetMessageTypeBoth)
                    .addComponent(rbTargetUrlTypeCustom)
                    .addComponent(tfTargetURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(cbUseRegex))
        );

        btnApply.setText("Apply");
        btnApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApplyActionPerformed(evt);
            }
        });

        btnDiscard.setText("Discard");
        btnDiscard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDiscardActionPerformed(evt);
            }
        });

        stagePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameter Handling"));

        jLabel3.setFont(new java.awt.Font("Tahoma", 3, 12)); // NOI18N
        jLabel3.setText("Parameter:");

        cboxUrlDecodeBefore.setSelected(true);
        cboxUrlDecodeBefore.setText("Automatically URL - decode value before modifying");

        cboxUrlEncodeAfter.setSelected(true);
        cboxUrlEncodeAfter.setText("Automatically URL - encode value if modified");

        javax.swing.GroupLayout stagePanelLayout = new javax.swing.GroupLayout(stagePanel);
        stagePanel.setLayout(stagePanelLayout);
        stagePanelLayout.setHorizontalGroup(
            stagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(stagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(stagePanelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(tfParamName, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cboxUrlDecodeBefore)
                    .addComponent(cboxUrlEncodeAfter))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(interceptStageForm, javax.swing.GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
        );
        stagePanelLayout.setVerticalGroup(
            stagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(stagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tfParamName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cboxUrlDecodeBefore)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cboxUrlEncodeAfter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(interceptStageForm, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel4.setFont(new java.awt.Font("Tahoma", 3, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 51, 51));
        jLabel4.setText("Note: Enabling/disabling tabs will also apply changes");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stagePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(targetPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnApply, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDiscard, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(targetPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnApply, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDiscard, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDiscardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiscardActionPerformed
        setConfiguration(configuration);
    }//GEN-LAST:event_btnDiscardActionPerformed

    private void swEnableItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_swEnableItemStateChanged
        setComponentEnabled(evt.getStateChange() == ItemEvent.SELECTED);
        InterceptConfiguration newConfiguration = getConfiguration();
        firePropertyChange("xx", configuration, newConfiguration);
        // Call to setConfiguration to remove unnecessary data
        setConfiguration(newConfiguration);
        configuration = newConfiguration;
    }//GEN-LAST:event_swEnableItemStateChanged

    public void applyChanges() {
        InterceptConfiguration newConfiguration = getConfiguration();
        firePropertyChange("xx", configuration, newConfiguration);
        // Call to setConfiguration to remove unnecessary data
        setConfiguration(newConfiguration);
        configuration = newConfiguration;
    }

    private void btnApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyActionPerformed
        InterceptConfiguration newConfiguration = getConfiguration();
        firePropertyChange("xx", configuration, newConfiguration);
        // Call to setConfiguration to remove unnecessary data
        setConfiguration(newConfiguration);
        configuration = newConfiguration;
    }//GEN-LAST:event_btnApplyActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgTargetMessageType;
    private javax.swing.ButtonGroup bgTargetUrlType;
    private javax.swing.JButton btnApply;
    private javax.swing.JButton btnDiscard;
    private javax.swing.JCheckBox cbUseRegex;
    private javax.swing.JCheckBox cboxUrlDecodeBefore;
    private javax.swing.JCheckBox cboxUrlEncodeAfter;
    private com.cyradar.ui.components.InterceptStageForm interceptStageForm;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JRadioButton rbTargetMessageTypeBoth;
    private javax.swing.JRadioButton rbTargetMessageTypeRequest;
    private javax.swing.JRadioButton rbTargetMessageTypeResponse;
    private javax.swing.JRadioButton rbTargetUrlTypeAll;
    private javax.swing.JRadioButton rbTargetUrlTypeCustom;
    private javax.swing.JRadioButton rbTargetUrlTypeInScope;
    private javax.swing.JPanel stagePanel;
    private com.cyradar.ui.components.Switch swEnable;
    private javax.swing.JPanel targetPanel;
    private javax.swing.JTextField tfParamName;
    private javax.swing.JTextField tfTargetURL;
    // End of variables declaration//GEN-END:variables
}
