/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyradar.core;

import com.cyradar.common.Utils;
import com.cyradar.models.InterceptStage;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author phinc27
 */
public class InterceptProcessor {

    public static String doProcess(String value, InterceptStage stage) throws Exception {
        String algorithm = (String) stage.get(InterceptStage.CTX_ALGORITHM);
        switch (algorithm) {
            case InterceptStage.ALG_HEX:
                return processHex(value, (String) stage.get(InterceptStage.CTX_MODE), (String) stage.get(InterceptStage.CTX_CHARSET));
            case InterceptStage.ALG_BASE64:
                return processBase64(value, (String) stage.get(InterceptStage.CTX_MODE), (String) stage.get(InterceptStage.CTX_CHARSET));
            case InterceptStage.ALG_AES_CBC_PKCS5_PADDING:
            case InterceptStage.ALG_AES_CTR_NOPADDING:
            case InterceptStage.ALG_AES_EBC_PKCS5_PADDING:
                switch ((String) stage.get(InterceptStage.CTX_MODE)) {
                    case InterceptStage.MODE_ENCRYPT:
                        return processAESEncrypt(value, stage);
                    case InterceptStage.MODE_DECRYPT:
                        return processAESDecrypt(value, stage);
                    default:
                        throw new Exception("invalid mode");
                }
            case InterceptStage.ALG_RSA_ECB_PKCS1PADDING:
                switch ((String) stage.get(InterceptStage.CTX_MODE)) {
                    case InterceptStage.MODE_ENCRYPT:
                        return processRSAEncrypt(
                                value,
                                (String) stage.get(InterceptStage.CTX_KEY),
                                (String) stage.get(InterceptStage.CTX_KEY_FORMAT),
                                (String) stage.get(InterceptStage.CTX_CHARSET),
                                (String) stage.get(InterceptStage.CTX_CIPHER_ENCODING)
                        );
                    case InterceptStage.MODE_DECRYPT:
                        return processRSADecrypt(
                                value,
                                (String) stage.get(InterceptStage.CTX_KEY),
                                (String) stage.get(InterceptStage.CTX_KEY_FORMAT),
                                (String) stage.get(InterceptStage.CTX_CHARSET),
                                (String) stage.get(InterceptStage.CTX_CIPHER_ENCODING)
                        );
                    default:
                        throw new Exception("invalid mode");
                }
            case InterceptStage.ALG_MD5:
            case InterceptStage.ALG_SHA1:
            case InterceptStage.ALG_SHA256:
            case InterceptStage.ALG_SHA384:
            case InterceptStage.ALG_SHA512:
                return processHash(value, algorithm, (String) stage.get(InterceptStage.CTX_CHARSET));
            default:
                throw new Exception("algorithm is missing or unsupported");
        }
    }

    private static String processHex(String value, String mode, String charset) throws Exception {
        if (InterceptStage.MODE_ENCODE.equals(mode)) {
            byte[] valueInBytes = value.getBytes(charset);
            return Utils.byteArrayToHex(valueInBytes);
        }
        if (InterceptStage.MODE_DECODE.equals(mode)) {
            byte[] valueInBytes = Utils.hexToByteArray(value);
            return new String(valueInBytes, charset);
        }
        throw new Exception("invalid mode");
    }

    private static String processBase64(String value, String mode, String charset) throws Exception {
        if (InterceptStage.MODE_ENCODE.equals(mode)) {
            byte[] valueInBytes = value.getBytes(charset);
            return Base64.getEncoder().encodeToString(valueInBytes);
        }
        if (InterceptStage.MODE_DECODE.equals(mode)) {
            byte[] valueInBytes = Base64.getDecoder().decode(value);
            return new String(valueInBytes, charset);
        }
        throw new Exception("invalid mode");
    }

    private static byte[] getRaw(String value, String format) throws Exception {
        switch (format) {
            case InterceptStage.FORMAT_RAW:
                return value.getBytes();
            case InterceptStage.FORMAT_HEX:
                return Utils.hexToByteArray(value);
            case InterceptStage.FORMAT_BASE64:
                return Base64.getDecoder().decode(value);
        }
        throw new Exception(String.format("format must be in (%s, %s, %s)", InterceptStage.FORMAT_RAW, InterceptStage.FORMAT_HEX, InterceptStage.FORMAT_BASE64));
    }

