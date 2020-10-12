/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyradar.ui;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import com.cyradar.ui.components.ClosableTabComponent;
import com.cyradar.ui.components.InterceptConfigurationForm;
import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author phinc27
 */
public final class CyInterceptor extends javax.swing.JPanel implements ChangeListener {

    private int count;

    /**
     * Creates new form CyInterceptor
     */
    public CyInterceptor() {
        initComponents();
        initOptionsTabComponent();
        initTabbedPaneKeyStrokes();
        main.addChangeListener(CyInterceptor.this);
    }

    public void addTab(Component component) {
        addTab(null, component);
    }

    public void addTab(String title, Component component) {
        int index = this.main.getTabCount() - 1;
        addTabAtIndex(title, component, index);
    }

    private void addTabAtIndex(String title, Component component, int index) {
        count += 1;
        title = title == null ? String.valueOf(count) : title;
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
            main.remove(removeIndex);
        });
        this.main.setTabComponentAt(index, titleTabComponent);
        this.main.setSelectedIndex(index);
    }

    private void initOptionsTabComponent() {
        JLabel optionsTabLbl = new JLabel("Options");
        optionsTabLbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Container parentContainer = SwingUtilities.getAncestorOfClass(JTabbedPane.class, (JLabel) e.getSource());
                if (parentContainer != null) {
                    JTabbedPane parent = (JTabbedPane) parentContainer;
                    MouseEvent parentEvent = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, parent);
                    parent.dispatchEvent(parentEvent);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Container parentContainer = SwingUtilities.getAncestorOfClass(JTabbedPane.class, (JLabel) e.getSource());
                if (parentContainer != null) {
                    JTabbedPane parent = (JTabbedPane) parentContainer;
                    MouseEvent parentEvent = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, parent);
                    parent.dispatchEvent(parentEvent);
                }
            }
        });
        optionsTabLbl.setBorder(BorderFactory.createEmptyBorder(6, 2, 6, 2));
        this.main.setTabComponentAt(0, optionsTabLbl);
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
                main.remove(removeIndex);
            }
        });
        main.getActionMap().put("newTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentIndex = main.getSelectedIndex();
                addTabAtIndex(null, createNewTabCompo(), currentIndex + 1);
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
                addTabAtIndex(title + " - clone", createNewTabCompo(), currentIndex + 1);
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
                main.add(component, nextIndex);
                main.setTabComponentAt(nextIndex, tabComponent);
                main.setSelectedIndex(nextIndex);
            }
        });
    }

    private Component createNewTabCompo() {
        return new InterceptConfigurationForm();
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
        cboxInstruder = new javax.swing.JCheckBox();
        cboxScanner = new javax.swing.JCheckBox();
        cboxExtender = new javax.swing.JCheckBox();
        cboxDecoder = new javax.swing.JCheckBox();
        cboxComparer = new javax.swing.JCheckBox();
        cboxSuite = new javax.swing.JCheckBox();
        switch1 = new com.cyradar.ui.components.Switch();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
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

        cboxInstruder.setSelected(true);
        cboxInstruder.setText("Instruder");
        cboxInstruder.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboxInstruderItemStateChanged(evt);
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
                            .addComponent(cboxInstruder)
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
                    .addComponent(cboxInstruder)
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

        switch1.setText("switch1");
        switch1.setOnOff(false);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel1.setText("Logs");

        javax.swing.GroupLayout optionsPaneLayout = new javax.swing.GroupLayout(optionsPane);
        optionsPane.setLayout(optionsPaneLayout);
        optionsPaneLayout.setHorizontalGroup(
            optionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionsPaneLayout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 730, Short.MAX_VALUE)
                        .addComponent(switch1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(optionsPaneLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        optionsPaneLayout.setVerticalGroup(
            optionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(switch1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
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

    }//GEN-LAST:event_cboxProxyItemStateChanged

    private void cboxTargetItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxTargetItemStateChanged

    }//GEN-LAST:event_cboxTargetItemStateChanged

    private void cboxSpiderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxSpiderItemStateChanged

    }//GEN-LAST:event_cboxSpiderItemStateChanged

    private void cboxRepeaterItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxRepeaterItemStateChanged

    }//GEN-LAST:event_cboxRepeaterItemStateChanged

    private void cboxSequencerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxSequencerItemStateChanged

    }//GEN-LAST:event_cboxSequencerItemStateChanged

    private void cboxInstruderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxInstruderItemStateChanged

    }//GEN-LAST:event_cboxInstruderItemStateChanged

    private void cboxScannerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxScannerItemStateChanged

    }//GEN-LAST:event_cboxScannerItemStateChanged

    private void cboxExtenderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxExtenderItemStateChanged
        int x = IBurpExtenderCallbacks.TOOL_COMPARER;
    }//GEN-LAST:event_cboxExtenderItemStateChanged

    private void cboxDecoderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxDecoderItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cboxDecoderItemStateChanged

    private void cboxComparerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxComparerItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cboxComparerItemStateChanged

    private void cboxSuiteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboxSuiteItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cboxSuiteItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cboxComparer;
    private javax.swing.JCheckBox cboxDecoder;
    private javax.swing.JCheckBox cboxExtender;
    private javax.swing.JCheckBox cboxInstruder;
    private javax.swing.JCheckBox cboxProxy;
    private javax.swing.JCheckBox cboxRepeater;
    private javax.swing.JCheckBox cboxScanner;
    private javax.swing.JCheckBox cboxSequencer;
    private javax.swing.JCheckBox cboxSpider;
    private javax.swing.JCheckBox cboxSuite;
    private javax.swing.JCheckBox cboxTarget;
    private javax.swing.JPanel dummyPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTabbedPane main;
    private javax.swing.JPanel optionsPane;
    private com.cyradar.ui.components.Switch switch1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        try {
            if (e.getSource() instanceof JTabbedPane) {
                JTabbedPane pane = (JTabbedPane) e.getSource();
                int selectedIndex = pane.getSelectedIndex();
                // Check if the last tab is selected, create a new tab instead
                if (selectedIndex == pane.getTabCount() - 1) {
                    // set selected index to -1 to avoid infinity loop
                    pane.setSelectedIndex(-1);
                    addTab(null, createNewTabCompo());
                    pane.setSelectedIndex(selectedIndex);
                }
            }
        } catch (Exception ex) {
            BurpExtender.callbacks.printError("[Error] main change event listener: " + ex.getMessage());
        }
    }

}
