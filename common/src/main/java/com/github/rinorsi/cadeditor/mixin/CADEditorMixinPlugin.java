package com.github.rinorsi.cadeditor.mixin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.InjectionPoint;

/**
 * Ensures that Mixin Extras is initialised when present so that dependent mods
 * can safely register their custom injection points during startup.
 */
public final class CADEditorMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("CAD Editor");
    private static final String MIXIN_EXTRAS_BOOTSTRAP = "com.llamalad7.mixinextras.MixinExtrasBootstrap";

    @Override
    public void onLoad(String mixinPackage) {
        try {
            Class<?> bootstrapClass = Class.forName(MIXIN_EXTRAS_BOOTSTRAP);
            Method initMethod = bootstrapClass.getMethod("init");
            initMethod.invoke(null);
            registerUppercaseMixinExtrasAliases();
        } catch (ClassNotFoundException ignored) {
            // Mixin Extras is optional – nothing to initialise.
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            LOGGER.error("Failed to initialise Mixin Extras", exception);
            throw new IllegalStateException("Unable to bootstrap Mixin Extras", exception);
        }
    }

    private void registerUppercaseMixinExtrasAliases() {
        try {
            Field typesField = InjectionPoint.class.getDeclaredField("types");
            typesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> types = (Map<String, Object>) typesField.get(null);
            if (types == null || types.isEmpty()) {
                return;
            }

            Map<String, Object> aliases = new HashMap<>();
            for (Map.Entry<String, Object> entry : types.entrySet()) {
                String id = entry.getKey();
                if (!id.startsWith("mixinextras:")) {
                    continue;
                }

                String uppercaseId = id.toUpperCase(Locale.ROOT);
                if (!types.containsKey(uppercaseId)) {
                    aliases.put(uppercaseId, entry.getValue());
                }
            }

            if (aliases.isEmpty()) {
                return;
            }

            types.putAll(aliases);
            LOGGER.info("Registered {} uppercase alias(es) for Mixin Extras injection points", aliases.size());
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.warn("Failed to register uppercase aliases for Mixin Extras injection points", exception);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // No-op
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // No-op
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // No-op
    }
}
