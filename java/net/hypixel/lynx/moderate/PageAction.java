package net.hypixel.lynx.moderate;

import net.hypixel.lynx.ui.key.KeyBinding;
import net.hypixel.lynx.ui.key.KeyBindings;

public enum PageAction implements Action {
   PREVIOUS_PAGE(KeyBindings.PREVIOUS_PAGE, (ui, user) -> {
      ui.page(-1);
   }),
   NEXT_PAGE(KeyBindings.NEXT_PAGE, (ui, user) -> {
      ui.page(1);
   }),
   CANCEL(KeyBindings.ACTION_CANCEL, (ui, user) -> {
      ui.back();
   });

   private final KeyBinding key;
   private final Action action;

   private PageAction(KeyBinding key, Action action) {
      this.key = key;
      this.action = action;
   }

   public KeyBinding getKeyBinding() {
      return this.key;
   }

   public void onAction(LynxUI ui, String username) {
      this.action.onAction(ui, username);
   }
}
