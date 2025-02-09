package net.hypixel.lynx.anticheat;

import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.ChatFacade;
import net.hypixel.lynx.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class ReachNotifier {
    public ReachNotifier() {
        Util.register(this, MinecraftForge.EVENT_BUS);
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent e) {
        EntityPlayer target = null;
        EntityPlayer entity = null;

        Util.out(e.source.getDamageType() + " | " + e.entity.getName());

        if (e.source != null && e.source.getEntity() instanceof EntityPlayer) {
            entity = (EntityPlayer) e.source.getEntity();
        }
        if (e.entity instanceof EntityPlayer) {
            target = (EntityPlayer) e.entity;
        }

        if (entity == null || target == null) return;

        if (Lynx.isTargeting() && Lynx.getTargetingUI() != null && Lynx.getTargetingUI().getTargetName() != null && entity.getName() == Lynx.getTargetingUI().getTargetName()) {
            if (!Lynx.get().isDoingReachCheckForTarget()) return;

            double dist = entity.getDistanceToEntity(target);

            if (dist >= 3.3) {
                ChatFacade.get().printChatMessage(new ChatComponentText("\u00A73[TMU / !] \u00A7b" + entity.getName() + " \u00A73attacked \u00A7b" + target.getName() + " \u00A73from \u00A79" + dist + " blocks\u00A73."));
            }
            else {
                ChatFacade.get().printChatMessage(new ChatComponentText("\u00A73[TMU / ?] \u00A7b" + entity.getName() + " \u00A73attacked \u00A7b" + target.getName() + " \u00A73from \u00A79" + dist + " blocks\u00A73."));
            }
        }
    }
}