    private static String processAESEncrypt(String value, InterceptStage stage) throws Exception {
        String algorithm = (String) stage.get(InterceptStage.CTX_ALGORITHM);
        Cipher cipher = Cipher.getInstance(algorithm);
        byte[] key = getRaw((String) stage.get(InterceptStage.CTX_KEY), (String) stage.get(InterceptStage.CTX_KEY_FORMAT));
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        byte[] input = value.getBytes((String) stage.get(InterceptStage.CTX_CHARSET));
        byte[] output;
        if (algorithm.equals(InterceptStage.ALG_AES_EBC_PKCS5_PADDING)) {
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            output = cipher.doFinal(input);
        } else {
            byte[] iv = (Boolean) stage.getOrDefault(InterceptStage.CTX_IV_FIXED, Boolean.FALSE) ? getRaw((String) stage.get(InterceptStage.CTX_IV), (String) stage.get(InterceptStage.CTX_IV_FORMAT)) : getRandomIV();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
            output = cipher.doFinal(input);
            switch ((String) stage.get(InterceptStage.CTX_IV_POSITION)) {
                case InterceptStage.IV_POSITION_PREFIX:
                    output = Utils.concat(iv, output);
                    break;
                case InterceptStage.IV_POSITION_POSTFIX:
                    output = Utils.concat(output, iv);
                    break;
                case InterceptStage.IV_POSITION_NONE:
                    break;
                default:
                    throw new Exception("invalid iv position");
            }
        }
        switch ((String) stage.get(InterceptStage.CTX_CIPHER_ENCODING)) {
            case InterceptStage.CIPHER_ENCODING_HEX:
                return Utils.byteArrayToHex(output);
            case InterceptStage.CIPHER_ENCODING_BASE64:
                return Base64.getEncoder().encodeToString(output);
            default:
                throw new Exception(String.format("cipher encoding must be in (%s, %s)", InterceptStage.CIPHER_ENCODING_HEX, InterceptStage.CIPHER_ENCODING_BASE64));
        }
    }

    private static String processAESDecrypt(String value, InterceptStage stage) throws Exception {
        byte[] input;
        switch ((String) stage.get(InterceptStage.CTX_CIPHER_ENCODING)) {
            case InterceptStage.CIPHER_ENCODING_HEX:
                input = Utils.hexToByteArray(value);
                break;
            case InterceptStage.CIPHER_ENCODING_BASE64:
                input = Base64.getDecoder().decode(value);
                break;
            default:
                throw new Exception(String.format("cipher encoding must be in (%s, %s)", InterceptStage.CIPHER_ENCODING_HEX, InterceptStage.CIPHER_ENCODING_BASE64));
        }
        byte[] output;
        String algorithm = (String) stage.get(InterceptStage.CTX_ALGORITHM);
        Cipher cipher = Cipher.getInstance(algorithm);
        byte[] key = getRaw((String) stage.get(InterceptStage.CTX_KEY), (String) stage.get(InterceptStage.CTX_KEY_FORMAT));
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        if (algorithm.equals(InterceptStage.ALG_AES_EBC_PKCS5_PADDING)) {
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            output = cipher.doFinal(input);
        } else {
            byte[] iv;
            if ((Boolean) stage.getOrDefault(InterceptStage.CTX_IV_FIXED, Boolean.FALSE)) {
                iv = getRaw((String) stage.get(InterceptStage.CTX_IV), (String) stage.get(InterceptStage.CTX_IV_FORMAT));
            } else {
                switch ((String) stage.get(InterceptStage.CTX_IV_POSITION)) {
                    case InterceptStage.IV_POSITION_PREFIX:
                        iv = Arrays.copyOfRange(input, 0, 16);
                        input = Arrays.copyOfRange(input, 16, input.length);
                        break;
                    case InterceptStage.IV_POSITION_POSTFIX:
                        iv = Arrays.copyOfRange(input, input.length - 16, input.length);
                        input = Arrays.copyOfRange(input, 0, input.length - 16);
                        break;
                    case InterceptStage.IV_POSITION_NONE:
                        throw new Exception("Invalid configuration for CBC/CTR mode: IV Position cannot be none");
                    default:
                        throw new Exception("invalid iv position");
                }
            }
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
            output = cipher.doFinal(input);
        }
        return new String(output, (String) stage.get(InterceptStage.CTX_CHARSET));
    }

    private static String processRSAEncrypt(String value, String key, String keyFormat, String charset, String cipherEncoding) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        byte[] input = value.getBytes(charset);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] keyInBytes = getRaw(key, keyFormat);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyInBytes);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] output = cipher.doFinal(input);
        switch (cipherEncoding) {
            case InterceptStage.CIPHER_ENCODING_HEX:
                return Utils.byteArrayToHex(output);
            case InterceptStage.CIPHER_ENCODING_BASE64:
                return Base64.getEncoder().encodeToString(output);
            default:
                throw new Exception(String.format("cipher encoding must be in (%s, %s)", InterceptStage.CIPHER_ENCODING_HEX, InterceptStage.CIPHER_ENCODING_BASE64));
        }
    }

    private static String processRSADecrypt(String value, String key, String keyFormat, String charset, String cipherEncoding) throws Exception {
        byte[] input;
        switch (cipherEncoding) {
            case InterceptStage.CIPHER_ENCODING_HEX:
                input = Utils.hexToByteArray(value);
                break;
            case InterceptStage.CIPHER_ENCODING_BASE64:
                input = Base64.getDecoder().decode(value);
                break;
            default:
                throw new Exception(String.format("cipher encoding must be in (%s, %s)", InterceptStage.CIPHER_ENCODING_HEX, InterceptStage.CIPHER_ENCODING_BASE64));
        }
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] keyInBytes = getRaw(key, keyFormat);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyInBytes);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] output = cipher.doFinal(input);
        return new String(output, charset);
    }

    private static String processHash(String value, String algorithm, String charset) throws Exception {
        byte[] input = value.getBytes(charset);
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        messageDigest.update(input);
        return Utils.byteArrayToHex(messageDigest.digest());
    }

    private static byte[] getRandomIV() throws Exception {
        byte[] bytes = new byte[16];
        SecureRandom.getInstanceStrong().nextBytes(bytes);
        return bytes;
    }
}
