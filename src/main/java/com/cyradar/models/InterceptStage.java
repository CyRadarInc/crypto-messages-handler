/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyradar.models;

import java.util.HashMap;

/**
 *
 * @author phinc27
 */
public class InterceptStage extends HashMap<String, Object> {

    public static final String CTX_ALGORITHM = "algorithm";
    public static final String CTX_MODE = "mode";
    public static final String CTX_KEY = "key";
    public static final String CTX_KEY_FORMAT = "keyFormat";
    public static final String CTX_IV = "iv";
    public static final String CTX_IV_FORMAT = "ivFormat";
    public static final String CTX_IV_POSITION = "ivPosition";
    public static final String CTX_IV_FIXED = "usingFixedIv";
    public static final String CTX_CHARSET = "charset";
    public static final String CTX_CIPHER_ENCODING = "cipherEncoding";
    // supported algorithms
    public static final String ALG_AES_EBC_PKCS5_PADDING = "AES/ECB/PKCS5Padding";
    public static final String ALG_AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    public static final String ALG_AES_CTR_NOPADDING = "AES/CTR/NoPadding";
    public static final String ALG_RSA_ECB_PKCS1PADDING = "RSA/ECB/PKCS1Padding";
    public static final String ALG_MD5 = "MD5";
    public static final String ALG_SHA1 = "SHA-1";
    public static final String ALG_SHA256 = "SHA-256";
    public static final String ALG_SHA384 = "SHA-384";
    public static final String ALG_SHA512 = "SHA-512";
    public static final String ALG_HEX = "HEX";
    public static final String ALG_BASE64 = "BASE64";
    // modes
    public static final String MODE_ENCRYPT = "Encrypt";
    public static final String MODE_DECRYPT = "Decrypt";
    public static final String MODE_ENCODE = "Encode";
    public static final String MODE_DECODE = "Decode";
    // form (IV || keys)
    public static final String FORMAT_RAW = "raw";
    public static final String FORMAT_HEX = "hex";
    public static final String FORMAT_BASE64 = "base64";
    // iv position
    public static final String IV_POSITION_PREFIX = "Prefix";
    public static final String IV_POSITION_POSTFIX = "Postfix";
    public static final String IV_POSITION_NONE = "None";
    // charsets
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String CHARSET_ASCII = "ASCII";
    // cipher encodings
    public static final String CIPHER_ENCODING_HEX = "Hexadecimal";
    public static final String CIPHER_ENCODING_BASE64 = "Base64";

    public static InterceptStage getDefault() {
        InterceptStage stage = new InterceptStage();
        stage.put(CTX_ALGORITHM, ALG_BASE64);
        stage.put(CTX_MODE, MODE_ENCODE);
        stage.put(CTX_CHARSET, CHARSET_UTF8);
        return stage;
    }
}
