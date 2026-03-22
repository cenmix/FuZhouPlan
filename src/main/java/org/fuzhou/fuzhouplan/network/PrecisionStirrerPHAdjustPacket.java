package org.fuzhou.fuzhouplan.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.fuzhou.fuzhouplan.blockentity.PrecisionStirrerBlockEntity;

import java.util.function.Supplier;

/**
 * 精密搅拌器PH值调整网络包
 * 用于从客户端向服务器发送PH值调整请求
 */
public class PrecisionStirrerPHAdjustPacket {

    private final BlockPos blockPos;
    private final int adjustment; // 正数表示增加，负数表示减少

    public PrecisionStirrerPHAdjustPacket(BlockPos blockPos, int adjustment) {
        this.blockPos = blockPos;
        this.adjustment = adjustment;
    }

    public PrecisionStirrerPHAdjustPacket(FriendlyByteBuf buffer) {
        this.blockPos = buffer.readBlockPos();
        this.adjustment = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeInt(adjustment);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 在服务器端处理
            ServerPlayer player = context.getSender();
            if (player != null) {
                BlockEntity blockEntity = player.level().getBlockEntity(blockPos);
                if (blockEntity instanceof PrecisionStirrerBlockEntity stirrerEntity) {
                    stirrerEntity.adjustPlayerPH(adjustment);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
