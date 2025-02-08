package net.hypixel.lynx.ui;

import java.util.HashSet;
import java.util.Set;
import net.hypixel.lynx.ui.key.KeyBinding;
import net.hypixel.lynx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

public enum MenuHandler {
   INSTANCE;

   private static final MenuHandler.MouseState MOUSE = new MenuHandler.MouseState();
   private final Set<Menu> menus = new HashSet();

   private MenuHandler() {
      Util.register(this, MinecraftForge.EVENT_BUS);
   }

   public static void keyTyped() {
      INSTANCE.menus.forEach(Menu::onKeyTyped);
   }

   public static void click(boolean right) {
      INSTANCE.menus.forEach((m) -> {
         m.click(right);
      });
   }

   public static void scroll(boolean up, boolean slow) {
      INSTANCE.menus.forEach((m) -> {
         m.onScroll(up, slow);
      });
   }

   public static void register(Menu menu) {
      INSTANCE.menus.add(menu);
   }

   @SubscribeEvent
   public void onMenuUpdate(RenderGameOverlayEvent event) {
      if (event.type == ElementType.CROSSHAIRS) {
         INSTANCE.menus.forEach((m) -> {
            m.doRender(Minecraft.getMinecraft().ingameGUI.getUpdateCounter() + (int)event.partialTicks);
         });
         MOUSE.query();
      }

   }

   private static class MouseState {
      private boolean mouseLeftDown;
      private boolean mouseRightDown;

      private MouseState() {
         this.mouseLeftDown = false;
         this.mouseRightDown = false;
      }

      public void query() {
         boolean leftDown = Mouse.isButtonDown(0);
         boolean rightDown = Mouse.isButtonDown(1);
         if (leftDown && !this.mouseLeftDown) {
            this.mouseLeftDown = true;
            MenuHandler.click(false);
         }

         if (rightDown && !this.mouseRightDown) {
            this.mouseRightDown = true;
            MenuHandler.click(true);
         }

         if (!leftDown && this.mouseLeftDown) {
            this.mouseLeftDown = false;
         }

         if (!rightDown && this.mouseRightDown) {
            this.mouseRightDown = false;
         }

         int i = Mouse.getEventDWheel();
         if (i != 0) {
            boolean slow = true;
            if (KeyBinding.isReversed()) {
               slow = false;
            }

            MenuHandler.scroll(i > 0, slow);
         }

      }

      // $FF: synthetic method
      MouseState(Object x0) {
         this();
      }
   }
}
