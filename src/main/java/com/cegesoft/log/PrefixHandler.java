package com.cegesoft.log;

import java.util.HashMap;

public class PrefixHandler {

    private static final HashMap<String, String> prefixes = new HashMap<>();

    static {
        registerPrefix("measure", ConsoleColors.PURPLE_BOLD + "[MEASURE] " + ConsoleColors.RESET);
        registerPrefix("game", ConsoleColors.GREEN_BOLD + "[GAME] " + ConsoleColors.RESET);
        registerPrefix("help", ConsoleColors.CYAN + "[HELP] " + ConsoleColors.RESET);
        registerPrefix("data", ConsoleColors.PURPLE + "[DATA] " + ConsoleColors.RESET);
        registerPrefix("simulation", ConsoleColors.BLUE + "[SIMULATION] " + ConsoleColors.RESET);
        registerPrefix("statistic", ConsoleColors.BLUE_BOLD + "[STATISTIC] " + ConsoleColors.RESET);
        registerPrefix("ui", ConsoleColors.CYAN_BRIGHT + "[GUI] " + ConsoleColors.RESET);
        registerPrefix("app", ConsoleColors.PURPLE_BRIGHT + "[APP] " + ConsoleColors.RESET);
        registerPrefix("ai", ConsoleColors.RED + "[AI] " + ConsoleColors.RESET);
        registerPrefix("", ConsoleColors.GREEN_BRIGHT + "[COMMAND] " + ConsoleColors.RESET);
    }

    private static void registerPrefix(String packageName, String prefix) {
        if (packageName.equals("")) {
            prefixes.put("com.cegesoft", prefix);
            return;
        }
        prefixes.put("com.cegesoft." + packageName, prefix);
    }

    protected static String getPrefix() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        int i = 1;
        StackTraceElement element = trace[i];
        while (element.getClassName().contains("log")) {
            i++;
            element = trace[i];
        }
        StringBuilder packageBuilder = new StringBuilder();
        String prefix = "";
        for (String comp : element.getClassName().split("\\.")) {
            if (!packageBuilder.toString().equals(""))
                packageBuilder.append(".");
            packageBuilder.append(comp);
            if (prefixes.containsKey(packageBuilder.toString())) {
                prefix = prefixes.get(packageBuilder.toString());
            }
        }
        return prefix;
    }

}
