/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyradar.ui.components;

import com.cyradar.common.Utils;
import com.cyradar.models.InterceptStage;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.text.JTextComponent;

/**
 *
 * @author phinc27
 */
public class InterceptStageForm extends javax.swing.JPanel {

    /**
     * Creates new form TransformerPanel
     */
    public InterceptStageForm() {
        initComponents();
        cmbEncodingMode.setModel(new DefaultComboBoxModel<>(new String[]{InterceptStage.MODE_ENCODE, InterceptStage.MODE_DECODE}));
        cmbEncodingCharset.setModel(new DefaultComboBoxModel<>(new String[]{InterceptStage.CHARSET_UTF8, InterceptStage.CHARSET_ASCII}));
        cmbAesAlgorithms.setModel(new DefaultComboBoxModel<>(new String[]{InterceptStage.ALG_AES_EBC_PKCS5_PADDING, InterceptStage.ALG_AES_CBC_PKCS5_PADDING, InterceptStage.ALG_AES_CTR_NOPADDING}));
        cmbAesMode.setModel(new DefaultComboBoxModel<>(new String[]{InterceptStage.MODE_ENCRYPT, InterceptStage.MODE_DECRYPT}));
        cmbAesCharset.setModel(new DefaultComboBoxModel<>(new String[]{InterceptStage.CHARSET_UTF8, InterceptStage.CHARSET_ASCII}));
        cbIsIVFixed.setSelected(false);
        cbIsIVFixed.setVisible(false);
        tfAesIv.setVisible(false);
        rbAesIvRaw.setVisible(false);
        rbAesIvHex.setVisible(false);
        rbAesIvBase64.setVisible(false);
        lblIVPos.setVisible(false);
        cmbAesIvPos.setVisible(false);
        cmbAesIvPos.setModel(new DefaultComboBoxModel<>(new String[]{InterceptStage.IV_POSITION_PREFIX, InterceptStage.IV_POSITION_POSTFIX, InterceptStage.IV_POSITION_NONE}));
        cmbAesCipherEncoding.setModel(new DefaultComboBoxModel<>(new String[]{InterceptStage.CIPHER_ENCODING_HEX, InterceptStage.CIPHER_ENCODING_BASE64}));
        cmbRsaMode.setModel(new DefaultComboBoxModel<>(new String[]{InterceptStage.MODE_ENCRYPT, InterceptStage.MODE_DECRYPT}));
        cmbRsaCharset.setModel(new DefaultComboBoxModel<>(new String[]{InterceptStage.CHARSET_UTF8, InterceptStage.CHARSET_ASCII}));
        cmbRsaCipherEncoding.setModel(new DefaultComboBoxModel<>(new String[]{InterceptStage.CIPHER_ENCODING_HEX, InterceptStage.CIPHER_ENCODING_BASE64}));
        RBAlgCategoryItemListener categoryItemListener = new RBAlgCategoryItemListener();
        cmbHashAlgorithms.setModel(new DefaultComboBoxModel<>(new String[]{InterceptStage.ALG_MD5, InterceptStage.ALG_SHA1, InterceptStage.ALG_SHA256, InterceptStage.ALG_SHA384, InterceptStage.ALG_SHA512}));
        cmbHashCharset.setModel(new DefaultComboBoxModel<>(new String[]{InterceptStage.CHARSET_UTF8, InterceptStage.CHARSET_ASCII}));
        rbCategoryEncoding.addItemListener(categoryItemListener);
        rbCategoryEncryption.addItemListener(categoryItemListener);
        rbCategoryHashing.addItemListener(categoryItemListener);
        RBEncryptionAlgItemListener encryptionAlgItemListener = new RBEncryptionAlgItemListener();
        rbAlgAES.addItemListener(encryptionAlgItemListener);
        rbAlgRSA.addItemListener(encryptionAlgItemListener);
        RBFormatItemStateChange rBFormatItemStateChange = new RBFormatItemStateChange();
        rbAesSecretRaw.addItemListener(rBFormatItemStateChange);
        rbAesSecretHex.addItemListener(rBFormatItemStateChange);
        rbAesSecretBase64.addItemListener(rBFormatItemStateChange);
        rbAesIvRaw.addItemListener(rBFormatItemStateChange);
        rbAesIvHex.addItemListener(rBFormatItemStateChange);
        rbAesIvBase64.addItemListener(rBFormatItemStateChange);
        rbRsaKeyRaw.addItemListener(rBFormatItemStateChange);
        rbRsaKeyHex.addItemListener(rBFormatItemStateChange);
        rbRsaKeyBase64.addItemListener(rBFormatItemStateChange);
    }

