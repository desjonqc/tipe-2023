package com.cegesoft.log;

import lombok.Getter;

import java.io.PrintStream;

public class Logger {

    @Getter
    private static final Logger logger = new Logger();

    private final PrintStream out;

    public Logger(PrintStream out) {
        this.out = out;
    }

    public Logger() {
        this(System.out);
    }

    public void println(String... str) {
        if (str.length != 0)
            out.print(PrefixHandler.getPrefix());
        for (int i = 0; i < str.length; i++)
            out.println((i != 0 ? "\t" : "") + str[i]);
    }

    public void print(String str, boolean prefix) {
        if (prefix)
            out.print(PrefixHandler.getPrefix());
        out.print(str);
    }

    public void print(String str) {
        this.print(str, false);
    }

    public void warn(String message) {
        this.println(ConsoleColors.YELLOW_BOLD + "[WARNING] " + ConsoleColors.RESET + ConsoleColors.YELLOW + message + ConsoleColors.RESET);
    }

    public void warn(String message, Exception e) {
        this.println(ConsoleColors.YELLOW_BOLD + "[WARNING] " + ConsoleColors.RESET + ConsoleColors.YELLOW + message);
        e.printStackTrace();
        this.print(ConsoleColors.RESET);
    }


}
