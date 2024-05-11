package com.cegesoft.simulation;

import com.cegesoft.log.Logger;
import com.cegesoft.log.ProgressBar;

/**
 * Classe abstraite permettant de gérer plusieurs jobs.
 * @see Job
 */
public abstract class MultipleJobHandler extends Thread {
    protected Job[] jobs;
    protected long start;
    protected final ProgressBar progressBar = Logger.getOrCreateProgressBar();

    public MultipleJobHandler() {
        super("JobHandler");
    }

    /**
     * Crée les jobs à exécuter
     * @return les jobs
     */
    public abstract Job[] createJobs();

    /**
     * Traite les résultats des jobs
     */
    public abstract void handleResults();

    /**
     * @return true si tous les jobs sont terminés
     */
    public boolean allDone() {
        for (Job job : jobs) {
            if (!job.isDone()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        // Mesure du temps d'exécution
        start = System.currentTimeMillis();
        this.jobs = createJobs();
        if (this.jobs.length == 0) {
            return;
        }
        for (Job job : this.jobs) {
            progressBar.addField(job); // Ajoute la progression des Jobs à la bar de progression
            job.start(); // Démarre les jobs
        }

        for (Job job : this.jobs) {
            try {
                job.join(); // Attend la fin des jobs
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        }

        Logger.print("\n");
        this.handleResults(); // Traite les résultats
        Logger.info("Jobs completed in " + (System.currentTimeMillis() - start) + "ms");
    }
}
