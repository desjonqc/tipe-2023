package com.cegesoft.simulation;

import com.cegesoft.log.Logger;
import com.cegesoft.log.ProgressBar;
import lombok.Getter;

public class Job extends Thread implements ProgressBar.PBField {

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
            Logger.error("Job is not done yet!");
            return null;
        }
        return executable;
    }
}
