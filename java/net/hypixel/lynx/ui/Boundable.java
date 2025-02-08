package net.hypixel.lynx.ui;

public interface Boundable {
   int getTop();

   int getBottom();

   int getRight();

   int getLeft();

   default int getHeight() {
      return this.getBottom() - this.getTop();
   }
}
