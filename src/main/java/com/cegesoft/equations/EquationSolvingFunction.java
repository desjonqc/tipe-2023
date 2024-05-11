package com.cegesoft.equations;

import com.cegesoft.opencl.CLField;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

/**
 * Fonction de résolution d'équations.
 */
public interface EquationSolvingFunction {

    /**
     * Appele la fonction OpenCL pour résoudre une équation.
     * @param queue la queue d'appel OpenCL
     * @param dimensions les dimensions de résolution (max 3)
     * @param eventsToWait les évènements à attendre
     * @return l'évènement de fin de résolution
     */
    SolvingEvent call(CLQueue queue, int[] dimensions, CLEvent... eventsToWait);

    <T> void setArgument(int i, CLField<T> field);
    CLField<?> getArgument(int i);
    int getArgumentCount();
}
