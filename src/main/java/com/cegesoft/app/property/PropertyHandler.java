package com.cegesoft.app.property;

/**
 * Interface de gestion de propriété.
 */
public interface PropertyHandler {

    void setProperty(Property property, Object o);
    Object getProperty(Property property);

    int getIntProperty(Property property);
    float getFloatProperty(Property property);
    <T> T getTProperty(Property property);

}
