package com.example.demo;

import org.testcontainers.containers.PostgreSQLContainer;

public class DockerSanityCheck {
    public static void main(String[] args) {
        System.out.println("⏳ Пытаемся связаться с Docker...");
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")) {
            postgres.start();
            System.out.println("✅ УРА! ДОКЕР РАБОТАЕТ! URL базы: " + postgres.getJdbcUrl());
        } catch (Exception e) {
            System.err.println("❌ Ошибка связи с Docker:");
            e.printStackTrace();
        }
    }
}