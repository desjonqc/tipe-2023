package com.cegesoft;

import com.cegesoft.app.Application;
import com.cegesoft.app.ApplicationsImpl;
import com.cegesoft.app.property.Property;
import com.cegesoft.log.Logger;

import java.util.Scanner;

/**
 * Application de simulation d'une partie de billard
 * L'objet de ce programme est de trouver le meilleur coup à jouer
 *
 * @author Clément DESJONQUERES
 */
public class Main {

    public static Scanner scanner;
    public static boolean CONSOLE_RUN = false;

    public static Application CURRENT_APPLICATION;

    public static void main(String[] args) {
        CONSOLE_RUN = (args.length >= 1 && args[0].startsWith("--ideLaunch")) || System.console() != null;
        scanner = new Scanner(System.in);

        handleCommand(args);

        Runtime.getRuntime().addShutdownHook(new Thread(CURRENT_APPLICATION::stop));
    }

    public static void listenCommand() {
        Logger.print("> ");
        String s;
        while ((s = scanner.nextLine()).equals("") || !handleCommand(s.split(" ")))
            Logger.print("> ");
    }

    public static boolean handleCommand(String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("quit")) {
            System.exit(0);
            return true;
        }

        ApplicationsImpl currentApplication = CONSOLE_RUN ? ApplicationsImpl.HELP : ApplicationsImpl.GAME;

        for (String arg : args) {
            if (arg.startsWith("app=")) {
                String applicationName = arg.split("=")[1].toUpperCase();
                try {
                    currentApplication = ApplicationsImpl.valueOf(applicationName);
                } catch (Exception ignored) {
                    Logger.warn("Application '" + applicationName + "' does not exist. Skipping to default");
                }
            }
        }

        CURRENT_APPLICATION = currentApplication.getApplication();
        try {
            if (CURRENT_APPLICATION.readArguments(args)) {
                return false;
            }
        } catch (IllegalArgumentException e) {
            Logger.error("Can't convert argument's type : " + e.getMessage());
            return false;
        }

        try {
            CURRENT_APPLICATION.start();
        } catch (Exception e) {
            Logger.error("Can't start properly app '" + currentApplication.getTag() + "' : " + e.getMessage());
            return false;
        }
        return true;
    }


    public static int getIntProperty(Property property) {
        return CURRENT_APPLICATION.getIntProperty(property);
    }

    public static float getFloatProperty(Property property) {
        return CURRENT_APPLICATION.getFloatProperty(property);
    }

    public static <T> T getTProperty(Property property) {
        return CURRENT_APPLICATION.getTProperty(property);
    }
}