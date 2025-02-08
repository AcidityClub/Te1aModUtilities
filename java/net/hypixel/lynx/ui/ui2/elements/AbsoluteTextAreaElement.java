package net.hypixel.lynx.ui.ui2.elements;

import net.hypixel.lynx.ui.ui2.AbsoluteContentElement;

public class AbsoluteTextAreaElement implements AbsoluteContentElement {
   private final StringBuilder value = new StringBuilder();

   public AbsoluteTextAreaElement() {
   }

   public AbsoluteTextAreaElement(String value) {
      this.value.append(value);
   }

   public AbsoluteTextAreaElement(int width, int height) {
   }

   public int getWidth() {
      return 0;
   }

   public int getHeight() {
      return 0;
   }

   public void onDraw(int x, int y) {
   }

   private static class TextAreaSplitter {
   }
}
