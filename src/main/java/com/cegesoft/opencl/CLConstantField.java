package com.cegesoft.opencl;

/**
 * Classe représentant un champ de données OpenCL constant.
 * @param <T> Type de données
 */
public class CLConstantField<T> extends CLField<T> {

    public CLConstantField(CLHandler handler, Class<T> tClass, T value) {
        super(handler, tClass, value);
    }

    @Override
    public T getArgument() {
        return (T) super.getArgument();
    }
}
