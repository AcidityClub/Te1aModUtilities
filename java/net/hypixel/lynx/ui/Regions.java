package net.hypixel.lynx.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.util.Util;
import net.minecraft.client.Minecraft;

public class Regions {
   public static final int TEXT_HORIZ_BUFFER = 3;
   public static final int CHECKBOX_WIDTH;

   private Regions() {
   }

   public static Region checkbox(String label, Supplier<Boolean> active, int left, int top) {
      return Region.builder().left(left).right(left + CHECKBOX_WIDTH).top(top).bottom(top + 9).build().addChild(Region.builder().color(153, 153, 153, 127).text(() -> {
         return (Boolean)active.get() ? "✓" : null;
      }).left(0).right(CHECKBOX_WIDTH).isElement().top(0).bottom(9).build()).addChild(Region.builder().left(CHECKBOX_WIDTH + 3).right(CHECKBOX_WIDTH + 30).top(0).bottom(9).text(label).build());
   }

   public static Region dropDown(int left, int top, int textWidth, Supplier<String> active, Consumer<String> onSelect, String... options) {
      Region dropper = Region.builder().color(102, 102, 102, 127).text(() -> {
         return trimToWidth((String)active.get(), textWidth);
      }).left(left).right(left + textWidth + CHECKBOX_WIDTH).isElement().top(top).bottom(top + 9).build();
      dropper.setInheritBoundsFromChildren(false);
      dropper.onClick((region, x, cy, right) -> {
         region.setChildrenVisible(!region.isChildrenVisible());
      });
      dropper.setChildrenVisible(false);
      int y = dropper.getHeight();
      String[] var8 = options;
      int var9 = options.length;

      for(int var10 = 0; var10 < var9; ++var10) {
         String s = var8[var10];
         dropper.addChild(Region.builder().color(102, 102, 102, 127).text(trimToWidth(s, textWidth)).left(0).top(y).right(textWidth).bottom(y + 9).build().onClick((region, x, cy, right) -> {
            onSelect.accept(s);
            dropper.setChildrenVisible(false);
         }));
         y += 9;
      }

      return dropper;
   }

   public static Region textField(int left, int top, String currentValue, Consumer<String> valueSetter) {
      return textField(left, top, 120, currentValue, valueSetter);
   }

   public static TextRegion textField(int left, int top, int width, String currentValue, Consumer<String> valueSetter) {
      Util.out("Textfield top: %d", top);
      return ((TextRegion)Region.builder().text(currentValue).left(left).top(top).right(left + width).bottom(top + 11).build(TextRegion::new)).onSetValue(valueSetter);
   }

   public static Region button(int left, int top, String text, int color, Consumer<Boolean> onClick) {
      int width = getStringWidth(text) + 6;
      return Region.builder().left(left).top(top).bottom(top + 15).right(left + width).color(color).build().onClick((region, x, y, right) -> {
         onClick.accept(right);
      }).addChild(Region.builder().top(3).bottom(12).text(text).build());
   }

   public static Menu textListMenu(String title) {
      Menu back = new Menu() {
         {
            this.addCloseButton();
            this.backgroundColor(Regions.colorFromRGB(0, 0, 0, 127));
         }

         protected void render(int ticks) {
         }

         protected void keyTyped() {
         }
      };
      return back;
   }

   public static int colorFromRGB(Number red, Number green, Number blue, Number alpha) {
      return red.intValue() << 16 | green.intValue() << 8 | blue.intValue() | alpha.intValue() << 24;
   }

   public static void printRGBColor(int color) {
      System.out.println("RGB{red: " + (color >> 16 & 255) + ", green: " + (color >> 8 & 255) + ", blue: " + (color & 255) + ", alpha: " + (color >> 24 & 255) + "}");
   }

   public static String trimToWidth(String in, int width) {
      return Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(in, width);
   }

   public static int getStringWidth(String in) {
      return Minecraft.getMinecraft().fontRendererObj.getStringWidth(in);
   }

   static {
      CHECKBOX_WIDTH = Lynx.DEBUGGING ? 11 : getStringWidth("✓") + 2;
   }
}
