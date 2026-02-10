package com.github.rinorsi.cadeditor.fabric;

import com.github.rinorsi.cadeditor.common.network.NetworkHandler;
import com.github.rinorsi.cadeditor.common.network.PacketSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PlatformUtilImpl {
    private static final Map<NetworkHandler<?>, CustomPacketPayload.Type<?>> TYPES = new ConcurrentHashMap<>();

    private PlatformUtilImpl() {
    }

    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static <P> void sendToServer(NetworkHandler.Server<P> handler, P packet) {
        ClientPlayNetworking.send(wrap(handler, packet));
    }

    public static <P> void sendToClient(ServerPlayer player, NetworkHandler.Client<P> handler, P packet) {
        ServerPlayNetworking.send(player, wrap(handler, packet));
    }

    public static <P> void registerServerHandler(NetworkHandler.Server<P> handler) {
        CustomPacketPayload.Type<WrappedPayload<P>> type = type(handler);
        PayloadTypeRegistry.playC2S().register(type, codec(handler));
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                context.server().execute(() -> handler.getPacketHandler().handle(context.player(), payload.packet())));
    }

    public static <P> void registerClientHandler(NetworkHandler.Client<P> handler) {
        CustomPacketPayload.Type<WrappedPayload<P>> type = type(handler);
        PayloadTypeRegistry.playS2C().register(type, codec(handler));
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientNetworking.register(type, handler);
        }
    }

    private static <P> StreamCodec<RegistryFriendlyByteBuf, WrappedPayload<P>> codec(NetworkHandler<P> handler) {
        PacketSerializer<P> serializer = handler.getSerializer();
        return StreamCodec.of((buf, payload) -> serializer.write(payload.packet(), buf),
                buf -> wrap(handler, serializer.read(buf)));
    }

    @SuppressWarnings("unchecked")
    private static <P> CustomPacketPayload.Type<WrappedPayload<P>> type(NetworkHandler<P> handler) {
        return (CustomPacketPayload.Type<WrappedPayload<P>>) TYPES.computeIfAbsent(handler, PlatformUtilImpl::createType);
    }

    private static CustomPacketPayload.Type<?> createType(NetworkHandler<?> handler) {
        Identifier location = handler.getLocation();
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

    private static final class ClientNetworking {
        private ClientNetworking() {
        }

        private static <P> void register(CustomPacketPayload.Type<WrappedPayload<P>> type, NetworkHandler.Client<P> handler) {
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                    context.client().execute(() -> handler.getPacketHandler().handle(payload.packet())));
        }
    }
}
