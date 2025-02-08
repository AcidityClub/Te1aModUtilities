package net.hypixel.lynx.ui.ui2;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public interface AbsoluteContentHolder<E extends AbsoluteContentElement> extends ContentHolder<E>, AbsoluteContentElement {
   default int getWidth() {
      Stream<Integer> vals = this.viewElements().map(AbsoluteContentElement::getWidth);
      return this.vertical() ? (Integer)vals.max(Integer::compare).orElse(0) : (Integer)vals.reduce(0, Integer::sum);
   }

   default int getHeight() {
      Stream<Integer> vals = this.viewElements().map(AbsoluteContentElement::getHeight);
      return this.vertical() ? (Integer)vals.reduce(0, Integer::sum) : (Integer)vals.max(Integer::compare).orElse(0);
   }

   default void onDraw(int x, int y) {
      AtomicInteger aM = new AtomicInteger(this.vertical() ? y : x);
      this.viewElements().forEach((e) -> {
         e.draw(this.vertical() ? x : x + aM.getAndAdd(e.getWidth()), this.vertical() ? y + aM.getAndAdd(e.getHeight()) : y);
      });
   }

   default void onClick(int x, int y) {
      int aM = this.vertical() ? y : x;
      AtomicInteger bX = new AtomicInteger();
      AtomicInteger bY = new AtomicInteger();
      this.viewElements().forEach((e) -> {
         int mX = this.vertical() ? x : bX.getAndAdd(aM);
         int mY = this.vertical() ? bY.getAndAdd(aM) : y;
         if (e.inBounds(mX, mY)) {
            e.onClick(mX, mY);
         }

      });
   }
}
