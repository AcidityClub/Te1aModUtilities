package net.hypixel.lynx.moderate;

import com.codelanx.commons.util.Reflections;
import net.hypixel.lynx.ui.key.KeyBinding;

@FunctionalInterface
public interface Action extends Pageable {
   void onAction(LynxUI var1, String var2);

   default KeyBinding getKeyBinding() {
      return KeyBinding.empty();
   }

   default String properName() {
      return this.getClass().isEnum() ? Reflections.properEnumName((Enum)this) : null;
   }

   default String getKeyShortcut() {
      return this.getKeyBinding().getName();
   }

   default boolean isAdminOption() {
      return false;
   }
}
