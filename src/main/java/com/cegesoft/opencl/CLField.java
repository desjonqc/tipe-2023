package com.cegesoft.opencl;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import lombok.Getter;
import org.bridj.Pointer;

/**
 * Classe représentant un champ de données OpenCL.
 * @param <T> Type de données
 */
public class CLField<T> {

    @Getter
    protected final CLHandler handler;
    @Getter
    protected final CLMem.Usage type;
    @Getter
    protected final long size;
    @Getter
    protected final Class<T> tClass;
    @Getter
    protected Object argument;

    /**
     * Constructeur pour une donnée mutable (Tableau de données)
     * @param handler Gestionnaire OpenCL
     * @param type Type d'accès
     * @param tClass Classe de la donnée
     * @param size Taille du tableau
     */
    public CLField(CLHandler handler, CLMem.Usage type, Class<T> tClass, long size) {
        this.type = type;
        this.size = size;
        this.tClass = tClass;
        this.handler = handler;
        if (type != CLMem.Usage.Input || size != 1) {
            argument = handler.getContext().createBuffer(type, tClass, size);
        }
    }

    /**
     * Constructeur pour une donnée constante (Valeur)
     * @param handler Gestionnaire OpenCL
     * @param tClass Classe de la donnée
     * @param value Valeur
     */
    public CLField(CLHandler handler, Class<T> tClass, T value) {
        this(handler, CLMem.Usage.Input, tClass, 1);
        this.argument = value;
    }

    /**
     * Met à jour les arguments du kernel OpenCL
     * @param queue Queue JavaCL
     * @param index Index de l'argument
     * @param value Valeur de l'argument
     * @return L'évènement de mise à jour
     */
    public CLEvent setValue(CLQueue queue, long index, T value) {
        if (this.argument instanceof CLBuffer) {
            Pointer<T> pointer = ((CLBuffer<T>) this.argument).read(queue);
            pointer.set(index, value);
            return ((CLBuffer<T>) this.argument).write(queue, pointer, false);
        }
        this.argument = value;
        return null;
    }

    /**
     * Copie le champ dans un autre contenant CField
     * @param queue Queue JavaCL
     * @return Le champ copié
     */
    public CLField<T> duplicate(CLQueue queue) {
        if (this.argument instanceof CLBuffer) {
            CLField<T> field = new CLField<>(this.handler, this.type, this.tClass, this.size);
            ((CLBuffer<?>) this.argument).copyTo(queue, (CLBuffer<T>) field.argument).waitFor();
            return field;
        }
        return new CLField<>(this.handler, this.tClass, (T) this.argument);
    }
}
