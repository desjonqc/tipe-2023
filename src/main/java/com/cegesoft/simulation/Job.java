package com.cegesoft.simulation;

import lombok.Getter;

public class Job extends Thread {

    private final IJobExecutable executable;
    @Getter
    private final int total;
    @Getter
    private int index;
    @Getter
    private boolean done = false;

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
            throw new IllegalStateException("Job is not done yet!");
        }
        return executable;
    }
}
