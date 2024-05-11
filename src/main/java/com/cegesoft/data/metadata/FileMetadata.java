package com.cegesoft.data.metadata;

import com.cegesoft.data.ByteStorable;

/**
 * Représente les métadonnées d'un fichier.
 */
public interface FileMetadata extends ByteStorable {
    short getDataGroupSize();
}
