package net.hypixel.lynx.anticheat;

import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.ChatFacade;
import net.hypixel.lynx.util.Util;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ReachNotifier {
    public ReachNotifier() {
        Util.register(this, MinecraftForge.EVENT_BUS);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        if (Lynx.isTargeting() && Lynx.getTargetingUI() != null && Lynx.getTargetingUI().getTarget() != null && e.entity == Lynx.getTargetingUI().getTarget()) {
            if (!Lynx.get().isDoingReachCheckForTarget()) return;

            double dist = e.entity.getDistanceToEntity(e.target);

            if (dist >= 3.3) {
                ChatFacade.get().printChatMessage(new ChatComponentText("\u00A73[TMU / !] \u00A7b" + e.entity.getName() + " \u00A73attacked \u00A7b" + e.target.getName() + " \u00A73from \u00A79" + dist + " blocks\u00A73."));
            }
            else {
                ChatFacade.get().printChatMessage(new ChatComponentText("\u00A73[TMU / ?] \u00A7b" + e.entity.getName() + " \u00A73attacked \u00A7b" + e.target.getName() + " \u00A73from \u00A79" + dist + " blocks\u00A73."));
            }
        }
    }
}
