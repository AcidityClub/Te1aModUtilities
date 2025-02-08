package net.hypixel.lynx.moderate;

public interface LengthsEnum extends Action {
   default void onAction(LynxUI ui, String username) {
      if (ui.getPreviousSelection() instanceof Expirable) {
         ((Expirable)ui.getPreviousSelection()).onAction(ui, username, this);
      }

   }

   default String properName() {
      return this.getLength() == null ? Action.super.properName() : this.getLength();
   }

   String getLength();

   boolean isCustomEntry();
}
