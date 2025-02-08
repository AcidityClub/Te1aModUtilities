package net.hypixel.lynx.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class TextScreen extends GuiScreen {
   private final GuiTextField inputField;
   private final List<Runnable> onLooseFocus;
   private final List<BiPredicate<Integer, Character>> constraints;
   private final Region region;
   private String text;

   public TextScreen(Region region) {
      this(region, (List)null);
   }

   public TextScreen(Region region, List<BiPredicate<Integer, Character>> constraints) {
      this.onLooseFocus = new LinkedList();
      this.constraints = new ArrayList();
      this.constraints.addAll(constraints);
      this.inputField = new GuiTextField(69, Minecraft.getMinecraft().fontRendererObj, region.getAbsoluteLeft(), region.getAbsoluteTop() + 1, region.getRight() - region.getLeft(), region.getBottom() - region.getTop());
      this.region = region;
      this.text = region.getText();
   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      switch(keyCode) {
      case 14:
      case 211:
         this.inputField.textboxKeyTyped(typedChar, keyCode);
         break;
      case 28:
      case 156:
         this.text = this.inputField.getText();
         this.region.setText(this.text);
      case 1:
         Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
         this.onLooseFocus.forEach(Runnable::run);
         break;
      default:
         if (this.constraints.stream().allMatch((p) -> {
            return p.test(keyCode, typedChar);
         })) {
            this.inputField.textboxKeyTyped(typedChar, keyCode);
         }
      }

   }

   public void initGui() {
      Keyboard.enableRepeatEvents(true);
      this.inputField.setEnableBackgroundDrawing(false);
      this.inputField.setFocused(true);
      this.inputField.setText(this.text == null ? "" : this.text);
      this.inputField.setCursorPosition(this.text == null ? 0 : this.text.length());
      this.inputField.setCanLoseFocus(false);
   }

   public void onLooseFocus(Runnable run) {
      this.onLooseFocus.add(run);
   }

   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
      this.region.setText(this.text);
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.inputField.drawTextBox();
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   public void updateScreen() {
      this.inputField.updateCursorCounter();
   }

   public boolean doesGuiPauseGame() {
      return false;
   }
}
