package com.cegesoft.data;

import com.cegesoft.data.exception.WrongFileMetadataException;
import com.cegesoft.data.metadata.FileMetadata;

/**
 * Représente un objet pouvant être stocké sous forme de bytes.
 * L'implémentation doit nécessairement admettre un constructeur sans paramètre.
 */
public interface ByteStorable {

    /**
     * Convertit l'objet en bytes.
     * @return les bytes de l'objet
     */
    byte[] toBytes();

    /**
     * Convertit les bytes en objet.
     * @param bytes les bytes de l'objet
     */
    void fromBytes(byte[] bytes);

    /**
     * Retourne la taille de l'objet en bytes.
     * @return la taille de l'objet
     */
    int size();

    /**
     * Retourne les métadonnées de l'objet.
     * @return les métadonnées de l'objet
     */
    FileMetadata getMetadata();

    /**
     * Définit les métadonnées de l'objet.
     * @param meta les métadonnées de l'objet
     * @throws WrongFileMetadataException si les métadonnées ne sont pas du bon type (suivant le type de ByteStorable)
     */
    void setMetadata(FileMetadata meta) throws WrongFileMetadataException;

}
