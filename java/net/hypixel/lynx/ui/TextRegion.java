package net.hypixel.lynx.ui;

import com.codelanx.commons.util.Scheduler;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import net.hypixel.lynx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class TextRegion extends Region {
   private final List<Consumer<String>> onSetValue = new LinkedList();
   private final List<BiPredicate<Integer, Character>> constraints = new LinkedList();
   private GuiScreen back;

   public TextRegion(Region.Builder build) {
      super(build);
      this.onClick((region, x, y, right) -> {
         this.back = Minecraft.getMinecraft().currentScreen;
         TextScreen scr = new TextScreen(this, this.constraints);
         this.setText((String)null);
         scr.onLooseFocus(() -> {
            this.onSetValue.forEach((c) -> {
               c.accept(this.getText());
            });
            Scheduler.runAsyncTask(() -> {
               Minecraft.getMinecraft().displayGuiScreen(this.back);
            });
         });
         Util.out("Displaying text screen");
         Minecraft.getMinecraft().displayGuiScreen(scr);
      });
   }

   public TextRegion onSetValue(Consumer<String> onSet) {
      this.onSetValue.add(onSet);
      return this;
   }

   public TextRegion addConstraint(BiPredicate<Integer, Character> typeFilter) {
      this.constraints.add(typeFilter);
      return this;
   }
}
