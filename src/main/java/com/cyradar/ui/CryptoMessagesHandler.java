/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyradar.ui;

import com.cyradar.common.TextAreaAppender;
import com.cyradar.models.AppConfiguration;
import com.cyradar.models.InterceptConfiguration;
import com.cyradar.ui.components.ClosableTabComponent;
import com.cyradar.ui.components.InterceptConfigurationForm;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.AWTKeyStroke;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 *
 * @author phinc27
 */
public final class CryptoMessagesHandler extends javax.swing.JPanel implements ChangeListener {

    private AppConfiguration configuration;
    private static final Logger logger = LogManager.getLogger("com.cyradar.ui.CryptoMessagesHandler");
    /**
     * count is used to count the number of tabs has been added.
     */
    private int count;

    /**
     * Creates new form CryptoMessagesHandler
     */
    public CryptoMessagesHandler() {
        initComponents();
        initTabbedPaneKeyStrokes();
        main.addChangeListener(CryptoMessagesHandler.this);
        setConfiguration(AppConfiguration.getDefault());
        TextAreaAppender.setTextArea(taLogging);
    }

    public AppConfiguration getConfiguration() {
        return configuration;
    }

    private void setConfiguration(AppConfiguration configuration) {
        this.configuration = configuration;
        setLogLevel(configuration.getVerbosityLevel());
        Map<Integer, Boolean> toolScopes = configuration.getToolScopes();
        cboxComparer.setSelected(toolScopes.getOrDefault(AppConfiguration.TOOL_COMPARER, Boolean.FALSE));
        cboxDecoder.setSelected(toolScopes.getOrDefault(AppConfiguration.TOOL_DECODER, Boolean.FALSE));
        cboxExtender.setSelected(toolScopes.getOrDefault(AppConfiguration.TOOL_EXTENDER, Boolean.FALSE));
        cboxIntruder.setSelected(toolScopes.getOrDefault(AppConfiguration.TOOL_INTRUDER, Boolean.FALSE));
        cboxProxy.setSelected(toolScopes.getOrDefault(AppConfiguration.TOOL_PROXY, Boolean.FALSE));
        cboxRepeater.setSelected(toolScopes.getOrDefault(AppConfiguration.TOOL_REPEATER, Boolean.FALSE));
        cboxScanner.setSelected(toolScopes.getOrDefault(AppConfiguration.TOOL_SCANNER, Boolean.FALSE));
        cboxSequencer.setSelected(toolScopes.getOrDefault(AppConfiguration.TOOL_SEQUENCER, Boolean.FALSE));
        cboxSpider.setSelected(toolScopes.getOrDefault(AppConfiguration.TOOL_SPIDER, Boolean.FALSE));
        cboxSuite.setSelected(toolScopes.getOrDefault(AppConfiguration.TOOL_SUITE, Boolean.FALSE));
        cboxTarget.setSelected(toolScopes.getOrDefault(AppConfiguration.TOOL_TARGET, Boolean.FALSE));
        count = 0;
        main.setSelectedIndex(0);
        while (main.getTabCount() > 2) {
            main.remove(1);
        }
        configuration.getInterceptConfigurations().stream().map(intercepConfiguration -> {
            count += 1;
            return intercepConfiguration;
        }).forEachOrdered(intercepConfiguration -> {
            InterceptConfigurationForm form = new InterceptConfigurationForm();
            form.setConfiguration(intercepConfiguration);
            addTab(intercepConfiguration.getTitle(), form);
        });
        main.setSelectedIndex(0);
    }

    private void applyChangesInAllTabs() {
        for (int i = 1; i < main.getTabCount() - 1; i++) {
            ClosableTabComponent tabComponent = (ClosableTabComponent) main.getTabComponentAt(i);
            tabComponent.stopEditing();
            InterceptConfigurationForm form = (InterceptConfigurationForm) main.getComponentAt(i);
            form.applyChanges();
        }
    }

    /**
     * Add a new InterceptConfigurationForm to the last of the main tabbedPane
     * This method does not add the model to the application configuration
     *
     * @param title tab title
     * @param component the component to be display in the tab
     */
    public void addTab(String title, InterceptConfigurationForm component) {
        int index = this.main.getTabCount() - 1;
        addTabAtIndex(title, component, index);
    }

