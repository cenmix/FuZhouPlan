package org.fuzhou.fuzhouplan.event;

import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.fuzhou.fuzhouplan.Fuzhouplan;

@Mod.EventBusSubscriber(modid = Fuzhouplan.MODID)
public class AnestheticEventHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        for (var server : event.getServer().getAllLevels()) {
            for (var entity : server.getAllEntities()) {
                if (entity instanceof Mob mob) {
                    var data = mob.getPersistentData();
                    
                    if (data.getBoolean("Anesthetized")) {
                        int timer = data.getInt("AnestheticTimer");
                        timer--;
                        
                        if (timer <= 0) {
                            data.remove("Anesthetized");
                            data.remove("AnestheticTimer");
                            mob.setNoAi(false);
                        } else {
                            data.putInt("AnestheticTimer", timer);
                            mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0);
                        }
                    }
                }
            }
        }
    }
}
