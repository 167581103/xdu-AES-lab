package com.juuu.lab1;

import java.math.BigInteger;
import java.util.Random;

public class DHExample {
    public static void main(String[] args) {
        // 选择大素数 p 和原根 g
        BigInteger p = new BigInteger("23");
        BigInteger g = new BigInteger("5");

        // Alice 选择一个私钥
        Random random = new Random();
        BigInteger a = new BigInteger(100, random);
        // Bob 选择一个私钥
        BigInteger b = new BigInteger(100, random);

        // Alice 计算公开值 A = g^a mod p
        BigInteger A = g.modPow(a, p);
        // Bob 计算公开值 B = g^b mod p
        BigInteger B = g.modPow(b, p);

        // Alice 接收到 Bob 的公开值 B 后，计算共享密钥 K = B^a mod p
        BigInteger aliceK = B.modPow(a, p);
        // Bob 接收到 Alice 的公开值 A 后，计算共享密钥 K = A^b mod p
        BigInteger bobK = A.modPow(b, p);

        // 输出结果
        System.out.println("大素数 p: " + p);
        System.out.println("原根 g: " + g);
        System.out.println("Alice 的私钥 a: " + a);
        System.out.println("Bob 的私钥 b: " + b);
        System.out.println("Alice 的公开值 A: " + A);
        System.out.println("Bob 的公开值 B: " + B);
        System.out.println("Alice 计算出的共享密钥 K: " + aliceK);
        System.out.println("Bob 计算出的共享密钥 K: " + bobK);

        // 验证双方计算的共享密钥是否相同
        if (aliceK.equals(bobK)) {
            System.out.println("双方计算的共享密钥相同，密钥交换成功！");
        } else {
            System.out.println("双方计算的共享密钥不同，密钥交换失败！");
        }
    }
}