package com.github.rinorsi.cadeditor.neoforge;

import com.github.rinorsi.cadeditor.common.network.NetworkHandler;
import com.github.rinorsi.cadeditor.common.network.PacketSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformUtilImpl {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    private static final String VERSION = "3";
    private static final Object REGISTRATION_LOCK = new Object();
    private static final List<NetworkHandler.Server<?>> PENDING_SERVER_HANDLERS = new ArrayList<>();
    private static final List<NetworkHandler.Client<?>> PENDING_CLIENT_HANDLERS = new ArrayList<>();
    private static final Map<NetworkHandler<?>, CustomPacketPayload.Type<?>> TYPES = new ConcurrentHashMap<>();
    private static PayloadRegistrar activeRegistrar;

    public static <P> void sendToServer(NetworkHandler.Server<P> handler, P packet) {
        ClientPacketDistributor.sendToServer(wrap(handler, packet));
    }

    public static <P> void sendToClient(ServerPlayer player, NetworkHandler.Client<P> handler, P packet) {
        PacketDistributor.sendToPlayer(player, wrap(handler, packet));
    }

    public static <P> void registerServerHandler(NetworkHandler.Server<P> handler) {
        synchronized (REGISTRATION_LOCK) {
            if (activeRegistrar != null) {
                registerServerHandler(activeRegistrar, handler);
            } else {
                PENDING_SERVER_HANDLERS.add(handler);
            }
        }
    }

    public static <P> void registerClientHandler(NetworkHandler.Client<P> handler) {
        synchronized (REGISTRATION_LOCK) {
            if (activeRegistrar != null) {
                registerClientHandler(activeRegistrar, handler);
            } else {
                PENDING_CLIENT_HANDLERS.add(handler);
            }
        }
    }

    static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ForgeCADEditorMod.MOD_ID)
                .versioned(VERSION)
                .optional()
                .executesOn(HandlerThread.MAIN);
        synchronized (REGISTRATION_LOCK) {
            activeRegistrar = registrar;
            PENDING_SERVER_HANDLERS.forEach(handler -> registerServerHandler(registrar, handler));
            PENDING_CLIENT_HANDLERS.forEach(handler -> registerClientHandler(registrar, handler));
            PENDING_SERVER_HANDLERS.clear();
            PENDING_CLIENT_HANDLERS.clear();
        }
    }

    private static <P> void registerServerHandler(PayloadRegistrar registrar, NetworkHandler.Server<P> handler) {
        registrar.playToServer(type(handler), codec(handler), (payload, context) -> handleServer(handler, payload.packet(), context));
    }

    private static <P> void registerClientHandler(PayloadRegistrar registrar, NetworkHandler.Client<P> handler) {
        registrar.playToClient(type(handler), codec(handler), (payload, context) -> handleClient(handler, payload.packet(), context));
    }

    private static <P> void handleServer(NetworkHandler.Server<P> handler, P packet, IPayloadContext context) {
        context.enqueueWork(() -> handler.getPacketHandler().handle((ServerPlayer) context.player(), packet));
    }

    private static <P> void handleClient(NetworkHandler.Client<P> handler, P packet, IPayloadContext context) {
        context.enqueueWork(() -> handler.getPacketHandler().handle(packet));
    }

    private static <P> StreamCodec<RegistryFriendlyByteBuf, WrappedPayload<P>> codec(NetworkHandler<P> handler) {
        PacketSerializer<P> serializer = handler.getSerializer();
        return StreamCodec.of((buf, payload) -> serializer.write(payload.packet(), buf), buf -> wrap(handler, serializer.read(buf)));
    }

    @SuppressWarnings("unchecked")
    private static <P> CustomPacketPayload.Type<WrappedPayload<P>> type(NetworkHandler<P> handler) {
        return (CustomPacketPayload.Type<WrappedPayload<P>>) TYPES.computeIfAbsent(handler, PlatformUtilImpl::createType);
    }

    private static CustomPacketPayload.Type<?> createType(NetworkHandler<?> handler) {
        ResourceLocation location = handler.getLocation();
        return new CustomPacketPayload.Type<>(location);
    }

    private static <P> WrappedPayload<P> wrap(NetworkHandler<P> handler, P packet) {
        return new WrappedPayload<>(packet, type(handler));
    }

    private record WrappedPayload<P>(P packet, CustomPacketPayload.Type<WrappedPayload<P>> type) implements CustomPacketPayload {
        @Override
        public CustomPacketPayload.Type<WrappedPayload<P>> type() {
            return type;
        }
    }
}
