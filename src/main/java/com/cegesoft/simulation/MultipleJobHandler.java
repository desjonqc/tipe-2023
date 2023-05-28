package com.cegesoft.simulation;

import com.cegesoft.util.ProgressBar;
import lombok.Getter;

public abstract class MultipleJobHandler extends Thread {

    @Getter
    private final int jobHandlerId;
    protected Job[] jobs;

    public MultipleJobHandler(int jobHandlerId) {
        super("JobHandler-" + jobHandlerId);
        this.jobHandlerId = jobHandlerId;
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

    private void printProgress() {
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
        this.jobs = createJobs();
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
    }
}
