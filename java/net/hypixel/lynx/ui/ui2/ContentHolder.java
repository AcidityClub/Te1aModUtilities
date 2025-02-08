package net.hypixel.lynx.ui.ui2;

import java.util.stream.Stream;

public interface ContentHolder<E extends ContentElement> extends ContentElement {
   void add(E... var1);

   void remove(E var1);

   Stream<E> viewElements();

   default boolean vertical() {
      return false;
   }
}
