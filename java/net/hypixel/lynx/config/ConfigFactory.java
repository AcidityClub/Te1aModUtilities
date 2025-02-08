package net.hypixel.lynx.config;

import java.util.Set;
import net.hypixel.lynx.ui.key.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.IModGuiFactory.RuntimeOptionCategoryElement;
import net.minecraftforge.fml.client.IModGuiFactory.RuntimeOptionGuiHandler;

public class ConfigFactory implements IModGuiFactory {
   public static final ConfigCategory PRIMARY_CATEGORY = new ConfigCategory("Config");
   public static final ConfigCategory GLOBAL;

   public void initialize(Minecraft minecraftInstance) {
   }

   public Class<? extends GuiScreen> mainConfigGuiClass() {
      return ConfigMenu.class;
   }

   public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
      return null;
   }

   public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
      return null;
   }

   static {
      GLOBAL = (new ForgeConfig.ForgeMenu("Global", ClientConfig.class, new ClientConfig[]{ClientConfig.PING_NOTIFICATIONS})).getParent();
      new ForgeConfig.ForgeMenu("Keys", KeyBindings.class, new KeyBindings[0]);
   }
}
