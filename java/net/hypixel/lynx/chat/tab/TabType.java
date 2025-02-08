package net.hypixel.lynx.chat.tab;

import com.codelanx.commons.util.Reflections;
import java.util.Arrays;
import java.util.function.Supplier;
import net.hypixel.lynx.chat.Tab;

public enum TabType {
   GENERAL(Window::new),
   PRIVATE_MESSAGES(PMWindow::new);

   private final Supplier<? extends Window> window;

   private TabType(Supplier<? extends Window> window) {
      this.window = window;
   }

   public static String[] names() {
      return (String[])Arrays.stream(values()).map(Reflections::properEnumName).toArray((x$0) -> {
         return new String[x$0];
      });
   }

   public Window getWindow() {
      return (Window)this.window.get();
   }

   public Tab newTab() {
      return new Tab(this);
   }

   public Tab newTab(String name) {
      return new Tab(name, this);
   }
}
