package com.cegesoft.opencl;

public class CLConstantField<T> extends CLField<T> {

    public CLConstantField(CLHandler handler, Class<T> tClass, T value) {
        super(handler, tClass, value);
    }

    @Override
    public T getArgument() {
        return (T) super.getArgument();
    }
}
