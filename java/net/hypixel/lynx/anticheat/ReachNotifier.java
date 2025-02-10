package net.hypixel.lynx.anticheat;

import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.ChatFacade;
import net.hypixel.lynx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class ReachNotifier {
    public ReachNotifier() {
        Util.register(this, MinecraftForge.EVENT_BUS);
    }

    public static boolean swung = false;
    public static long lastValidSwingTime = 0L;
    public static boolean nearestTookDamage = false;
    public static boolean sentAlert = false;

    @SubscribeEvent
    public void onTick(TickEvent e) {
        // TODO: Replace this with a better method

        if (e.phase == TickEvent.Phase.START) {
            swung = false;
            nearestTookDamage = false;
            if (System.currentTimeMillis() - lastValidSwingTime >= 250L) { // usually 500 ms
                lastValidSwingTime = 0L;
                sentAlert = false;
            }
        }

        if (e.phase == TickEvent.Phase.END && Lynx.isTargeting() && Lynx.getTargetingUI() != null && Lynx.getTargetingUI().getTarget() != null && Lynx.get().isDoingReachCheckForTarget()) {
            double dist = Double.MAX_VALUE;
            EntityPlayer nearestPlayer = null;

            for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                // >= 1.0 to prevent it from being the swinger/attacker
                if (player.getDistanceToEntity(Lynx.getTargetingUI().getTarget()) < dist && player.getDistanceToEntity(Lynx.getTargetingUI().getTarget()) >= 1.0) {
                    dist = player.getDistanceToEntity(Lynx.getTargetingUI().getTarget());
                    nearestPlayer = player;
                }
            }

            if (nearestPlayer != null && nearestPlayer.hurtTime > 0) {
                nearestTookDamage = true;
            }

            if (Lynx.getTargetingUI().getTarget().isSwingInProgress && lastValidSwingTime <= 0L) {
                swung = true;
                lastValidSwingTime = System.currentTimeMillis();
            }

            EntityPlayer entity = Lynx.getTargetingUI().getTarget();

            // Actually send the message
            if (swung && nearestTookDamage && System.currentTimeMillis() - lastValidSwingTime <= 100L && !sentAlert) {
                if (dist >= 3.3) {
                    ChatFacade.get().printChatMessage(new ChatComponentText("\u00A73[TMU / !] \u00A7b" + entity.getName() + " \u00A73attacked \u00A7b" + nearestPlayer.getName() + " \u00A73from \u00A79" + dist + " blocks\u00A73."));
                }
                else {
                    ChatFacade.get().printChatMessage(new ChatComponentText("\u00A73[TMU / ?] \u00A7b" + entity.getName() + " \u00A73attacked \u00A7b" + nearestPlayer.getName() + " \u00A73from \u00A79" + dist + " blocks\u00A73."));
                }

                sentAlert = true;
            }

        }
    }
}
