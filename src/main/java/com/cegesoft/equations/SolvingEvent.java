package com.cegesoft.equations;

/**
 * Évènement de résolution d'équation
 */
public interface SolvingEvent {
    /**
     * Attend que l'équation soit résolue.
     */
    void waitFor();

}
