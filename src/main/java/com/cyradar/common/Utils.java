/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyradar.common;

import java.nio.ByteBuffer;

/**
 *
 * @author phinc27
 */
public class Utils {

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hexToByteArray(String hex) {
        if (hex.isEmpty()) {
            return new byte[0];
        }
        String[] list = hex.split("(?<=\\G.{2})");
        ByteBuffer buffer = ByteBuffer.allocate(list.length);
        System.out.println(list.length);
        for (String str : list) {
            buffer.put((byte) Integer.parseInt(str, 16));
        }
        return buffer.array();
    }
}
