package net.hypixel.lynx.chat;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TempChatInterceptor {
   public TempChatInterceptor() {
      MinecraftForge.EVENT_BUS.register(this);
   }

   @SubscribeEvent
   public void onChat(ClientChatReceivedEvent ev) {
      if (ev.message.getUnformattedText().startsWith("[CHAT REPORT] Please type /chatreport confirm")) {
         ev.setCanceled(true);
         ChatFacade.get().sendMessage("/chatreport confirm", false);
      }

   }
}
