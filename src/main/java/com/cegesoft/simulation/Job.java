package com.cegesoft.simulation;

import com.cegesoft.log.Logger;
import com.cegesoft.log.ProgressBar;
import lombok.Getter;

/**
 * Représente un travail à effectuer. Un travail est une tâche qui peut être exécutée en parallèle.
 * Peut être utilisé pour afficher une barre de progression.
 * @see ProgressBar.PBField
 */
public class Job extends Thread implements ProgressBar.PBField {

    private final IJobExecutable executable;
    @Getter
    private final int total;
    @Getter
    private int index;
    @Getter
    private boolean done = false;

    /**
     * Crée un travail
     * @param executable la tâche à effectuer
     * @param total le nombre d'itérations à effectuer
     */
    public Job(IJobExecutable executable, int total) {
        this.executable = executable;
        this.total = total;
    }

    @Override
    public void run() {
        for (index = 0; index < total; index++) {
            executable.run(index);
        }
        executable.reset();
        done = true;
    }

    public IJobExecutable getExecutable() {
        if (!done) {
            Logger.error("Job is not done yet!");
            return null;
        }
        return executable;
    }
}
