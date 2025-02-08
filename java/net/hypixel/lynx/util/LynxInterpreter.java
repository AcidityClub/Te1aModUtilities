package net.hypixel.lynx.util;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;

public class LynxInterpreter {
   private LynxInterpreter.State currentState = null;
   private Map<Runnable, LynxInterpreter.State> stateQueue = new TreeMap();
   private String displayName = null;

   public void interpret(String input) {
      if (this.currentState != null && this.currentState.interpret(this, input)) {
         this.next();
      }

   }

   public String getDisplayName() {
      if (this.displayName == null) {
         LynxInterpreter.State st = LynxInterpreter.State.DISPLAY_NAME;
         return "\u00A7d" + Minecraft.getMinecraft().getSession().getUsername();
      } else {
         return this.displayName;
      }
   }

   private void enqueue(Runnable queue, LynxInterpreter.State state) {
      if (this.stateQueue.isEmpty()) {
         this.currentState = state;
         queue.run();
      } else {
         this.stateQueue.put(queue, state);
      }

   }

   private void next() {
      if (!this.stateQueue.isEmpty()) {
         Entry<Runnable, LynxInterpreter.State> ent = (Entry)this.stateQueue.entrySet().iterator().next();
         this.currentState = (LynxInterpreter.State)ent.getValue();
         ((Runnable)ent.getKey()).run();
      } else {
         this.currentState = null;
      }

   }

   private static enum State {
      DISPLAY_NAME("/prowler displayname") {
         public boolean interpret(LynxInterpreter interpeter, String input) {
            interpeter.displayName = input;
            return true;
         }
      };

      private final String command;

      private State(String command) {
         this.command = command;
      }

      public abstract boolean interpret(LynxInterpreter var1, String var2);

      public String getCommand() {
         return this.command;
      }

      // $FF: synthetic method
      State(String x2, Object x3) {
         this(x2);
      }
   }
}
