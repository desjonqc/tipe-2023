package com.cegesoft.opencl;

import com.cegesoft.equations.EquationSolvingFunction;
import com.cegesoft.equations.SolvingEvent;
import com.nativelibs4java.opencl.*;
import lombok.Getter;
import org.bridj.Pointer;

import java.util.Arrays;

public class CLFunction implements EquationSolvingFunction {

    private final CLFile file;
    private final CLField<?>[] fields;
    private final CLKernel kernel;
    private CLEvent event;
    public CLFunction(CLFile file, String name, CLField<?>... fields) {
        this.file = file;
        this.fields = fields;
        this.kernel = file.getProgram().createKernel(name);
        this.updateArgs();
    }

    private void updateArgs() {
        Object[] args = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            args[i] = fields[i].getArgument();
        }
        this.kernel.setArgs(args);
    }

    public CLFunctionEvent call(CLQueue queue, int[] range, CLEvent... eventsToWait) {
        return new CLFunctionEvent(this.kernel.enqueueNDRange(queue, range, eventsToWait));
    }

    public <T> Pointer<T> getOutput(int fieldId, CLQueue queue, CLEvent... eventsToWait) {
        CLField<T> field = (CLField<T>) this.fields[fieldId];
        if (field.getType() != CLMem.Usage.Output && field.getType() != CLMem.Usage.InputOutput)
            return null;
        CLBuffer<T> buffer = (CLBuffer<T>) field.getArgument();
        return buffer.read(queue, eventsToWait);
    }

    public <T> void setArgument(int i, CLField<T> field) {
        fields[i] = field;
        this.updateArgs();
    }

    public CLField<?> getArgument(int i) {
        return fields[i];
    }

    public int getArgumentCount() {
        return fields.length;
    }

    public static class CLFunctionEvent implements SolvingEvent {
        private final CLEvent event;

        public CLFunctionEvent(CLEvent event) {
            this.event = event;
        }

        @Override
        public void waitFor() {
            this.event.waitFor();
        }
    }
}
