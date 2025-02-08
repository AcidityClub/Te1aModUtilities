package net.hypixel.lynx.config;

public interface ConfigUI {
   String getTitle();

   String getSubtitle();

   void onDoneButton();

   void onResetButton();

   ConfigUI getParent();
}
