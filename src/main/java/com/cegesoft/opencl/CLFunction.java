package com.cegesoft.opencl;

import com.cegesoft.equations.EquationSolvingFunction;
import com.cegesoft.equations.SolvingEvent;
import com.nativelibs4java.opencl.*;
import lombok.Getter;
import org.bridj.Pointer;

import java.util.Arrays;

/**
 * Classe représentant une fonction kernel OpenCL, située dans le fichier 'file' et portant le nom 'name'.
 * Les paramètres de la fonction sont sous la forme d'un CLField, et sont soit précisés dans le constructeur,
 * soit modifiées dans la fonction setArguments.
 *
 * @see CLFile
 * @see CLField
 */
public class CLFunction implements EquationSolvingFunction {

    @Getter
    private final CLFile file;
    private final CLField<?>[] fields;
    private final CLKernel kernel;
    public CLFunction(CLFile file, String name, CLField<?>... fields) {
        this.file = file;
        this.fields = fields;
        this.kernel = file.getProgram().createKernel(name);
        this.updateArgs();
    }

    /**
     * Envoie les arguments CLField vers le kernel OpenCL. (La conversion de l'objet vers un pointeur C est effectuée par JavaCL)
     */
    private void updateArgs() {
        Object[] args = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            args[i] = fields[i].getArgument();
        }
        this.kernel.setArgs(args);
    }

    /**
     * Execute la fonction sur la carte graphique associée.
     * @param queue Queue JavaCL
     * @param range Partition du parallélisme
     * @param eventsToWait Évènements OpenCL à attendre avant l'exécution
     * @return L'évènement d'appel de la fonction
     *
     * @see SolvingEvent
     */
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

    /**
     * Modifie l'argument i de la fonction OpenCL
     * @param i l'indice de l'argument
     * @param field le nouvel argument
     * @param <T> le type générique de l'argument
     */
    public <T> void setArgument(int i, CLField<T> field) {
        fields[i] = field;
        this.updateArgs();
    }

    /**
     * @param i l'indice de l'argument
     * @return l'argument en position i
     */
    public CLField<?> getArgument(int i) {
        return fields[i];
    }

    /**
     * @return le nombre d'arguments
     */
    public int getArgumentCount() {
        return fields.length;
    }

    /**
     * Évènement de l'appel d'une fonction
     */
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
