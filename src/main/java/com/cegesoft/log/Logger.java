package com.cegesoft.log;

import lombok.Getter;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    @Getter
    private static final Logger logger = new Logger();

    private final PrintStream out;
    private final SimpleDateFormat dateFormat;
    public Logger(PrintStream out) {
        this.out = out;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    public Logger() {
        this(System.out);
    }

    private void println(String... str) {
        if (str.length != 0)
            out.print(PrefixHandler.getPrefix() + "[" + this.dateFormat.format(new Date()) + "] ");
        for (int i = 0; i < str.length; i++)
            out.println((i != 0 ? "\t" : "") + str[i]);
    }

    private void print_(String str, boolean prefix) {
        if (prefix)
            out.print(PrefixHandler.getPrefix() + "[" + this.dateFormat.format(new Date()) + "] ");
        out.print(str);
    }

    private void print_(String str) {
        this.print_(str, false);
    }

    public static void warn(String message) {
        logger.println(ConsoleColors.YELLOW_BOLD + "[WARNING] " + ConsoleColors.RESET + ConsoleColors.YELLOW + message + ConsoleColors.RESET);
    }

    public static void warn(String message, Exception e) {
        logger.println(ConsoleColors.YELLOW_BOLD + "[WARNING] " + ConsoleColors.RESET + ConsoleColors.YELLOW + message);
        e.printStackTrace();
        logger.print_(ConsoleColors.RESET);
    }

    public static void error(String message) {
        logger.println(ConsoleColors.RED_BOLD + "[ERROR] " + ConsoleColors.RESET + ConsoleColors.RED + message + ConsoleColors.RESET);
    }

    public static void error(Exception e) {
        error(e.getMessage());
        if (e.getCause() != null)
            error("Caused by : " + e.getCause().getMessage());
    }

    public static void error(String message, Exception e) {
        error(message);
        error(e);
    }

    public static void info(String... message) {
        logger.println(message);
    }

    public static void print(String message) {
        logger.print_(message);
    }

    public static void print(String message, boolean prefix) {
        logger.print_(message, prefix);
    }

}
