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
            Logger.info("Application '" + applicationId + "' not found.");
            return;
        }
        Logger.info("============= HELP =============");
        if (appImpl == null) {
            Logger.info("Usage : app={ApplicationID} to start an application.");
            Logger.info("Applications available :");
            for (ApplicationsImpl impl : ApplicationsImpl.values()) {
                Logger.info("\t - " + impl.getTag() + " : " + impl.getDescription());
            }
            Logger.info("Use (app=help) --apph={ApplicationID} for more informations about an application.");
            Logger.info("Note : (Optional) | [Startup Command] | {Placeholder}.");
        } else {
            Logger.info("[HELP] Usage : app=" + applicationId + " --{ARGS}");
            Logger.info("Argument list :");
            for (ApplicationArgument<?> argument : appImpl.getApplication().getArguments()) {
                Logger.info("\t• " + argument.getPrefix());
                Logger.info("\t\t - " + (argument.isRequired() ? ConsoleColors.RED_BOLD + "R" : "Not r") + "equired" + ConsoleColors.RESET);
                Logger.info("\t\t - Description : " + argument.getDescription());
                if (argument.getDefaultValue() != null)
                    Logger.info("\t\t - Default value : " + argument.getDefaultValue());
            }
        }

        Logger.info("================================");

        Logger.info("");

        Logger.info(ConsoleColors.RED_BRIGHT + "Type 'quit' to exit the application." + ConsoleColors.RESET);

        Main.listenCommand();
    }

    @Override
    protected boolean readArgument(ApplicationArgument<?> argument, Object value) {
        applicationId = (String) value;
        return false;
    }
}
