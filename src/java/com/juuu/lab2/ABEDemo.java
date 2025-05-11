package com.juuu.lab2;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class ABEDemo {
    private Pairing pairing;
    private Element g;  // 生成元
    private Element mk; // 主密钥
    private Element pk; // 公钥

    public ABEDemo() {
        // 初始化配对
        pairing = PairingFactory.getPairing("a.properties");
        // 初始化系统参数
        setup();
    }

    private void setup() {
        // 生成系统参数
        g = pairing.getG1().newRandomElement().getImmutable();
        mk = pairing.getZr().newRandomElement().getImmutable();
        pk = g.powZn(mk).getImmutable();
    }

    public Element[] encrypt(String message, Set<String> attributes) {
        System.out.println("加密过程中使用的属性集: " + attributes);
        
        // 将消息转换为Element
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        Element m = pairing.getGT().newElement()
                .setFromHash(messageBytes, 0, messageBytes.length)
                .getImmutable();
        
        // 随机数s
        Element s = pairing.getZr().newRandomElement().getImmutable();
        
        // 计算C = m * e(g,g)^(s*mk)
        Element egg = pairing.pairing(g, g).getImmutable();
        Element eggsmk = egg.powZn(s.mulZn(mk)).getImmutable();
        Element C = m.mul(eggsmk).getImmutable();
        
        // 对每个属性计算C_i = g^(s/attr)
        Element[] attrElements = new Element[attributes.size() + 1];
        attrElements[0] = C;
        
        int i = 1;
        for (String attr : attributes) {
            Element attrHash = pairing.getZr().newElement()
                    .setFromHash(attr.getBytes(StandardCharsets.UTF_8), 0, attr.length())
                    .getImmutable();
            Element ci = g.powZn(s.div(attrHash)).getImmutable();
            attrElements[i++] = ci;
        }
        
        return attrElements;
    }

    public String decrypt(Element[] ciphertext, Set<String> attributes) {
        if (attributes.size() + 1 != ciphertext.length) {
            return "解密失败: 属性数量不匹配";
        }
        
        Element C = ciphertext[0];
        
        try {
            // 重构 e(g,g)^(s*mk)
            Element product = pairing.getGT().newOneElement();
            int i = 1;
            for (String attr : attributes) {
                Element attrHash = pairing.getZr().newElement()
                        .setFromHash(attr.getBytes(StandardCharsets.UTF_8), 0, attr.length())
                        .getImmutable();
                Element ci = ciphertext[i++];
                Element pai = pairing.pairing(ci, g.powZn(attrHash));
                product.mul(pai);
            }
            
            // 还原消息
            Element m = C.div(product);
            
            // 将Element转换回字符串
            byte[] messageBytes = m.toBytes();
            return new String(messageBytes, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            return "解密失败: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        ABEDemo abe = new ABEDemo();
        
        // 原文
        String message = "这是一个属性基加密的测试消息";
        System.out.println("原文: " + message);
        
        // 正确的属性集
        Set<String> correctAttributes = new HashSet<>(Arrays.asList("学生", "教师", "管理员"));
        
        // 加密
        Element[] ciphertext = abe.encrypt(message, correctAttributes);
        System.out.println("密文长度: " + ciphertext.length);
        
        // 将密文转换为Base64以便显示
        System.out.println("\n密文(Base64编码):");
        for (Element e : ciphertext) {
            System.out.println(Base64.getEncoder().encodeToString(e.toBytes()));
        }
        
        // 使用正确的属性集解密
        System.out.println("\n使用正确的属性集解密:");
        String decryptedCorrect = abe.decrypt(ciphertext, correctAttributes);
        System.out.println("解密结果: " + decryptedCorrect);
        
        // 使用错误的属性集解密
        Set<String> wrongAttributes = new HashSet<>(Arrays.asList("访客", "学生", "教师"));
        System.out.println("\n使用错误的属性集解密:");
        String decryptedWrong = abe.decrypt(ciphertext, wrongAttributes);
        System.out.println("解密结果: " + decryptedWrong);
    }
}