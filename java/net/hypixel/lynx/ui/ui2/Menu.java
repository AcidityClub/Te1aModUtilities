package net.hypixel.lynx.ui.ui2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public abstract class Menu<E extends AbsoluteContentElement> implements AbsoluteContentHolder<E> {
   private final String title;
   private final List<E> elements = new ArrayList();

   public Menu(String title) {
      this.title = title;
   }

   public void add(E... element) {
      if (element != null && element.length != 0) {
         this.elements.addAll(Arrays.asList(element));
      }
   }

   public void remove(E element) {
      if (element != null) {
         this.elements.remove(element);
      }
   }

   public boolean isCentered() {
      return true;
   }

   public Stream<E> viewElements() {
      return this.elements.stream();
   }

   public void onDraw(int x, int y) {
      this.drawTitleBar();
      AtomicInteger aX = new AtomicInteger();
      AtomicInteger aY = new AtomicInteger();
      this.elements.forEach((e) -> {
         e.draw(x + aX.get(), y + aY.get());
         if (this.vertical()) {
            aY.set(aY.get() + e.getHeight());
         } else {
            aX.set(aX.get() + e.getWidth());
         }

      });
   }

   private void drawTitleBar() {
      if (this.title != null) {
         ;
      }
   }
}
