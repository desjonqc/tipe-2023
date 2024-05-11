package com.cegesoft.comparator;

import com.cegesoft.equations.EquationSolvingFunction;
import com.cegesoft.equations.SolvingEvent;
import com.cegesoft.opencl.CLField;
import com.cegesoft.opencl.CLFunction;
import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;
import org.bridj.Pointer;

import java.util.ArrayList;
import java.util.List;

/**
 * Fausse classe pour simuler une classe de la bibliothèque OpenCL
 * @see CLFunction
 */
public class PythonFunction implements EquationSolvingFunction {
    private final List<CLField<?>> fields = new ArrayList<>();
    private final String path;

    public PythonFunction(String path) {
        this.path = path;
    }

    @Override
    public PythonFunctionEvent call(CLQueue queue, int[] dimensions, CLEvent... eventsToWait) {
        String[] args = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            args[i] = convertField(fields.get(i), queue);
        }
        String result = PythonBridge.callPythonScript(this.path, args);
        updateResult(result, queue);
        return new PythonFunctionEvent();
    }

    private String convertField(CLField<?> field, CLQueue queue) {
        if (field.getSize() == 1) {
            return field.getArgument().toString();
        }
        StringBuilder builder = new StringBuilder();
        Pointer<Object> pointer = ((CLBuffer<Object>) field.getArgument()).read(queue);
        for (int i = 0; i < field.getSize(); i++) {
            builder.append(pointer.get(i).toString());
            if (i < field.getSize() - 1) {
                builder.append(";");
            }
        }
        return builder.toString();
    }

    private void updateResult(String result, CLQueue queue) {
        String[] query = result.split("!");
        int index = Integer.parseInt(query[0]);

        if (query[1].contains(";")) {
            // Cas d'un tableau
            String[] values = query[1].split(";");
            for (int i = 0; i < values.length; i++) {
                ((CLField<Float>)fields.get(index)).setValue(queue, i, Float.parseFloat(values[i]));
            }
            return;
        }
        ((CLField<Float>)fields.get(index)).setValue(queue, 0, Float.parseFloat(query[1]));
    }

    @Override
    public <T> void setArgument(int i, CLField<T> field) {
        if (fields.size() > i) {
            fields.set(i, field);
        } else {
            fields.add(i, field);
        }
    }

    @Override
    public CLField<?> getArgument(int i) {
        return fields.get(i);
    }

    @Override
    public int getArgumentCount() {
        return fields.size();
    }

    public static class PythonFunctionEvent implements SolvingEvent {
        @Override
        public void waitFor() {
            // Do nothing
        }
    }
}
