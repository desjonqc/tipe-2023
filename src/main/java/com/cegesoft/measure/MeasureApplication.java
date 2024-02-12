package com.cegesoft.measure;

import com.cegesoft.Main;
import com.cegesoft.app.Application;
import com.cegesoft.app.argument.ApplicationArgument;
import com.cegesoft.log.Logger;
import com.cegesoft.opencl.*;
import com.nativelibs4java.opencl.CLMem;

public class MeasureApplication extends Application {

    private boolean opencl = false;
    private int N = 100000000;

    public MeasureApplication() {
        this.registerArgument(new ApplicationArgument<>(false, "opencl", false, "Use OpenCL to calculate the measure"));
        this.registerArgument(new ApplicationArgument<>(false, "count", 100000000, "Amount of iterations"));
    }
    @Override
    public void start() throws Exception {
        if (this.opencl) {
            CLHandler handler = new CLHandler();
            CLFile file = new CLFile("measure.cl", handler.getContext());
            CLFunction function = new CLFunction(file, "flops", new CLField<>(handler, CLMem.Usage.InputOutput, Float.class, (long) N), new CLConstantField<>(handler, Integer.class, (int)Math.sqrt(N)));
            long time = System.currentTimeMillis();
            function.call(handler.createQueue(), new int[]{(int)Math.sqrt(N), (int)Math.sqrt(N)}).waitFor();
            double finalTime = (System.currentTimeMillis() - time) / 1000.0d;
            Logger.info("Time: " + finalTime + "s");
            Logger.info("Flops: " + N/finalTime);
        } else {
            float[] a = new float[N];
            long time = System.currentTimeMillis();
            for (int i = 0; i < N; i++) {
                a[i] = i * 0.5f;
            }
            double finalTime = (System.currentTimeMillis() - time) / 1000.0d;
            Logger.info("Time: " + finalTime + "s");
            Logger.info("Flops: " + N/finalTime);
        }
        Main.listenCommand();
    }

    @Override
    protected boolean readArgument(ApplicationArgument<?> argument, Object value) {
        if (argument.getPrefix().equalsIgnoreCase("opencl")) {
            this.opencl = (boolean) value;
        }
        if (argument.getPrefix().equalsIgnoreCase("count")) {
            this.N = (int) value;
        }
        return false;
    }
}
