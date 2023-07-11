package com.cegesoft.data;

import com.cegesoft.data.exception.StorageHandlerRegistrationException;
import com.cegesoft.data.handlers.StorageHandler;
import com.cegesoft.log.Logger;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;

public class StorageManager {

    private static final HashMap<StorageTag, StorageHandler> handlers = new HashMap<>();

    public static StorageHandler get(StorageTag tag) throws StorageHandlerRegistrationException {
        if (!handlers.containsKey(tag)) {
            throw new StorageHandlerRegistrationException("Handler must be registered before accessed");
        }
        return handlers.get(tag);
    }

    public static void register(StorageTag tag, StorageHandler handler) {
        if (handlers.containsKey(tag)) {
            Logger.warn("HandlerTag '" + tag.name + "' already refers to a StorageHandler");
            return;
        }
        handlers.put(tag, handler);
    }

    public static void unregister(StorageTag storageTag) {
        handlers.remove(storageTag);
    }

    @AllArgsConstructor
    public enum StorageTag {
        AI_DATA("ai_data"),
        STATISTIC_POSITION("statistics");
        @Getter
        private final String name;
    }

}