    /**
     * Add a new InterceptConfigurationForm to the main tabbedPane at specified
     * index This method does not add the model to the application configuration
     * The index in tabbedPane is index - 1 in interceptConfigurations list
     *
     * @param title tab title
     * @param component the component to be display in the tab
     * @param index index to be added
     */
    private void addTabAtIndex(String title, InterceptConfigurationForm component, int index) {
        component.addPropertyChangeListener(new InterceptConfigurationPropertyChangeListener());
        this.main.add(component, index);
        ClosableTabComponent titleTabComponent = new ClosableTabComponent(title);
        titleTabComponent.setCloseButtonToolTip("close this tab");
        titleTabComponent.addCloseActionListener((ActionEvent e) -> {
            JButton source = (JButton) e.getSource();
            // Find tab component which contains source button
            ClosableTabComponent tabComponent = (ClosableTabComponent) SwingUtilities.getAncestorOfClass(ClosableTabComponent.class, source);
            int removeIndex = main.indexOfTabComponent(tabComponent);
            // if remove the last configuration tab, set the selected index to
            // the one before it to avoid changeListener automatically create
            // a new one
            if (removeIndex == main.getTabCount() - 2) {
                main.setSelectedIndex(removeIndex - 1);
            }
            configuration.getInterceptConfigurations().remove(removeIndex - 1);
            main.remove(removeIndex);
        });
        titleTabComponent.addPropertyChangeListener("title", (PropertyChangeEvent evt) -> {
            ClosableTabComponent tabComponent = (ClosableTabComponent) evt.getSource();
            int index1 = main.indexOfTabComponent(tabComponent);
            configuration.getInterceptConfigurations().get(index1 - 1).setTitle(evt.getNewValue().toString());
        });
        this.main.setTabComponentAt(index, titleTabComponent);
        this.main.setSelectedIndex(index);
    }

