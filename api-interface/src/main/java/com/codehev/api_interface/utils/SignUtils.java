package com.codehev.api_interface.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * @author <a href="https://github.com/codehev">codehev</a>
 * @email codehev@qq.com
 * @date 2025/1/13 17:33
 * @description 签名工具
 */
public class SignUtils {
    /**
     * 生成签名
     * @param body
     * @param secretKey
     * @return
     */
    public static String genSign(String body, String secretKey) {
        // 摘要算法，对原数据进行某种形式的提取，经过计算后输出的密文都是固定长度的，且不可逆
        Digester md5 = new Digester(DigestAlgorithm.SHA256);
        // 拼接密钥
        String content = body + "." + secretKey;
        return md5.digestHex(content);
    }
}
