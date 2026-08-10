package com.nova.factoryerp.utils;

/**
 * Run this class standalone to generate BCrypt hashes for seed.sql.
 * Usage: java -cp ... com.nova.factoryerp.utils.PasswordHasher
 */
public class PasswordHasher {
    public static void main(String[] args) {
        String[] passwords = {"admin123", "manager123"};
        System.out.println("=== BCrypt Hashes for seed.sql ===");
        for (String p : passwords) {
            System.out.println(p + " -> " + PasswordUtil.hash(p));
        }
    }
}
