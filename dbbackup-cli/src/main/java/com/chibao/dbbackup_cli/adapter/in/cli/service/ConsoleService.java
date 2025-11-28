package com.chibao.dbbackup_cli.adapter.in.cli.service;

import org.springframework.stereotype.Service;

@Service
public class ConsoleService {

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    public void printSuccess(String message) {
        System.out.println(GREEN + "✅ " + message + RESET);
    }

    public void printError(String message) {
        System.out.println(RED + "❌ " + message + RESET);
    }

    public void printWarning(String message) {
        System.out.println(YELLOW + "⚠️ " + message + RESET);
    }

    public void printInfo(String message) {
        System.out.println(BLUE + "ℹ️ " + message + RESET);
    }

    public String formatSuccess(String message) {
        return GREEN + "✅ " + message + RESET;
    }

    public String formatError(String message) {
        return RED + "❌ " + message + RESET;
    }

    public String formatKey(String key) {
        return CYAN + BOLD + key + RESET;
    }

    public String formatValue(String value) {
        return YELLOW + value + RESET;
    }

    /**
     * Simulates a progress bar for a task.
     * Note: This is a visual simulation. In a real scenario, you'd update based on
     * actual progress.
     */
    public void animateProgress(String taskName) {
        System.out.print(CYAN + taskName + " " + RESET);
        try {
            for (int i = 0; i < 10; i++) {
                System.out.print(".");
                Thread.sleep(100);
            }
            System.out.println(GREEN + " Done!" + RESET);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
