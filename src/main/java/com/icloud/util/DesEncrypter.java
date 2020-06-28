package com.icloud.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DesEncrypter {
    public static final int INT_PRIM_NUMBER = 95;
    public static final int INT_RETURN_LOOP = 94;

    private static String decode(String strCode) {
        String strOriginal;
        int intRnd;
        String strRnd;
        int intStrLen;
        String strDecodeMe = "";

        if (strCode.equals("")) {
            return strDecodeMe;
        }
        intStrLen = strCode.length() - 1;

        strRnd = strCode.substring(intStrLen / 2, intStrLen / 2 + 1);
        intRnd = strRnd.hashCode() - startChar();
        strCode = strCode.substring(0, intStrLen / 2) + strCode.substring(intStrLen / 2 + 1, intStrLen + 1);
        strOriginal = loopCode(strCode, INT_RETURN_LOOP - intRnd);
        strDecodeMe = strOriginal;
        return strDecodeMe;
    }

    /**
     * 加密方法.随机取得加密的循环次数，使得每次加密所得的秘文会有所不同 zhg 创建日期 (2002-12-15 10:17:08)
     * strOriginal 需加密的字符串 加密后的字符串 1.0
     */
    private static String encode(String strOriginal) {
        String strCode;
        int intRnd;
        char rnd;
        int intStrLen;
        String strCodeMe = "";
        if (strOriginal.equals(""))
            return strCodeMe;
        // 2 到 93 之间的随即数，即同一原文可能获得93种不同的秘文
        intRnd = (int) (Math.random() * (INT_RETURN_LOOP - 2) + 2);
        strCode = loopCode(strOriginal, intRnd);
        // 对随机数作偏移加密
        rnd = (char) (intRnd + startChar());
        intStrLen = strCode.length();
        strCodeMe = strCode.substring(0, intStrLen / 2) + rnd + strCode.substring(intStrLen / 2, intStrLen);
        if (strCodeMe.indexOf("??") >= 0)
            return encode(strOriginal);
        else
            return strCodeMe;
    }

    // 基础的凯撒算法,并对于每一位增加了偏移
    private static String kaiserCode(String strOriginal) {
        int intChar;
        String strCode;
        int i;
        int intStrLen;
        int intTmp;

        intStrLen = strOriginal.length();

        strCode = "";
        for (i = 0; i < intStrLen; i++) {
            intChar = strOriginal.substring(i, i + 1).hashCode();
            intTmp = intChar - startChar();
            intTmp = (intTmp * INT_PRIM_NUMBER + i + 1) % maxChar() + startChar();
            strCode = strCode + (char) (intTmp);
        }
        return strCode;
    }

    // 循环调用凯撒算法一定次数后，可以取得原文
    private static String loopCode(String strOriginal, int intLoopCount) {
        String strCode;
        int i;
        strCode = strOriginal;
        for (i = 0; i < intLoopCount; i++)
            strCode = kaiserCode(strCode);
        return strCode;
    }

    private static int maxChar() {
        return "~".hashCode() - "!".hashCode() + 1;
    }

    private static int startChar() {
        return "!".hashCode();
    }

    /**
     * 加密
     * @param pw
     * @return
     */
    public static String encrypt(String pw) {
        try {
//            pw = new sun.misc.BASE64Encoder().encode(pw.getBytes());
            pw = Base64.getEncoder().encodeToString(pw.getBytes(StandardCharsets.UTF_8));
            return pw;

        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 解密
     * @param pw
     * @return
     */
    public static String decrypt(String pw) {
        try {
            String pw1 = encode(pw);
            String pw2 = decode(pw1);
//            byte[] bt = new sun.misc.BASE64Decoder().decodeBuffer(pw2);
            byte[] bt = Base64.getDecoder().decode(pw2);
            pw2 = new String(bt);
            return pw2;
        } catch (Exception e) {
        }
        return null;
    }

//    public static void main(String[] args) throws Exception {
////        String enCode = "zlbatch@yr2018";// t.encrypt(pw);
//        String enCode = "123456";// t.encrypt(pw);
//
//        System.out.println(enCode);
//        System.out.println(DesEncrypter.encrypt(enCode));
//        System.out.println(DesEncrypter.decrypt(DesEncrypter.encrypt(enCode)));
//
//    }

}
