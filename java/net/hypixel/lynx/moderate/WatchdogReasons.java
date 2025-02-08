package net.hypixel.lynx.moderate;

import net.hypixel.lynx.chat.tab.InputWindow;
import net.minecraft.client.Minecraft;

public enum WatchdogReasons implements Action, Pageable {
   Fly("Flight"),
   Killaura("KillAura"),
   Autoclicker("Auto Clicker"),
   Speed("Speed / Bhop"),
   Antikockback("Anti Knockback"),
   Reach("Reach"),
   Dolphin("Dolphin / Waterwalk / Jesus");

   private final String name;

   private WatchdogReasons(String name) {
      this.name = name;
   }

   public String properName() {
      return this.name;
   }

   public void onAction(LynxUI ui, String username) {
      String def;
      Minecraft.getMinecraft().displayGuiScreen((new InputWindow(def = String.format("/wdr %s %s", username, this.name()), def.length() + 1, true)).onSubmit((s) -> {
         if (s.toLowerCase().startsWith("/wdr")) {
            ui.root();
         }

      }));
   }
}
