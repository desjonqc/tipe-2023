package com.cegesoft.log;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Classe permettant de gérer les logs de l'application, ainsi que les barres de progression.
 * Ajoute les messages à traiter dans le AsyncLogger
 * @see AsyncLogger
 */
public class Logger {

    @Getter
    private static final Logger logger = new Logger();

    private final AsyncLogger asyncLogger;
    private final SimpleDateFormat dateFormat;
    public Logger(PrintStream out) {
        this.asyncLogger = new AsyncLogger(out);
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    public Logger() {
        this(System.out);
    }

    public static ProgressBar getOrCreateProgressBar() {
        ProgressBar bar = logger.asyncLogger.currentProgressBar;
        bar = bar == null ? new ProgressBar() : bar;
        logger.registerProgressBar(bar);
        return bar;
    }

    public static void resetProgressBar() {
        logger.unregisterProgressBar();
    }

    public void registerProgressBar(ProgressBar progressBar) {
        this.asyncLogger.setCurrentProgressBar(progressBar);
    }

    public void unregisterProgressBar() {
        this.asyncLogger.setCurrentProgressBar(null);
    }

    private void println(String... str) {
        if (str.length != 0)
            asyncLogger.print(PrefixHandler.getPrefix() + "[" + this.dateFormat.format(new Date()) + "] ");
        for (int i = 0; i < str.length; i++)
            asyncLogger.println((i != 0 ? "\t" : "") + str[i]);
    }

    private void print_(String str, boolean prefix) {
        if (prefix)
            asyncLogger.print(PrefixHandler.getPrefix() + "[" + this.dateFormat.format(new Date()) + "] ");
        asyncLogger.print(str);
    }

    private void print_(String str) {
        this.print_(str, false);
    }

    public static void warn(String message) {
        logger.println(ConsoleColors.YELLOW_BOLD + "[WARNING] " + ConsoleColors.RESET + ConsoleColors.YELLOW + message + ConsoleColors.RESET);
    }

    public static void warn(Exception e) {
        warn(e.getMessage());
        if (e.getCause() != null)
            warn("Caused by : " + e.getCause().getMessage());
    }

    public static void warn(String message, Exception e) {
        warn(message);
        warn(e);
    }

    public static void error(String message) {
        logger.println(ConsoleColors.RED_BOLD + "[ERROR] " + ConsoleColors.RESET + ConsoleColors.RED + message + ConsoleColors.RESET);
    }

    public static void error(Exception e) {
        error(e.getMessage());
        if (e.getCause() != null)
            error("Caused by : " + e.getCause().getMessage());
        e.printStackTrace();
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

    /**
     * Gère la console en asynchrone
     */
    private static class AsyncLogger extends Thread {
        private final Queue<String> queue = new ConcurrentLinkedQueue<>();
        @Getter
        @Setter
        private ProgressBar currentProgressBar = null;
        private final PrintStream out;
        public AsyncLogger(PrintStream out) {
            this.out = out;
            this.start();
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                String message;
                if (currentProgressBar != null && currentProgressBar.isValid() && !queue.isEmpty()) {
                    out.print("\r");
                }
                while ((message = queue.poll()) != null) {
                    out.print(message);
                }
                if (currentProgressBar != null && currentProgressBar.isValid() && (!queue.isEmpty() || currentProgressBar.hasChanged())) {
                    out.print(currentProgressBar.update());
                    if (currentProgressBar.isFinished()) {
                        currentProgressBar = null;
                    }
                }
            }
        }

        public void println(String message) {
            queue.add(message + "\n");
        }

        public void print(String message) {
            queue.add(message);
        }
    }

}
