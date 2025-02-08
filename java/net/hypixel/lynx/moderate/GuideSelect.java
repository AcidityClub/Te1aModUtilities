package net.hypixel.lynx.moderate;

import com.codelanx.commons.logging.Debugger;
import com.codelanx.commons.util.RNG;
import com.codelanx.commons.util.Scheduler;
import com.codelanx.commons.util.ref.Box;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.hypixel.lynx.chat.ChatFacade;
import net.hypixel.lynx.config.ClientConfig;
import net.hypixel.lynx.util.Util;

public enum GuideSelect implements Action, Pageable {
   CHAT,
   PM;

   private static GuideSelect current = PM;
   private static Action[] macros = getCurrentMacros();

   private static Action[] getCurrentMacros() {
      Map<String, Object> o = ClientConfig.MACROS.as(Map.class, String.class, Object.class);
      List<Action> back = new LinkedList();
      Iterator var2 = o.entrySet().iterator();

      while(true) {
         Entry ent;
         List options;
         while(true) {
            if (!var2.hasNext()) {
               return (Action[])back.toArray(new Action[back.size()]);
            }

            ent = (Entry)var2.next();
            if (ent.getValue() instanceof String) {
               options = Collections.singletonList((String)ent.getValue());
               break;
            }

            if (ent.getValue() instanceof List) {
               options = (List)ent.getValue();
               break;
            }

            Debugger.error(new ClassCastException(), "Error reading macro value: '%s'", ent.getKey());
         }

         List<String[]> opts = (List)options.stream().map((s) -> {
            return ((String)s).split("\\. ");
         }).collect(Collectors.toList());
         back.add(actionFor((String)ent.getKey(), opts));
      }
   }

   private static Action actionFor(final String key, final List<String[]> options) {
      return new Action() {
         public void onAction(LynxUI ui, String username) {
            GuideSelect.sendTo(username, key, (String[])RNG.get(options), GuideSelect.current == GuideSelect.PM);
            ui.getPrevSelections().clear();
            ui.setOptionList(Option.class);
         }

         public String properName() {
            return key;
         }
      };
   }

   private static void sendTo(String username, String key, String[] out, boolean pm) {
      String prefix = pm ? "/msg " + username + " " : "";
      if (out.length <= 0) {
         Util.out("Error: received empty macro for '%s'", key);
      }

      AtomicInteger index = new AtomicInteger();
      Box<Runnable> op = new Box();
      op.value = () -> {
         String s = out[index.get()];
         System.out.printf("Format: %s\n", s);
         if (!s.isEmpty()) {
            if (pm) {
               s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
            }

            ChatFacade.get().sendMessage(prefix + s, false);
            index.set(index.get() + 1);
            if (index.get() < out.length) {
               Scheduler.getService().schedule((Runnable)op.value, (long)(RNG.THREAD_LOCAL.current().nextInt(750) + 1250), TimeUnit.MILLISECONDS);
            }
         }
      };
      ((Runnable)op.value).run();
   }

   public void onAction(LynxUI ui, String username) {
      ui.addPrevious(this);
      current = this;
      ui.setOptionList(() -> {
         return macros;
      });
   }
}
