package net.hypixel.lynx.ui.ui2;

import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.ui.Regions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

public interface AbsoluteContentElement extends ContentElement {
   int TEXT_HEIGHT = 11;

   default int getOffsetX() {
      if (this.isCentered()) {
         int width = Lynx.DEBUGGING ? 1920 : Minecraft.getMinecraft().displayWidth;
         return width / 2 - this.getWidth() / 2;
      } else {
         return 0;
      }
   }

   default int getOffsetY() {
      if (this.isCentered()) {
         int height = Lynx.DEBUGGING ? 1080 : Minecraft.getMinecraft().displayHeight;
         return height / 2 - this.getHeight() / 2;
      } else {
         return 0;
      }
   }

   default boolean isCentered() {
      return false;
   }

   int getWidth();

   int getHeight();

   default int getBackroundRGB() {
      return Regions.colorFromRGB(0, 0, 0, 51);
   }

   default void draw() {
      this.onDraw(this.getOffsetX(), this.getOffsetY());
   }

   default void draw(int x, int y) {
      int width = this.getWidth();
      int height = this.getHeight();
      Gui.drawRect(x, y, x + width, y + height, this.getBackroundRGB());
      this.onDraw(x, y);
   }

   default void hover() {
      int x = Mouse.getX();
      int y = Mouse.getY();
      if (this.inBounds(x, y)) {
         this.onHover(x, y);
      }

   }

   default void click() {
      int x = Mouse.getX();
      int y = Mouse.getY();
      x = this.isCentered() ? x - this.getOffsetX() : x;
      y = this.isCentered() ? y - this.getOffsetY() : y;
      this.onClick(x, y);
   }

   void onDraw(int var1, int var2);

   default void onHover(int x, int y) {
   }

   default void onClick(int x, int y) {
   }

   default boolean inBounds(int x, int y) {
      return true;
   }
}
