package net.hypixel.lynx.ui.key;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.ChatFacade;
import net.hypixel.lynx.config.ClientConfig;
import net.hypixel.lynx.ui.MenuHandler;
import net.hypixel.lynx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class KeyListener {
   private final AtomicBoolean stupidEventGetsCalledTwice = new AtomicBoolean();

   public KeyListener() {
      Util.register(this, MinecraftForge.EVENT_BUS);
      Util.register(this, FMLCommonHandler.instance().bus());
      Keys.registerBinding(KeyBinding.of(211), Util::runTestable);
      Keys.registerBinding(KeyBindings.NEXT_TAB, () -> {
         Lynx.getChat().shiftTab(!KeyBinding.isReversed());
      });
      Keys.registerBinding(KeyBinding.of(29, 211), () -> {
         Util.out("Saving chat tab configuration...");
         ClientConfig.TABS.set(ChatFacade.get().getTabs());

         try {
            ClientConfig.TABS.save();
         } catch (IOException var1) {
            System.err.println("Error saving config");
            var1.printStackTrace(System.err);
            System.err.flush();
         }

      });
      Keys.registerBinding(KeyBindings.SELECT_TARGET, () -> {
         return !Lynx.getChat().getChatOpen();
      }, () -> {
         MovingObjectPosition objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
         if (objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectType.ENTITY && objectMouseOver.entityHit instanceof EntityPlayer) {
            Lynx.target(objectMouseOver.entityHit.getName());
         }

      });
   }

   @SubscribeEvent
   public void keyInput(KeyInputEvent event) {
      this.keyFired();
   }

   @SubscribeEvent
   public void keyInputInScreen(KeyboardInputEvent event) {
      if (this.stupidEventGetsCalledTwice.getAndSet(!this.stupidEventGetsCalledTwice.get())) {
         this.keyFired();
      }

   }

   private void keyFired() {
      Keys.updateTick();
      if (!Lynx.getChat().getChatOpen()) {
         if (Lynx.isTargeting()) {
            Lynx.getTargetingUI().typed();
         }

         MenuHandler.keyTyped();
      }

   }
}
