package org.fuzhou.fuzhouplan.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.fuzhou.fuzhouplan.Fuzhouplan;

/**
 * 网络包处理器
 * 注册和处理所有自定义网络包
 */
public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel INSTANCE;

    public static void register() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(Fuzhouplan.MODID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        // 注册精密搅拌器PH值调整包
        INSTANCE.messageBuilder(PrecisionStirrerPHAdjustPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PrecisionStirrerPHAdjustPacket::encode)
                .decoder(PrecisionStirrerPHAdjustPacket::new)
                .consumerMainThread(PrecisionStirrerPHAdjustPacket::handle)
                .add();
    }

    public static void sendToServer(Object message) {
        INSTANCE.sendToServer(message);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToAll(Object message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
