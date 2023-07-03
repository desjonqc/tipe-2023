package com.cegesoft.help;

import com.cegesoft.Main;
import com.cegesoft.app.Application;
import com.cegesoft.app.ApplicationsImpl;
import com.cegesoft.app.argument.ApplicationArgument;
import com.cegesoft.log.ConsoleColors;
import com.cegesoft.log.Logger;

public class HelpApplication extends Application {
    private String applicationId = "none";
    public HelpApplication() {
        this.registerArgument(new ApplicationArgument<>(false, "apph", applicationId, "Select an application (none for global menu)"));
    }
    @Override
    public void start() throws Exception {
        ApplicationsImpl appImpl = ApplicationsImpl.getByTag(applicationId);
        if (appImpl == null && !applicationId.equals("none")) {
            Logger.getLogger().println("Application '" + applicationId + "' not found.");
            return;
        }
        Logger.getLogger().println("============= HELP =============");
        if (appImpl == null) {
            Logger.getLogger().println("Usage : app={ApplicationID} to start an application.");
            Logger.getLogger().println("Applications available :");
            for (ApplicationsImpl impl : ApplicationsImpl.values()) {
                Logger.getLogger().println("\t - " + impl.getTag() + " : " + impl.getDescription());
            }
            Logger.getLogger().println("Use (app=help) --apph={ApplicationID} for more informations about an application.");
            Logger.getLogger().println("Note : (Optional) | [Startup Command] | {Placeholder}.");
        } else {
            Logger.getLogger().println("[HELP] Usage : app=" + applicationId + " --{ARGS}");
            Logger.getLogger().println("Argument list :");
            for (ApplicationArgument<?> argument : appImpl.getApplication().getArguments()) {
                Logger.getLogger().println("\t• " + argument.getPrefix());
                Logger.getLogger().println("\t\t - " + (argument.isRequired() ? ConsoleColors.RED_BOLD + "R" : "Not r") + "equired" + ConsoleColors.RESET);
                Logger.getLogger().println("\t\t - Description : " + argument.getDescription());
            }
        }

        Logger.getLogger().println("================================");

        Logger.getLogger().println("");

        Main.listenCommand();
    }

    @Override
    protected boolean readArgument(ApplicationArgument<?> argument, Object value) {
        applicationId = (String) value;
        return false;
    }
}
