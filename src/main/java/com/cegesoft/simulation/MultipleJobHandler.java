package com.cegesoft.simulation;

import com.cegesoft.util.ProgressBar;
import lombok.Getter;

public abstract class MultipleJobHandler extends Thread {
    protected Job[] jobs;
    protected long start;

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

    protected void printProgress() {
        int done = 0;
        int total = 0;
        for (Job job : jobs) {
            done += job.getIndex();
            total += job.getTotal();
        }
        ProgressBar.printProgress(done, total);
    }

    @Override
    public void run() {
        start = System.currentTimeMillis();
        this.jobs = createJobs();
        if (this.jobs.length == 0) {
            return;
        }
        for (Job job : this.jobs) {
            job.start();
        }
        while (!this.allDone()) {
            try {
                Thread.sleep(200);
                this.printProgress();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.handleResults();
        System.out.println("Jobs completed in " + (System.currentTimeMillis() - start) + "ms");
    }
}