    private void initTabbedPaneKeyStrokes() {
        KeyStroke ctrlTab = KeyStroke.getKeyStroke("ctrl TAB");
        KeyStroke ctrlShiftTab = KeyStroke.getKeyStroke("ctrl shift TAB");
        // Remove ctrl-tab from normal focus traversal
        Set<AWTKeyStroke> forwardKeys = new HashSet<>(main.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.remove(ctrlTab);
        main.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
        // Remove ctrl-shift-tab from normal focus traversal
        Set<AWTKeyStroke> backwardKeys = new HashSet<>(main.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backwardKeys.remove(ctrlShiftTab);
        main.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
        // Add keys to the tab's input map
        InputMap inputMap = main.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(ctrlTab, "navigateNext");
        inputMap.put(ctrlShiftTab, "navigatePrevious");
        inputMap.put(KeyStroke.getKeyStroke("ctrl released W"), "closeTab");
        inputMap.put(KeyStroke.getKeyStroke("ctrl released N"), "newTab");
        inputMap.put(KeyStroke.getKeyStroke("ctrl shift released N"), "cloneTab");
        inputMap.put(KeyStroke.getKeyStroke("F2"), "renameTab");
        inputMap.put(KeyStroke.getKeyStroke("ctrl RIGHT"), "moveTabForward");
        inputMap.put(KeyStroke.getKeyStroke("ctrl LEFT"), "moveTabBackward");

        // Override two built-in keystroke
        main.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "navigateNext");
        main.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "navigatePrevious");

        main.getActionMap().put("navigateNext", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int nextIndex = (main.getSelectedIndex() + 1) % main.getTabCount();
                if (nextIndex == main.getTabCount() - 1) {
                    nextIndex = 0;
                }
                main.setSelectedIndex(nextIndex);
            }
        });
        main.getActionMap().put("navigatePrevious", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int nextIndex = (main.getSelectedIndex() + main.getTabCount() - 1) % main.getTabCount();
                if (nextIndex == main.getTabCount() - 1) {
                    nextIndex = main.getTabCount() - 2;
                }
                main.setSelectedIndex(nextIndex);
            }
        });
        main.getActionMap().put("closeTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int removeIndex = main.getSelectedIndex();
                if (removeIndex == 0) {
                    return;
                }
                if (removeIndex == main.getTabCount() - 2) {
                    main.setSelectedIndex(removeIndex - 1);
                }
                configuration.getInterceptConfigurations().remove(removeIndex - 1);
                main.remove(removeIndex);
            }
        });
        main.getActionMap().put("newTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentIndex = main.getSelectedIndex();
                InterceptConfiguration interceptConfiguration = InterceptConfiguration.getDefault();
                count += 1;
                String title = String.valueOf(count);
                interceptConfiguration.setTitle(title);
                configuration.getInterceptConfigurations().add(currentIndex, interceptConfiguration);
                addTabAtIndex(title, createNewTabCompo(interceptConfiguration), currentIndex + 1);
            }
        });
        main.getActionMap().put("cloneTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentIndex = main.getSelectedIndex();
                if (currentIndex == 0) {
                    return;
                }
                String title = ((ClosableTabComponent) main.getTabComponentAt(currentIndex)).getTitle();
                InterceptConfigurationForm form = (InterceptConfigurationForm) main.getComponentAt(currentIndex);
                InterceptConfiguration interceptConfiguration = form.getConfiguration();
                Gson g = new Gson();
                InterceptConfiguration cloned = g.fromJson(g.toJson(interceptConfiguration), InterceptConfiguration.class);
                cloned.setTitle(cloned.getTitle() + " - clone");
                count += 1;
                configuration.getInterceptConfigurations().add(currentIndex, cloned);
                addTabAtIndex(title + " - clone", createNewTabCompo(cloned), currentIndex + 1);
            }
        });
        main.getActionMap().put("renameTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentIndex = main.getSelectedIndex();
                if (currentIndex == 0) {
                    return;
                }
                ClosableTabComponent tabComponent = ((ClosableTabComponent) main.getTabComponentAt(currentIndex));
                tabComponent.startEditting();
            }
        });
        main.getActionMap().put("moveTabForward", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentIndex = main.getSelectedIndex();
                if (currentIndex == 0) {
                    return;
                }
                // TODO logic to change running configuration too
                ClosableTabComponent tabComponent = (ClosableTabComponent) main.getTabComponentAt(currentIndex);
                InterceptConfigurationForm component = (InterceptConfigurationForm) main.getComponentAt(currentIndex);
                int nextIndex = currentIndex + 1;
                if (currentIndex == main.getTabCount() - 2) {
                    nextIndex = 1;
                    main.setSelectedIndex(0);
                }
                main.remove(currentIndex);
                InterceptConfiguration movingConfiguration = configuration.getInterceptConfigurations().remove(currentIndex - 1);
                configuration.getInterceptConfigurations().add(nextIndex - 1, movingConfiguration);
                main.add(component, nextIndex);
                main.setTabComponentAt(nextIndex, tabComponent);
                main.setSelectedIndex(nextIndex);
            }
        });
        main.getActionMap().put("moveTabBackward", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentIndex = main.getSelectedIndex();
                if (currentIndex == 0) {
                    return;
                }
                // TODO logic to change running configuration too
                ClosableTabComponent tabComponent = (ClosableTabComponent) main.getTabComponentAt(currentIndex);
                InterceptConfigurationForm component = (InterceptConfigurationForm) main.getComponentAt(currentIndex);
                // To avoid a new tab is created when moving tab
                if (currentIndex == main.getTabCount() - 2) {
                    main.setSelectedIndex(currentIndex - 1);
                }
                int nextIndex = currentIndex - 1;
                if (nextIndex == 0) {
                    nextIndex = main.getTabCount() - 2;
                }
                main.remove(currentIndex);
                InterceptConfiguration movingConfiguration = configuration.getInterceptConfigurations().remove(currentIndex - 1);
                configuration.getInterceptConfigurations().add(nextIndex - 1, movingConfiguration);
                main.add(component, nextIndex);
                main.setTabComponentAt(nextIndex, tabComponent);
                main.setSelectedIndex(nextIndex);
            }
        });
    }

    private InterceptConfigurationForm createNewTabCompo(InterceptConfiguration config) {
        InterceptConfigurationForm form = new InterceptConfigurationForm();
        form.setConfiguration(config);
        return form;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        main = new javax.swing.JTabbedPane();
        optionsPane = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cboxProxy = new javax.swing.JCheckBox();
        cboxTarget = new javax.swing.JCheckBox();
        cboxSpider = new javax.swing.JCheckBox();
        cboxRepeater = new javax.swing.JCheckBox();
        cboxSequencer = new javax.swing.JCheckBox();
        cboxIntruder = new javax.swing.JCheckBox();
        cboxScanner = new javax.swing.JCheckBox();
        cboxExtender = new javax.swing.JCheckBox();
        cboxDecoder = new javax.swing.JCheckBox();
        cboxComparer = new javax.swing.JCheckBox();
        cboxSuite = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        taLogging = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        btnLoadConfigurations = new javax.swing.JButton();
        btnApplyChangesInTabs = new javax.swing.JButton();
        btnSaveConfigurations = new javax.swing.JButton();
        cmbLogVerbosityLevel = new javax.swing.JComboBox<>();
        btnClearLogs = new javax.swing.JButton();
        dummyPane = new javax.swing.JPanel();

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Tool scope settings"));

        cboxProxy.setSelected(true);
        cboxProxy.setText("Proxy");
        cboxProxy.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxProxyItemStateChanged(evt);
            }
        });

        cboxTarget.setText("Target");
        cboxTarget.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxTargetItemStateChanged(evt);
            }
        });

        cboxSpider.setText("Spider");
        cboxSpider.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxSpiderItemStateChanged(evt);
            }
        });

        cboxRepeater.setSelected(true);
        cboxRepeater.setText("Repeater");
        cboxRepeater.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxRepeaterItemStateChanged(evt);
            }
        });

        cboxSequencer.setText("Sequencer");
        cboxSequencer.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxSequencerItemStateChanged(evt);
            }
        });

        cboxIntruder.setSelected(true);
        cboxIntruder.setText("Intruder");
        cboxIntruder.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxIntruderItemStateChanged(evt);
            }
        });

        cboxScanner.setText("Scanner");
        cboxScanner.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxScannerItemStateChanged(evt);
            }
        });

        cboxExtender.setText("Extender");
        cboxExtender.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxExtenderItemStateChanged(evt);
            }
        });

        cboxDecoder.setText("Decoder");
        cboxDecoder.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxDecoderItemStateChanged(evt);
            }
        });

        cboxComparer.setText("Comparer");
        cboxComparer.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxComparerItemStateChanged(evt);
            }
        });

        cboxSuite.setText("Suite");
        cboxSuite.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxSuiteItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboxProxy)
                    .addComponent(cboxRepeater)
                    .addComponent(cboxTarget)
                    .addComponent(cboxSpider))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboxExtender)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cboxSequencer)
                            .addComponent(cboxIntruder)
                            .addComponent(cboxScanner))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cboxSuite)
                            .addComponent(cboxComparer)
                            .addComponent(cboxDecoder))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboxProxy)
                    .addComponent(cboxSequencer)
                    .addComponent(cboxDecoder))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboxTarget)
                    .addComponent(cboxIntruder)
                    .addComponent(cboxComparer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboxSpider, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboxScanner)
                    .addComponent(cboxSuite))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboxRepeater)
                    .addComponent(cboxExtender)))
        );

        taLogging.setEditable(false);
        taLogging.setColumns(20);
        taLogging.setRows(5);
        jScrollPane1.setViewportView(taLogging);

        jLabel1.setText("Log verbosity level:");

        btnLoadConfigurations.setText("Load configurations");
        btnLoadConfigurations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadConfigurationsActionPerformed(evt);
            }
        });

        btnApplyChangesInTabs.setText("Apply changes in tabs");
        btnApplyChangesInTabs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApplyChangesInTabsActionPerformed(evt);
            }
        });

        btnSaveConfigurations.setText("Save configurations");
        btnSaveConfigurations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveConfigurationsActionPerformed(evt);
            }
        });

        cmbLogVerbosityLevel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "DEBUG", "INFO", "WARN", "ERROR" }));
        cmbLogVerbosityLevel.setSelectedIndex(1);
        cmbLogVerbosityLevel.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbLogVerbosityLevelItemStateChanged(evt);
            }
        });

        btnClearLogs.setText("Clear logs");
        btnClearLogs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearLogsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout optionsPaneLayout = new javax.swing.GroupLayout(optionsPane);
        optionsPane.setLayout(optionsPaneLayout);
        optionsPaneLayout.setHorizontalGroup(
            optionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(optionsPaneLayout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(optionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnApplyChangesInTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                            .addComponent(btnSaveConfigurations, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnLoadConfigurations, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 570, Short.MAX_VALUE))
                    .addGroup(optionsPaneLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbLogVerbosityLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClearLogs)))
                .addContainerGap())
        );
        optionsPaneLayout.setVerticalGroup(
            optionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(optionsPaneLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(btnLoadConfigurations)
                        .addGap(4, 4, 4)
                        .addComponent(btnSaveConfigurations)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnApplyChangesInTabs)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbLogVerbosityLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClearLogs))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                .addContainerGap())
        );

        main.addTab("Options", optionsPane);

        javax.swing.GroupLayout dummyPaneLayout = new javax.swing.GroupLayout(dummyPane);
        dummyPane.setLayout(dummyPaneLayout);
        dummyPaneLayout.setHorizontalGroup(
            dummyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1075, Short.MAX_VALUE)
        );
        dummyPaneLayout.setVerticalGroup(
            dummyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 692, Short.MAX_VALUE)
        );

        main.addTab("...", dummyPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(main)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(main)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cboxProxyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxProxyItemStateChanged
        logger.debug("tool scope PROXY enabled: ", evt.getStateChange() == ItemEvent.SELECTED);
        configuration.getToolScopes().put(AppConfiguration.TOOL_PROXY, evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_cboxProxyItemStateChanged

    private void cboxTargetItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxTargetItemStateChanged
        logger.debug("tool scope TARGET enabled: ", evt.getStateChange() == ItemEvent.SELECTED);
        configuration.getToolScopes().put(AppConfiguration.TOOL_TARGET, evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_cboxTargetItemStateChanged

    private void cboxSpiderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxSpiderItemStateChanged
        logger.debug("tool scope SPIDER enabled: ", evt.getStateChange() == ItemEvent.SELECTED);
        configuration.getToolScopes().put(AppConfiguration.TOOL_SPIDER, evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_cboxSpiderItemStateChanged

    private void cboxRepeaterItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxRepeaterItemStateChanged
        logger.debug("tool scope REPEATER enabled: ", evt.getStateChange() == ItemEvent.SELECTED);
        configuration.getToolScopes().put(AppConfiguration.TOOL_REPEATER, evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_cboxRepeaterItemStateChanged

    private void cboxSequencerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxSequencerItemStateChanged
        logger.debug("tool scope SEQUENCER enabled: ", evt.getStateChange() == ItemEvent.SELECTED);
        configuration.getToolScopes().put(AppConfiguration.TOOL_SEQUENCER, evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_cboxSequencerItemStateChanged

    private void cboxIntruderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxIntruderItemStateChanged
        logger.debug("tool scope INTRUDER enabled: ", evt.getStateChange() == ItemEvent.SELECTED);
        configuration.getToolScopes().put(AppConfiguration.TOOL_INTRUDER, evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_cboxIntruderItemStateChanged

    private void cboxScannerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxScannerItemStateChanged
        logger.debug("tool scope SCANNER enabled: ", evt.getStateChange() == ItemEvent.SELECTED);
        configuration.getToolScopes().put(AppConfiguration.TOOL_SCANNER, evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_cboxScannerItemStateChanged

    private void cboxExtenderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxExtenderItemStateChanged
        logger.debug("tool scope EXTENDER enabled: ", evt.getStateChange() == ItemEvent.SELECTED);
        configuration.getToolScopes().put(AppConfiguration.TOOL_EXTENDER, evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_cboxExtenderItemStateChanged

    private void cboxDecoderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxDecoderItemStateChanged
        logger.debug("tool scope DECODER enabled: ", evt.getStateChange() == ItemEvent.SELECTED);
        configuration.getToolScopes().put(AppConfiguration.TOOL_DECODER, evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_cboxDecoderItemStateChanged

    private void cboxComparerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxComparerItemStateChanged
        logger.debug("tool scope COMPARER enabled: ", evt.getStateChange() == ItemEvent.SELECTED);
        configuration.getToolScopes().put(AppConfiguration.TOOL_COMPARER, evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_cboxComparerItemStateChanged

    private void cboxSuiteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxSuiteItemStateChanged
        logger.debug("tool scope SUITE enabled: ", evt.getStateChange() == ItemEvent.SELECTED);
        configuration.getToolScopes().put(AppConfiguration.TOOL_SUITE, evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_cboxSuiteItemStateChanged

    private void btnClearLogsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearLogsActionPerformed
        taLogging.setText("");
    }//GEN-LAST:event_btnClearLogsActionPerformed

    private void btnApplyChangesInTabsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyChangesInTabsActionPerformed
        applyChangesInAllTabs();
    }//GEN-LAST:event_btnApplyChangesInTabsActionPerformed

    private void setLogLevel(int level) {
        switch (level) {
            case AppConfiguration.LOG_VERBOSITY_LEVEL_DEBUG:
                setLogLevel(Level.DEBUG);
                cmbLogVerbosityLevel.setSelectedIndex(0);
                break;
            case AppConfiguration.LOG_VERBOSITY_LEVEL_INFO:
                setLogLevel(Level.INFO);
                cmbLogVerbosityLevel.setSelectedIndex(1);
                break;
            case AppConfiguration.LOG_VERBOSITY_LEVEL_WARN:
                setLogLevel(Level.WARN);
                cmbLogVerbosityLevel.setSelectedIndex(2);
                break;
            case AppConfiguration.LOG_VERBOSITY_LEVEL_ERROR:
                setLogLevel(Level.ERROR);
                cmbLogVerbosityLevel.setSelectedIndex(3);
                break;
            default:
                logger.warn("Invalid log verbosity level. Using default level INFO instead");
                setLogLevel(Level.INFO);
                cmbLogVerbosityLevel.setSelectedIndex(1);
                break;
        }
    }

    private void setLogLevel(Level level) {
        try {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration conf = ctx.getConfiguration();
            LoggerConfig root = conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            root.setLevel(level);
            LoggerConfig named = conf.getLoggerConfig("com.cyradar.ui.CryptoMessagesHandler");
            Level currentLevel = named.getLevel();
            named.setLevel(level);
            logger.log(Level.OFF, "Log verbosity level change " + currentLevel.name() + " => " + level.name());
            ctx.updateLoggers(conf);
        } catch (Exception e) {
            logger.error("cannot set log level: ", e);
        }
    }

    private void cmbLogVerbosityLevelItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbLogVerbosityLevelItemStateChanged
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            return;
        }
        switch (evt.getItem().toString()) {
            case "DEBUG":
                setLogLevel(Level.DEBUG);
                configuration.setVerbosityLevel(AppConfiguration.LOG_VERBOSITY_LEVEL_DEBUG);
                break;
            case "INFO":
                setLogLevel(Level.INFO);
                configuration.setVerbosityLevel(AppConfiguration.LOG_VERBOSITY_LEVEL_INFO);
                break;
            case "WARN":
                setLogLevel(Level.WARN);
                configuration.setVerbosityLevel(AppConfiguration.LOG_VERBOSITY_LEVEL_WARN);
                break;
            case "ERROR":
                setLogLevel(Level.ERROR);
                configuration.setVerbosityLevel(AppConfiguration.LOG_VERBOSITY_LEVEL_ERROR);
                break;
        }
    }//GEN-LAST:event_cmbLogVerbosityLevelItemStateChanged

    private void btnLoadConfigurationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadConfigurationsActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter("JSON file (*.json)", "json");
        fileChooser.setFileFilter(jsonFilter);
        fileChooser.setMultiSelectionEnabled(false);
        int option = fileChooser.showOpenDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = fileChooser.getSelectedFile();
        try {
            Gson g = new Gson();
            AppConfiguration newConfiguration = g.fromJson(new FileReader(selectedFile), AppConfiguration.class);
            logger.info("read configuration from " + selectedFile.getAbsolutePath());
            setConfiguration(newConfiguration);
        } catch (Exception e) {
            logger.error("cannot read configuration file", e);
            JOptionPane.showMessageDialog(this, "Failed to load configuration file. Reason: " + e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnLoadConfigurationsActionPerformed

    private File getSelectedFileWithExtensions(File selectedFile, FileFilter filter) {
        if (filter instanceof FileNameExtensionFilter) {
            String[] exts = ((FileNameExtensionFilter) filter).getExtensions();
            String nameLower = selectedFile.getName();
            for (String ext : exts) {
                if (nameLower.endsWith("." + ext)) {
                    return selectedFile;
                }
            }
            return new File(selectedFile.toString() + "." + exts[0]);
        }
        return selectedFile;
    }

    private void btnSaveConfigurationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveConfigurationsActionPerformed
        applyChangesInAllTabs();
        JFileChooser fileChooser = new JFileChooser() {
            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                FileFilter currentFilter = getFileFilter();
                f = getSelectedFileWithExtensions(f, currentFilter);
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int confirm = JOptionPane.showConfirmDialog(this, f.getName() + " already exists.\nDo you want to replace it?", "Confirm Save As", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    switch (confirm) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter("JSON file (*.json)", "json");
        fileChooser.setFileFilter(jsonFilter);
        fileChooser.setMultiSelectionEnabled(false);
        int option = fileChooser.showSaveDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = fileChooser.getSelectedFile();
        FileFilter currentFilter = fileChooser.getFileFilter();
        selectedFile = getSelectedFileWithExtensions(selectedFile, currentFilter);
        Gson g = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        String json = g.toJson(configuration);
        try (
                FileWriter fw = new FileWriter(selectedFile);
                BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(json);
            logger.info("Successfully save configurations to " + selectedFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Successfully save configurations to " + selectedFile.getName(), "", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            logger.error("Failed to save configuration. Reason: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Failed to save configuration. Reason: " + e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveConfigurationsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApplyChangesInTabs;
    private javax.swing.JButton btnClearLogs;
    private javax.swing.JButton btnLoadConfigurations;
    private javax.swing.JButton btnSaveConfigurations;
    private javax.swing.JCheckBox cboxComparer;
    private javax.swing.JCheckBox cboxDecoder;
    private javax.swing.JCheckBox cboxExtender;
    private javax.swing.JCheckBox cboxIntruder;
    private javax.swing.JCheckBox cboxProxy;
    private javax.swing.JCheckBox cboxRepeater;
    private javax.swing.JCheckBox cboxScanner;
    private javax.swing.JCheckBox cboxSequencer;
    private javax.swing.JCheckBox cboxSpider;
    private javax.swing.JCheckBox cboxSuite;
    private javax.swing.JCheckBox cboxTarget;
    private javax.swing.JComboBox<String> cmbLogVerbosityLevel;
    private javax.swing.JPanel dummyPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane main;
    private javax.swing.JPanel optionsPane;
    private javax.swing.JTextArea taLogging;
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        try {
            if (e.getSource() instanceof JTabbedPane) {
                JTabbedPane pane = (JTabbedPane) e.getSource();
                int selectedIndex = pane.getSelectedIndex();
                // Check if the last tab is selected, create a new tab instead
                if (selectedIndex == pane.getTabCount() - 1) {
                    // set selected index to 0 to avoid infinity loop
                    pane.setSelectedIndex(0);
                    count += 1;
                    InterceptConfiguration interceptConfiguration = InterceptConfiguration.getDefault();
                    interceptConfiguration.setTitle(String.valueOf(count));
                    configuration.getInterceptConfigurations().add(interceptConfiguration);
                    addTab(String.valueOf(count), createNewTabCompo(interceptConfiguration));
                    pane.setSelectedIndex(selectedIndex);
                }
            }
        } catch (Exception ex) {
            logger.error("main change event listener: ", ex);
        }
    }

    public class InterceptConfigurationPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!(evt.getSource() instanceof InterceptConfigurationForm)) {
                return;
            }
            if (!(evt.getOldValue() instanceof InterceptConfiguration)) {
                return;
            }
            if (!(evt.getNewValue() instanceof InterceptConfiguration)) {
                return;
            }
            InterceptConfiguration oldValue = (InterceptConfiguration) evt.getOldValue();
            InterceptConfiguration newValue = (InterceptConfiguration) evt.getNewValue();
            List<InterceptConfiguration> interceptConfigurations = configuration.getInterceptConfigurations();
            int index = interceptConfigurations.indexOf(oldValue);
            interceptConfigurations.set(index, newValue);
            // TODO log configuration changes
            logger.debug(String.format("Configuration change: %s", new Gson().toJson(configuration)));
        }
    }

}
