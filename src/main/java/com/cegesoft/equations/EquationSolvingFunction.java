package com.cegesoft.equations;

import com.cegesoft.opencl.CLField;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public interface EquationSolvingFunction {

    SolvingEvent call(CLQueue queue, int[] dimensions, CLEvent... eventsToWait);

    <T> void setArgument(int i, CLField<T> field);
    CLField<?> getArgument(int i);
    int getArgumentCount();
}
