package com.juuu.lab2;

import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.jpbc.PairingParametersGenerator;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

import java.math.BigInteger;
import java.security.SecureRandom;

public class JPBCTest {

    public static void main(String[] args) {

        //使用自定义曲线
        int rBits = 160; // 素数阶的位长度
        int qBits = 512; // 群的位长度
        PairingParametersGenerator pg = new TypeACurveGenerator(rBits, qBits);
        PairingParameters params = pg.generate();
        Pairing pairing = PairingFactory.getPairing(params);

        // 获取群G1, G2, GT
        Field<Element> G1 = pairing.getG1();
        Field<Element> G2 = pairing.getG2();
        Field<Element> GT = pairing.getGT();

        // 获取生成元
        Element P1 = G1.newRandomElement().getImmutable();
        Element P2 = G2.newRandomElement().getImmutable();

        // 随机选择主私钥
        BigInteger N = pairing.getG1().getOrder();
        BigInteger ks = new BigInteger(N.bitLength(), new SecureRandom()).mod(N);
        BigInteger ke = new BigInteger(N.bitLength(), new SecureRandom()).mod(N);

        // 计算主公钥
        Element P_pub_s = P2.mul(ks).getImmutable();
        Element P_pub_e = P1.mul(ke).getImmutable();

        // 输出结果
        System.out.println("P_pub_s: " + P_pub_s);
        System.out.println("P_pub_e: " + P_pub_e);
    }
}