    public void setInterceptStage(InterceptStage model) {
        try {
            String alg = (String) model.get(InterceptStage.CTX_ALGORITHM);
            CardLayout mainLayout = (CardLayout) algCategoryPanel.getLayout();
            switch (alg) {
                case InterceptStage.ALG_BASE64:
                    clearEncryptionPanel();
                    clearHashingPanel();
                    rbCategoryEncoding.setSelected(true);
                    rbEncodingBase64Alg.setSelected(true);
                    mainLayout.show(algCategoryPanel, "algEncodingPanel");
                    cmbEncodingMode.setSelectedItem(model.get(InterceptStage.CTX_MODE));
                    cmbEncodingCharset.setSelectedItem(model.get(InterceptStage.CTX_CHARSET));
                    break;
                case InterceptStage.ALG_HEX:
                    clearEncryptionPanel();
                    clearHashingPanel();
                    rbCategoryEncoding.setSelected(true);
                    rbEncodingHexAlg.setSelected(true);
                    mainLayout.show(algCategoryPanel, "algEncodingPanel");
                    cmbEncodingMode.setSelectedItem(model.get(InterceptStage.CTX_MODE));
                    cmbEncodingCharset.setSelectedItem(model.get(InterceptStage.CTX_CHARSET));
                    break;
                case InterceptStage.ALG_AES_CBC_PKCS5_PADDING:
                case InterceptStage.ALG_AES_EBC_PKCS5_PADDING:
                case InterceptStage.ALG_AES_CTR_NOPADDING:
                    clearEncodingPanel();
                    clearRSAPanel();
                    clearHashingPanel();
                    rbCategoryEncryption.setSelected(true);
                    rbAlgAES.setSelected(true);
                    mainLayout.show(algCategoryPanel, "algEncryptionPanel");
                    cmbAesAlgorithms.setSelectedItem(alg);
                    cmbAesMode.setSelectedItem(model.get(InterceptStage.CTX_MODE));
                    cmbAesCharset.setSelectedItem(model.get(InterceptStage.CTX_CHARSET));
                    tfAesSecret.setText((String) model.get(InterceptStage.CTX_KEY));
                    String keyFormat = (String) model.get(InterceptStage.CTX_KEY_FORMAT);
                    rbAesSecretRaw.setSelected(keyFormat.equals(InterceptStage.FORMAT_RAW));
                    rbAesSecretHex.setSelected(keyFormat.equals(InterceptStage.FORMAT_HEX));
                    rbAesSecretBase64.setSelected(keyFormat.equals(InterceptStage.FORMAT_BASE64));

                    if (alg.equals(InterceptStage.ALG_AES_EBC_PKCS5_PADDING)) {
                        cbIsIVFixed.setVisible(false);
                        lblIVPos.setVisible(false);
                        cmbAesIvPos.setVisible(false);
                    } else {
                        cbIsIVFixed.setVisible(true);
                        lblIVPos.setVisible(true);
                        cmbAesIvPos.setVisible(true);
                        Boolean isIvFixed = (Boolean) model.get(InterceptStage.CTX_IV_FIXED);
                        if (isIvFixed) {
                            cbIsIVFixed.setSelected(true);
                            tfAesIv.setVisible(true);
                            rbAesIvRaw.setVisible(true);
                            rbAesIvHex.setVisible(true);
                            rbAesIvBase64.setVisible(true);
                            String ivFormat = (String) model.get(InterceptStage.CTX_IV_FORMAT);
                            rbAesIvRaw.setSelected(ivFormat.equals(InterceptStage.FORMAT_RAW));
                            rbAesIvHex.setSelected(ivFormat.equals(InterceptStage.FORMAT_HEX));
                            rbAesIvBase64.setSelected(ivFormat.equals(InterceptStage.FORMAT_BASE64));
                        } else {
                            cbIsIVFixed.setSelected(false);
                            tfAesIv.setVisible(false);
                            rbAesIvRaw.setVisible(false);
                            rbAesIvHex.setVisible(false);
                            rbAesIvBase64.setVisible(false);
                        }
                        cmbAesIvPos.setSelectedItem(model.get(InterceptStage.CTX_IV_POSITION));
                    }
                    cmbAesCipherEncoding.setSelectedItem(model.get(InterceptStage.CTX_CIPHER_ENCODING));
                    break;
                case InterceptStage.ALG_RSA_ECB_PKCS1PADDING:
                    clearEncodingPanel();
                    clearAESPanel();
                    clearHashingPanel();
                    rbCategoryEncryption.setSelected(true);
                    rbAlgRSA.setSelected(true);
                    mainLayout.show(algCategoryPanel, "algEncryptionPanel");
                    cmbRsaMode.setSelectedItem(model.get(InterceptStage.CTX_MODE));
                    cmbRsaCharset.setSelectedItem(model.get(InterceptStage.CTX_CHARSET));
                    cmbRsaCipherEncoding.setSelectedItem(model.get(InterceptStage.CTX_CIPHER_ENCODING));
                    taRsaKey.setText((String) model.get(InterceptStage.CTX_KEY));
                    String rsaKeyFormat = (String) model.get(InterceptStage.CTX_KEY_FORMAT);
                    rbRsaKeyRaw.setSelected(rsaKeyFormat.equals(InterceptStage.FORMAT_RAW));
                    rbRsaKeyHex.setSelected(rsaKeyFormat.equals(InterceptStage.FORMAT_HEX));
                    rbRsaKeyBase64.setSelected(rsaKeyFormat.equals(InterceptStage.FORMAT_BASE64));
                    break;
                case InterceptStage.ALG_MD5:
                case InterceptStage.ALG_SHA1:
                case InterceptStage.ALG_SHA256:
                case InterceptStage.ALG_SHA384:
                case InterceptStage.ALG_SHA512:
                    clearEncodingPanel();
                    clearEncryptionPanel();
                    rbCategoryHashing.setSelected(true);
                    mainLayout.show(algCategoryPanel, "algHashingPanel");
                    cmbHashAlgorithms.setSelectedItem(alg);
                    cmbHashCharset.setSelectedItem(model.get(InterceptStage.CTX_CHARSET));
                    break;
                default:
                    throw new Exception("Unsupported algorithm " + alg);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to set stage model: " + e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
        }
    }

    public InterceptStage getInterceptStage() {
        InterceptStage stage = new InterceptStage();
        if (rbCategoryEncoding.isSelected()) {
            String alg = rbEncodingHexAlg.isSelected() ? InterceptStage.ALG_HEX : InterceptStage.ALG_BASE64;
            stage.put(InterceptStage.CTX_ALGORITHM, alg);
            stage.put(InterceptStage.CTX_MODE, (String) cmbEncodingMode.getSelectedItem());
            stage.put(InterceptStage.CTX_CHARSET, (String) cmbEncodingCharset.getSelectedItem());
        } else if (rbCategoryEncryption.isSelected()) {
            // AES algorithm
            if (rbAlgAES.isSelected()) {
                String alg = (String) cmbAesAlgorithms.getSelectedItem();
                stage.put(InterceptStage.CTX_ALGORITHM, alg);
                stage.put(InterceptStage.CTX_MODE, (String) cmbAesMode.getSelectedItem());
                stage.put(InterceptStage.CTX_CHARSET, (String) cmbAesCharset.getSelectedItem());
                stage.put(InterceptStage.CTX_KEY, tfAesSecret.getText());
                String keyFormat = rbAesSecretHex.isSelected() ? InterceptStage.FORMAT_HEX : (rbAesSecretBase64.isSelected() ? InterceptStage.FORMAT_BASE64 : InterceptStage.FORMAT_RAW);
                stage.put(InterceptStage.CTX_KEY_FORMAT, keyFormat);
                if (alg.equals(InterceptStage.ALG_AES_EBC_PKCS5_PADDING)) {
                    // EBC mode does not use IV
                } else {
                    if (cbIsIVFixed.isSelected()) {
                        stage.put(InterceptStage.CTX_IV_FIXED, true);
                        stage.put(InterceptStage.CTX_IV, tfAesSecret.getText());
                        String ivFormat = rbAesIvHex.isSelected() ? InterceptStage.FORMAT_HEX : (rbAesIvBase64.isSelected() ? InterceptStage.FORMAT_BASE64 : InterceptStage.FORMAT_RAW);
                        stage.put(InterceptStage.CTX_IV_FORMAT, ivFormat);
                    } else {
                        stage.put(InterceptStage.CTX_IV_FIXED, false);
                    }
                    stage.put(InterceptStage.CTX_IV_POSITION, (String) cmbAesIvPos.getSelectedItem());
                }
                stage.put(InterceptStage.CTX_CIPHER_ENCODING, (String) cmbAesCipherEncoding.getSelectedItem());
            } else {
                // RSA
                stage.put(InterceptStage.CTX_ALGORITHM, InterceptStage.ALG_RSA_ECB_PKCS1PADDING);
                stage.put(InterceptStage.CTX_MODE, (String) cmbRsaMode.getSelectedItem());
                stage.put(InterceptStage.CTX_CHARSET, (String) cmbRsaCharset.getSelectedItem());
                stage.put(InterceptStage.CTX_KEY, taRsaKey.getText());
                String keyFormat = rbRsaKeyHex.isSelected() ? InterceptStage.FORMAT_HEX : (rbRsaKeyBase64.isSelected() ? InterceptStage.FORMAT_BASE64 : InterceptStage.FORMAT_RAW);
                stage.put(InterceptStage.CTX_KEY_FORMAT, keyFormat);
                stage.put(InterceptStage.CTX_CIPHER_ENCODING, (String) cmbRsaCipherEncoding.getSelectedItem());
            }
        } else {
            stage.put(InterceptStage.CTX_ALGORITHM, (String) cmbHashAlgorithms.getSelectedItem());
            stage.put(InterceptStage.CTX_CHARSET, (String) cmbHashCharset.getSelectedItem());
        }
        return stage;
    }

    private void clearEncodingPanel() {
        rbEncodingBase64Alg.setSelected(true);
        cmbEncodingCharset.setSelectedIndex(0);
        cmbEncodingMode.setSelectedIndex(0);
    }
    private void clearAESPanel() {
        cmbAesAlgorithms.setSelectedIndex(0);
        cmbAesMode.setSelectedIndex(0);
        cmbAesCharset.setSelectedIndex(0);
        tfAesSecret.setText("");
        rbAesSecretRaw.setSelected(true);
        cbIsIVFixed.setSelected(false);
        cbIsIVFixed.setVisible(false);
        tfAesIv.setText("");
        tfAesIv.setVisible(false);
        rbAesIvRaw.setSelected(true);
        rbAesIvRaw.setVisible(false);
        rbAesIvHex.setVisible(false);
        rbAesIvBase64.setVisible(false);
        cmbAesIvPos.setSelectedIndex(0);
        cmbAesCipherEncoding.setSelectedIndex(0);
    }
    
    private void clearRSAPanel() {
        cmbRsaMode.setSelectedIndex(0);
        cmbRsaCharset.setSelectedIndex(0);
        cmbRsaCipherEncoding.setSelectedIndex(0);
        rbRsaKeyRaw.setSelected(true);
        taRsaKey.setText("");
    }
    
    private void clearHashingPanel() {
        cmbHashAlgorithms.setSelectedIndex(0);
        cmbHashCharset.setSelectedIndex(0);
    }
    
    private void clearEncryptionPanel() {
        rbAlgAES.setSelected(true);
        CardLayout layout = (CardLayout) algEncryptionOptions.getLayout();
        clearAESPanel();
        clearRSAPanel();
        layout.show(algEncryptionOptions, "AESOptionsPanel");
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgMode = new javax.swing.ButtonGroup();
        bgEncodingCipher = new javax.swing.ButtonGroup();
        bgEncryptionCipher = new javax.swing.ButtonGroup();
        bgAesSecretFormat = new javax.swing.ButtonGroup();
        bgAesIvFormat = new javax.swing.ButtonGroup();
        bgRsaKeyFormat = new javax.swing.ButtonGroup();
        algCategoryPanel = new javax.swing.JPanel();
        algEncodingPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rbEncodingBase64Alg = new javax.swing.JRadioButton();
        rbEncodingHexAlg = new javax.swing.JRadioButton();
        cmbEncodingMode = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        cmbEncodingCharset = new javax.swing.JComboBox<>();
        algEncryptionPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        rbAlgAES = new javax.swing.JRadioButton();
        rbAlgRSA = new javax.swing.JRadioButton();
        algEncryptionOptions = new javax.swing.JPanel();
        AESOptionsPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        cmbAesAlgorithms = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        cmbAesMode = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        tfAesSecret = new javax.swing.JTextField();
        rbAesSecretRaw = new javax.swing.JRadioButton();
        rbAesSecretHex = new javax.swing.JRadioButton();
        rbAesSecretBase64 = new javax.swing.JRadioButton();
        tfAesIv = new javax.swing.JTextField();
        rbAesIvRaw = new javax.swing.JRadioButton();
        rbAesIvHex = new javax.swing.JRadioButton();
        rbAesIvBase64 = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        cmbAesCharset = new javax.swing.JComboBox<>();
        lblIVPos = new javax.swing.JLabel();
        cmbAesIvPos = new javax.swing.JComboBox<>();
        lblAESCipherEncoding = new javax.swing.JLabel();
        cmbAesCipherEncoding = new javax.swing.JComboBox<>();
        cbIsIVFixed = new javax.swing.JCheckBox();
        RSAOptionsPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        cmbRsaMode = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        cmbRsaCharset = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        rbRsaKeyRaw = new javax.swing.JRadioButton();
        rbRsaKeyHex = new javax.swing.JRadioButton();
        rbRsaKeyBase64 = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        taRsaKey = new javax.swing.JTextArea();
        lblRSACipherEncoding = new javax.swing.JLabel();
        cmbRsaCipherEncoding = new javax.swing.JComboBox<>();
        btnImportRsaKey = new javax.swing.JButton();
        algHashingPanel = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        cmbHashAlgorithms = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        cmbHashCharset = new javax.swing.JComboBox<>();
        rbCategoryEncoding = new javax.swing.JRadioButton();
        rbCategoryEncryption = new javax.swing.JRadioButton();
        rbCategoryHashing = new javax.swing.JRadioButton();

        algCategoryPanel.setLayout(new java.awt.CardLayout());

        jLabel1.setText("Select:");

        bgEncodingCipher.add(rbEncodingBase64Alg);
        rbEncodingBase64Alg.setSelected(true);
        rbEncodingBase64Alg.setText("Base64");

        bgEncodingCipher.add(rbEncodingHexAlg);
        rbEncodingHexAlg.setText("Hexadecimal");

        cmbEncodingMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Encode", "Decode" }));

        jLabel10.setText("Mode:");

        jLabel11.setText("Charset:");

        cmbEncodingCharset.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "UTF-8", "ASCII" }));

        javax.swing.GroupLayout algEncodingPanelLayout = new javax.swing.GroupLayout(algEncodingPanel);
        algEncodingPanel.setLayout(algEncodingPanelLayout);
        algEncodingPanelLayout.setHorizontalGroup(
            algEncodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algEncodingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(algEncodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel10)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(algEncodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(algEncodingPanelLayout.createSequentialGroup()
                        .addComponent(rbEncodingBase64Alg)
                        .addGap(12, 12, 12)
                        .addComponent(rbEncodingHexAlg))
                    .addComponent(cmbEncodingMode, 0, 250, Short.MAX_VALUE)
                    .addComponent(cmbEncodingCharset, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        algEncodingPanelLayout.setVerticalGroup(
            algEncodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algEncodingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(algEncodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(rbEncodingBase64Alg)
                    .addComponent(rbEncodingHexAlg))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(algEncodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(cmbEncodingMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(algEncodingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(cmbEncodingCharset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        algCategoryPanel.add(algEncodingPanel, "algEncodingPanel");

        jLabel2.setText("Algorithm:");

        bgEncryptionCipher.add(rbAlgAES);
        rbAlgAES.setSelected(true);
        rbAlgAES.setText("Advanced Encryption Standard (AES)");

        bgEncryptionCipher.add(rbAlgRSA);
        rbAlgRSA.setText("RSA");

        algEncryptionOptions.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        algEncryptionOptions.setLayout(new java.awt.CardLayout());

        jLabel3.setText("Mode of operation:");

        cmbAesAlgorithms.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Electronic codebook (ECB)", "Cipher block chaining (CBC)", "Counter (CTR) - No padding" }));
        cmbAesAlgorithms.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbAesAlgorithmsItemStateChanged(evt);
            }
        });

        jLabel4.setText("Mode:");

        cmbAesMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Encrypt", "Decrypt" }));

        jLabel5.setText("Secret:");

        bgAesSecretFormat.add(rbAesSecretRaw);
        rbAesSecretRaw.setSelected(true);
        rbAesSecretRaw.setText("Raw");

        bgAesSecretFormat.add(rbAesSecretHex);
        rbAesSecretHex.setText("Hex");

        bgAesSecretFormat.add(rbAesSecretBase64);
        rbAesSecretBase64.setText("Base64");

        bgAesIvFormat.add(rbAesIvRaw);
        rbAesIvRaw.setSelected(true);
        rbAesIvRaw.setText("Raw");

        bgAesIvFormat.add(rbAesIvHex);
        rbAesIvHex.setText("Hex");

        bgAesIvFormat.add(rbAesIvBase64);
        rbAesIvBase64.setText("Base64");

        jLabel7.setText("Charset:");

        cmbAesCharset.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "UTF-8", "ASCII" }));

        lblIVPos.setText("IV Position:");

        cmbAesIvPos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Prefix", "Postfix", "None" }));

        lblAESCipherEncoding.setText("Cipher encoding:");

        cmbAesCipherEncoding.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hexadecimal", "Base64" }));

        cbIsIVFixed.setText("Use fixed Initialization vector (IV)");
        cbIsIVFixed.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbIsIVFixedItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout AESOptionsPanelLayout = new javax.swing.GroupLayout(AESOptionsPanel);
        AESOptionsPanel.setLayout(AESOptionsPanelLayout);
        AESOptionsPanelLayout.setHorizontalGroup(
            AESOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AESOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AESOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel7)
                    .addComponent(jLabel5)
                    .addComponent(lblIVPos)
                    .addComponent(lblAESCipherEncoding))
                .addGap(18, 18, 18)
                .addGroup(AESOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tfAesIv)
                    .addComponent(cmbAesIvPos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbAesCipherEncoding, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbIsIVFixed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tfAesSecret)
                    .addComponent(cmbAesCharset, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbAesMode, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbAesAlgorithms, 0, 250, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(AESOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AESOptionsPanelLayout.createSequentialGroup()
                        .addComponent(rbAesSecretRaw)
                        .addGap(18, 18, 18)
                        .addComponent(rbAesSecretHex)
                        .addGap(18, 18, 18)
                        .addComponent(rbAesSecretBase64))
                    .addGroup(AESOptionsPanelLayout.createSequentialGroup()
                        .addComponent(rbAesIvRaw)
                        .addGap(18, 18, 18)
                        .addComponent(rbAesIvHex)
                        .addGap(18, 18, 18)
                        .addComponent(rbAesIvBase64)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        AESOptionsPanelLayout.setVerticalGroup(
            AESOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AESOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AESOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cmbAesAlgorithms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AESOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cmbAesMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AESOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cmbAesCharset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AESOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfAesSecret, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(rbAesSecretRaw)
                    .addComponent(rbAesSecretHex)
                    .addComponent(rbAesSecretBase64))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbIsIVFixed)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AESOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfAesIv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rbAesIvRaw)
                    .addComponent(rbAesIvHex)
                    .addComponent(rbAesIvBase64))
                .addGap(4, 4, 4)
                .addGroup(AESOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbAesIvPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblIVPos))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(AESOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbAesCipherEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAESCipherEncoding))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        algEncryptionOptions.add(AESOptionsPanel, "AESOptionsPanel");

        jLabel6.setText("Mode:");

        cmbRsaMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Encrypt", "Decrypt" }));

        jLabel8.setText("Charset:");

        cmbRsaCharset.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "UTF-8", "ASCII" }));

        jLabel9.setText("Key:");

        bgRsaKeyFormat.add(rbRsaKeyRaw);
        rbRsaKeyRaw.setSelected(true);
        rbRsaKeyRaw.setText("Raw");

        bgRsaKeyFormat.add(rbRsaKeyHex);
        rbRsaKeyHex.setText("Hex");

        bgRsaKeyFormat.add(rbRsaKeyBase64);
        rbRsaKeyBase64.setText("Base64");

        taRsaKey.setColumns(20);
        taRsaKey.setRows(5);
        jScrollPane1.setViewportView(taRsaKey);

        lblRSACipherEncoding.setText("Cipher encoding:");

        cmbRsaCipherEncoding.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hexadecimal", "Base64" }));

        btnImportRsaKey.setText("Import");
        btnImportRsaKey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportRsaKeyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout RSAOptionsPanelLayout = new javax.swing.GroupLayout(RSAOptionsPanel);
        RSAOptionsPanel.setLayout(RSAOptionsPanelLayout);
        RSAOptionsPanelLayout.setHorizontalGroup(
            RSAOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RSAOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RSAOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RSAOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(RSAOptionsPanelLayout.createSequentialGroup()
                            .addGroup(RSAOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel6)
                                .addComponent(jLabel8)
                                .addComponent(lblRSACipherEncoding)
                                .addComponent(jLabel9))
                            .addGap(31, 31, 31)
                            .addGroup(RSAOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(cmbRsaCharset, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbRsaMode, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbRsaCipherEncoding, 0, 250, Short.MAX_VALUE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, RSAOptionsPanelLayout.createSequentialGroup()
                                    .addComponent(rbRsaKeyRaw)
                                    .addGap(18, 18, 18)
                                    .addComponent(rbRsaKeyHex)
                                    .addGap(18, 18, 18)
                                    .addComponent(rbRsaKeyBase64)))))
                    .addComponent(btnImportRsaKey, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        RSAOptionsPanelLayout.setVerticalGroup(
            RSAOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RSAOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RSAOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cmbRsaMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RSAOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(cmbRsaCharset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RSAOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbRsaCipherEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRSACipherEncoding))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RSAOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(rbRsaKeyRaw)
                    .addComponent(rbRsaKeyHex)
                    .addComponent(rbRsaKeyBase64))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnImportRsaKey)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        algEncryptionOptions.add(RSAOptionsPanel, "RSAOptionsPanel");

        javax.swing.GroupLayout algEncryptionPanelLayout = new javax.swing.GroupLayout(algEncryptionPanel);
        algEncryptionPanel.setLayout(algEncryptionPanelLayout);
        algEncryptionPanelLayout.setHorizontalGroup(
            algEncryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algEncryptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(algEncryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(algEncryptionOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(algEncryptionPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbAlgAES)
                        .addGap(18, 18, 18)
                        .addComponent(rbAlgRSA)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        algEncryptionPanelLayout.setVerticalGroup(
            algEncryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algEncryptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(algEncryptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(rbAlgAES)
                    .addComponent(rbAlgRSA))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(algEncryptionOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        algCategoryPanel.add(algEncryptionPanel, "algEncryptionPanel");

        jLabel12.setText("Algorithm:");

        cmbHashAlgorithms.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "MD5", "SHA-1", "SHA-256", "SHA-384", "SHA-512" }));

        jLabel13.setText("Charset:");

        cmbHashCharset.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "UTF-8", "ASCII" }));

        javax.swing.GroupLayout algHashingPanelLayout = new javax.swing.GroupLayout(algHashingPanel);
        algHashingPanel.setLayout(algHashingPanelLayout);
        algHashingPanelLayout.setHorizontalGroup(
            algHashingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algHashingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(algHashingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(algHashingPanelLayout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(18, 18, 18)
                        .addGroup(algHashingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cmbHashCharset, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbHashAlgorithms, 0, 250, Short.MAX_VALUE)))
                    .addComponent(jLabel13))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        algHashingPanelLayout.setVerticalGroup(
            algHashingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algHashingPanelLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(algHashingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbHashAlgorithms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(algHashingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(cmbHashCharset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        algCategoryPanel.add(algHashingPanel, "algHashingPanel");

        bgMode.add(rbCategoryEncoding);
        rbCategoryEncoding.setSelected(true);
        rbCategoryEncoding.setText("Encode/Decode");

        bgMode.add(rbCategoryEncryption);
        rbCategoryEncryption.setText("Encrypt/Decrypt");

        bgMode.add(rbCategoryHashing);
        rbCategoryHashing.setText("Hash");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(algCategoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rbCategoryEncoding)
                        .addGap(18, 18, 18)
                        .addComponent(rbCategoryEncryption)
                        .addGap(18, 18, 18)
                        .addComponent(rbCategoryHashing)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbCategoryEncoding)
                    .addComponent(rbCategoryEncryption)
                    .addComponent(rbCategoryHashing))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(algCategoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnImportRsaKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportRsaKeyActionPerformed
        JFileChooser fc = new JFileChooser();
        int option = fc.showOpenDialog(this);
        fc.setMultiSelectionEnabled(false);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            try {
                byte[] data = Files.readAllBytes(selectedFile.toPath());
                taRsaKey.setText(new String(data, "UTF-8"));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error importing key. Reason: " + e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnImportRsaKeyActionPerformed

    private void cmbAesAlgorithmsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbAesAlgorithmsItemStateChanged
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            return;
        }
        String alg = (String) cmbAesAlgorithms.getSelectedItem();
        if (alg.equals(InterceptStage.ALG_AES_EBC_PKCS5_PADDING)) {
            cbIsIVFixed.setVisible(false);
            tfAesIv.setVisible(false);
            rbAesIvRaw.setVisible(false);
            rbAesIvHex.setVisible(false);
            rbAesIvBase64.setVisible(false);
            lblIVPos.setVisible(false);
            cmbAesIvPos.setVisible(false);
        } else {
            cbIsIVFixed.setVisible(true);
            lblIVPos.setVisible(true);
            cmbAesIvPos.setVisible(true);
            if (cbIsIVFixed.isSelected()) {
                tfAesIv.setVisible(true);
                rbAesIvRaw.setVisible(true);
                rbAesIvHex.setVisible(true);
                rbAesIvBase64.setVisible(true);
            } else {
                tfAesIv.setVisible(false);
                rbAesIvRaw.setVisible(false);
                rbAesIvHex.setVisible(false);
                rbAesIvBase64.setVisible(false);
            }
        }
    }//GEN-LAST:event_cmbAesAlgorithmsItemStateChanged

    private void cbIsIVFixedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbIsIVFixedItemStateChanged
        boolean visible = evt.getStateChange() == ItemEvent.SELECTED;
        tfAesIv.setVisible(visible);
        rbAesIvRaw.setVisible(visible);
        rbAesIvHex.setVisible(visible);
        rbAesIvBase64.setVisible(visible);
    }//GEN-LAST:event_cbIsIVFixedItemStateChanged

    public void setComponentEnabled(boolean enabled) {
        // algorithm categories
        rbCategoryEncoding.setEnabled(enabled);
        rbCategoryEncryption.setEnabled(enabled);
        rbCategoryHashing.setEnabled(enabled);
        // encoding panel
        rbEncodingBase64Alg.setEnabled(enabled);
        rbEncodingHexAlg.setEnabled(enabled);
        cmbEncodingMode.setEnabled(enabled);
        cmbEncodingCharset.setEnabled(enabled);
        // encrypting panel
        rbAlgAES.setEnabled(enabled);
        rbAlgRSA.setEnabled(enabled);
        // aes
        cmbAesAlgorithms.setEnabled(enabled);
        cmbAesMode.setEnabled(enabled);
        cmbAesCharset.setEnabled(enabled);
        tfAesSecret.setEnabled(enabled);
        rbAesSecretRaw.setEnabled(enabled);
        rbAesSecretHex.setEnabled(enabled);
        rbAesSecretBase64.setEnabled(enabled);
        cbIsIVFixed.setEnabled(enabled);
        tfAesIv.setEnabled(enabled);
        rbAesIvRaw.setEnabled(enabled);
        rbAesIvHex.setEnabled(enabled);
        rbAesIvBase64.setEnabled(enabled);
        cmbAesIvPos.setEnabled(enabled);
        cmbAesCipherEncoding.setEnabled(enabled);
        // rsa
        cmbRsaMode.setEnabled(enabled);
        cmbRsaCharset.setEnabled(enabled);
        cmbRsaCipherEncoding.setEnabled(enabled);
        rbRsaKeyRaw.setEnabled(enabled);
        rbRsaKeyHex.setEnabled(enabled);
        rbRsaKeyBase64.setEnabled(enabled);
        btnImportRsaKey.setEnabled(enabled);
        taRsaKey.setEnabled(enabled);
        taRsaKey.setEditable(enabled);
        // hashing panel
        cmbHashAlgorithms.setEnabled(enabled);
        cmbHashCharset.setEnabled(enabled);
    }

    private class RBAlgCategoryItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            CardLayout cl = (CardLayout) algCategoryPanel.getLayout();
            JRadioButton selectedItem = (JRadioButton) e.getSource();
            if (selectedItem == rbCategoryEncoding) {
                cl.show(algCategoryPanel, "algEncodingPanel");
            } else if (selectedItem == rbCategoryEncryption) {
                cl.show(algCategoryPanel, "algEncryptionPanel");
            } else {
                cl.show(algCategoryPanel, "algHashingPanel");
            }
        }

    }

    private class RBEncryptionAlgItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            CardLayout cl = (CardLayout) algEncryptionOptions.getLayout();
            JRadioButton selectedItem = (JRadioButton) e.getSource();
            if (selectedItem == rbAlgAES) {
                cl.show(algEncryptionOptions, "AESOptionsPanel");
            } else {
                cl.show(algEncryptionOptions, "RSAOptionsPanel");
            }
        }

    }

    private class RBFormatItemStateChange implements ItemListener {

        private JRadioButton current = null;

        @Override
        public void itemStateChanged(ItemEvent e) {
            JRadioButton source = (JRadioButton) e.getSource();
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                current = source;
                return;
            }
            JTextComponent tf = null;
            String currentFormat = null;
            String newFormat = null;
            // determine the current value
            if (source == rbAesSecretRaw || source == rbAesSecretHex || source == rbAesSecretBase64) {
                tf = tfAesSecret;
            } else if (source == rbAesIvRaw || source == rbAesIvHex || source == rbAesIvBase64) {
                tf = tfAesIv;
            } else if (source == rbRsaKeyRaw || source == rbRsaKeyHex || source == rbRsaKeyBase64) {
                tf = taRsaKey;
            }
            // determine current format
            if (current == rbAesSecretRaw || current == rbAesIvRaw || current == rbRsaKeyRaw) {
                currentFormat = InterceptStage.FORMAT_RAW;
            } else if (current == rbAesSecretHex || current == rbAesIvHex || current == rbRsaKeyHex) {
                currentFormat = InterceptStage.FORMAT_HEX;
            } else if (current == rbAesSecretBase64 || current == rbAesIvBase64 || current == rbRsaKeyBase64) {
                currentFormat = InterceptStage.FORMAT_BASE64;
            }
            // determine new format
            if (source == rbAesSecretRaw || source == rbAesIvRaw || source == rbRsaKeyRaw) {
                newFormat = InterceptStage.FORMAT_RAW;
            } else if (source == rbAesSecretHex || source == rbAesIvHex || source == rbRsaKeyHex) {
                newFormat = InterceptStage.FORMAT_HEX;
            } else if (source == rbAesSecretBase64 || source == rbAesIvBase64 || source == rbRsaKeyBase64) {
                newFormat = InterceptStage.FORMAT_BASE64;
            }
            if (currentFormat == null || newFormat == null || tf == null) {
                return;
            }
            String currentValue = tf.getText();
            byte[] currentValueInBytes = new byte[]{};
            switch (currentFormat) {
                case InterceptStage.FORMAT_RAW:
                    currentValueInBytes = currentValue.getBytes();
                    break;
                case InterceptStage.FORMAT_HEX:
                    currentValueInBytes = Utils.hexToByteArray(currentValue);
                    break;
                case InterceptStage.FORMAT_BASE64:
                    currentValueInBytes = Base64.getDecoder().decode(currentValue);
                    break;
            }
            String newValue = "";
            switch (newFormat) {
                case InterceptStage.FORMAT_RAW:
                    newValue = new String(currentValueInBytes);
                    break;
                case InterceptStage.FORMAT_HEX:
                    newValue = Utils.byteArrayToHex(currentValueInBytes);
                    break;
                case InterceptStage.FORMAT_BASE64:
                    newValue = Base64.getEncoder().encodeToString(currentValueInBytes);
                    break;
            }
            tf.setText(newValue);
        }

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AESOptionsPanel;
    private javax.swing.JPanel RSAOptionsPanel;
    private javax.swing.JPanel algCategoryPanel;
    private javax.swing.JPanel algEncodingPanel;
    private javax.swing.JPanel algEncryptionOptions;
    private javax.swing.JPanel algEncryptionPanel;
    private javax.swing.JPanel algHashingPanel;
    private javax.swing.ButtonGroup bgAesIvFormat;
    private javax.swing.ButtonGroup bgAesSecretFormat;
    private javax.swing.ButtonGroup bgEncodingCipher;
    private javax.swing.ButtonGroup bgEncryptionCipher;
    private javax.swing.ButtonGroup bgMode;
    private javax.swing.ButtonGroup bgRsaKeyFormat;
    private javax.swing.JButton btnImportRsaKey;
    private javax.swing.JCheckBox cbIsIVFixed;
    private javax.swing.JComboBox<String> cmbAesAlgorithms;
    private javax.swing.JComboBox<String> cmbAesCharset;
    private javax.swing.JComboBox<String> cmbAesCipherEncoding;
    private javax.swing.JComboBox<String> cmbAesIvPos;
    private javax.swing.JComboBox<String> cmbAesMode;
    private javax.swing.JComboBox<String> cmbEncodingCharset;
    private javax.swing.JComboBox<String> cmbEncodingMode;
    private javax.swing.JComboBox<String> cmbHashAlgorithms;
    private javax.swing.JComboBox<String> cmbHashCharset;
    private javax.swing.JComboBox<String> cmbRsaCharset;
    private javax.swing.JComboBox<String> cmbRsaCipherEncoding;
    private javax.swing.JComboBox<String> cmbRsaMode;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAESCipherEncoding;
    private javax.swing.JLabel lblIVPos;
    private javax.swing.JLabel lblRSACipherEncoding;
    private javax.swing.JRadioButton rbAesIvBase64;
    private javax.swing.JRadioButton rbAesIvHex;
    private javax.swing.JRadioButton rbAesIvRaw;
    private javax.swing.JRadioButton rbAesSecretBase64;
    private javax.swing.JRadioButton rbAesSecretHex;
    private javax.swing.JRadioButton rbAesSecretRaw;
    private javax.swing.JRadioButton rbAlgAES;
    private javax.swing.JRadioButton rbAlgRSA;
    private javax.swing.JRadioButton rbCategoryEncoding;
    private javax.swing.JRadioButton rbCategoryEncryption;
    private javax.swing.JRadioButton rbCategoryHashing;
    private javax.swing.JRadioButton rbEncodingBase64Alg;
    private javax.swing.JRadioButton rbEncodingHexAlg;
    private javax.swing.JRadioButton rbRsaKeyBase64;
    private javax.swing.JRadioButton rbRsaKeyHex;
    private javax.swing.JRadioButton rbRsaKeyRaw;
    private javax.swing.JTextArea taRsaKey;
    private javax.swing.JTextField tfAesIv;
    private javax.swing.JTextField tfAesSecret;
    // End of variables declaration//GEN-END:variables
}
