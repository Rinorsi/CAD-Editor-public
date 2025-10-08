package com.github.rinorsi.cadeditor.client.debug;

import com.github.franckyi.guapi.api.util.DebugMode;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import com.github.rinorsi.cadeditor.client.ClientConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class DebugLog {
    private static final Logger LOGGER = LogManager.getLogger("CAD-Editor/Debug");
    private static final long UI_THROTTLE_MS = 500L;
    private static final Map<String, Long> UI_LAST_EMIT = new ConcurrentHashMap<>();
    private static volatile DebugMode lastAnnouncedMode = null;

    private DebugLog() {
    }

    public static void modeChanged(DebugMode mode) {
        DebugMode safeMode = mode == null ? DebugMode.NONE : mode;
        if (safeMode != lastAnnouncedMode) {
            lastAnnouncedMode = safeMode;
            LOGGER.info("Debug mode switched to {}", safeMode);
        }
    }

    public static void info(String message) {
        info(() -> message);
    }

    public static void infoKey(String translationKey, Object... args) {
        Object[] safeArgs = sanitizeArgs(args);
        info(() -> translate(translationKey, safeArgs));
    }

    public static void info(Supplier<String> messageSupplier) {
        if (currentMode() == DebugMode.INFO) {
            LOGGER.info("[Info] {}", messageSupplier.get());
        }
    }

    public static void ui(String key, Supplier<String> messageSupplier) {
        if (currentMode() != DebugMode.UI) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = UI_LAST_EMIT.put(key, now);
        if (last == null || now - last >= UI_THROTTLE_MS) {
            LOGGER.info("[UI] {}", messageSupplier.get());
        }
    }

    private static DebugMode currentMode() {
        try {
            var cfg = ClientConfiguration.INSTANCE;
            if (cfg == null) {
                return DebugMode.NONE;
            }
            DebugMode mode = cfg.getGuapiDebugMode();
            return mode == null ? DebugMode.NONE : mode;
        } catch (Throwable t) {
            return DebugMode.NONE;
        }
    }

    private static Object[] sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return args;
        }
        Object[] sanitized = null;
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Object safe = sanitizeArg(arg);
            if (sanitized != null) {
                sanitized[i] = safe;
            } else if (safe != arg) {
                sanitized = new Object[args.length];
                System.arraycopy(args, 0, sanitized, 0, i);
                sanitized[i] = safe;
            }
        }
        return sanitized == null ? args : sanitized;
    }

    private static Object sanitizeArg(Object arg) {
        if (arg == null || arg instanceof Component || arg instanceof Number || arg instanceof Boolean || arg instanceof String) {
            return arg;
        }
        return String.valueOf(arg);
    }

    private static String translate(String translationKey, Object[] args) {
        try {
            if (I18n.exists(translationKey)) {
                return I18n.get(translationKey, args);
            }
        } catch (IllegalFormatException e) {
            LOGGER.warn("Failed to format translation {}", translationKey, e);
        }
        if (args == null || args.length == 0) {
            return translationKey;
        }
        return translationKey + " " + Arrays.toString(args);
    }
}
