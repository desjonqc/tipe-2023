package com.cegesoft.simulation;

import com.cegesoft.log.Logger;
import com.cegesoft.log.ProgressBar;

/**
 * Classe abstraite permettant de gérer plusieurs jobs.
 */
public abstract class MultipleJobHandler extends Thread {
    protected Job[] jobs;
    protected long start;
    protected final ProgressBar progressBar = Logger.getOrCreateProgressBar();

    public MultipleJobHandler() {
        super("JobHandler");
    }

    public abstract Job[] createJobs();

    public abstract void handleResults();

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
        start = System.currentTimeMillis();
        this.jobs = createJobs();
        if (this.jobs.length == 0) {
            return;
        }
        for (Job job : this.jobs) {
            progressBar.addField(job);
            job.start();
        }

        for (Job job : this.jobs) {
            try {
                job.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Logger.print("\n");
        this.handleResults();
        Logger.info("Jobs completed in " + (System.currentTimeMillis() - start) + "ms");
    }
}
