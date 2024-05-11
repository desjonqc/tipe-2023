package com.cegesoft.app;

import com.cegesoft.Main;
import com.cegesoft.app.argument.ArgumentsReader;
import com.cegesoft.app.property.Property;
import com.cegesoft.app.property.PropertyHandler;
import com.cegesoft.log.Logger;

import java.util.HashMap;

/**
 * Classe abstraite définissant une application : Permet de lancer un programme spécifique en contrôlant ce qui est utilisé.
 * Une application qui ne doit pas lancer de simulation ne chargera pas OpenCL nécessairement.
 */
public abstract class Application extends ArgumentsReader implements PropertyHandler {
    /**
     * Les propriétés sont des valeurs accessibles partout par l'application, et qui sont prépondérantes dans le programme.
     * @see Property
     */
    private final HashMap<Property, Object> properties = new HashMap<>();

    public void setProperty(Property property, Object o) {
        properties.remove(property);
        properties.put(property, o);
    }

    public Object getProperty(Property property) {
        return properties.get(property);
    }

    @Override
    public int getIntProperty(Property property) {
        return (int) getProperty(property);
    }

    @Override
    public float getFloatProperty(Property property) {
        return (float) getProperty(property);
    }

    @Override
    public <T> T getTProperty(Property property) {
        return (T) getProperty(property);
    }

    public abstract void start() throws Exception;
    public void stop() {
        Logger.resetProgressBar();
    }
}
