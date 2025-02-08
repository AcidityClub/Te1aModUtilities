package net.hypixel.lynx.moderate;

import net.hypixel.lynx.chat.tab.InputWindow;
import net.minecraft.client.Minecraft;

enum Reporting implements Action {
   HACKING("/wdr %s "),
   CHAT_REPORT("/chatreport %s"),
   OTHER("/s Report of %s for ");

   private String template;

   private Reporting(String template) {
      this.template = template;
   }

   public void onAction(LynxUI ui, String username) {
      if (this == HACKING) {
         ui.addPrevious(this);
         ui.setOptionList(WatchdogReasons.class);
      } else {
         String finalString;
         Minecraft.getMinecraft().displayGuiScreen((new InputWindow(finalString = String.format(this.template, username), finalString.length() + 1, this != OTHER)).onSubmit((s) -> {
            if (s.toLowerCase().startsWith("/s Report of") || s.toLowerCase().startsWith("/chatreport")) {
               ui.root();
            }

         }));
      }
   }
}
