package com.cegesoft.data;

import com.cegesoft.data.handlers.StorageHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;

public class StorageManager {

    private static final HashMap<StorageTag, StorageHandler> handlers = new HashMap<>();

    public static StorageHandler get(StorageTag tag) {
        if (!handlers.containsKey(tag)) {
            throw new IllegalStateException("Handler must be registered before accessed");
        }
        return handlers.get(tag);
    }

    public static void register(StorageTag tag, StorageHandler handler) {
        if (handlers.containsKey(tag)) {
            throw new IllegalStateException("HandlerTag already refers to a StorageHandler");
        }
        handlers.put(tag, handler);
    }

    @AllArgsConstructor
    public enum StorageTag {
        AI_DATA("ai_data"),
        STATISTIC_POSITION("statistics");
        @Getter
        private final String name;
    }

}
