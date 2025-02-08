package net.hypixel.lynx.chat.channel;

import com.codelanx.commons.data.FileSerializable;
import com.codelanx.commons.util.Scheduler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.util.Symbols;

public interface CommandChannel extends FileSerializable {
   Map<String, CommandChannel.ChannelMeta> getMeta();

   Map<String, Pattern> getRules();

   String getCommand();

   default boolean input(String input) {
      Iterator<Entry<String, CommandChannel.ChannelMeta>> itr = this.getMeta().entrySet().iterator();
      if (this.ignoreColor()) {
         input = Symbols.stripChatColor(input);
      }

      boolean match = false;

      while(true) {
         while(itr.hasNext()) {
            Entry<String, CommandChannel.ChannelMeta> ent = (Entry)itr.next();
            if (((CommandChannel.ChannelMeta)ent.getValue()).isExpired()) {
               ((CommandChannel.ChannelMeta)ent.getValue()).complete(this.getPostProcess());
               itr.remove();
            } else {
               Pattern p = this.getNext((String)ent.getKey());
               Matcher m = p.matcher(input);
               if (m.matches()) {
                  String key = null;
                  String data = null;
                  boolean repeat = false;

                  try {
                     data = m.group("data");
                  } catch (IllegalArgumentException var13) {
                  }

                  try {
                     if (m.group("cancel") != null) {
                        itr.remove();
                        continue;
                     }
                  } catch (IllegalArgumentException var14) {
                  }

                  try {
                     if (m.group("repeat") != null) {
                        repeat = true;
                     }
                  } catch (IllegalArgumentException var12) {
                  }

                  try {
                     if (m.group("ignore") != null || m.group("ignore2") != null) {
                        match = true;
                        if (!repeat) {
                           ((CommandChannel.ChannelMeta)ent.getValue()).incrementPosition();
                        }
                        continue;
                     }
                  } catch (IllegalArgumentException var15) {
                  }

                  try {
                     String gKey = m.group("key");
                     if (gKey != null) {
                        key = gKey;
                     }
                  } catch (IllegalArgumentException var11) {
                  }

                  if (this.match((CommandChannel.ChannelMeta)ent.getValue(), (String)ent.getKey(), key, data, repeat)) {
                     itr.remove();
                  }

                  match = true;
               }
            }
         }

         return match;
      }
   }

   default boolean testPerNewLine() {
      return true;
   }

   default void execute(String username, Consumer<CommandChannel.Output> onComplete) {
      this.getMeta().put(username, new CommandChannel.ChannelMeta(this, username, onComplete));
      if (this.getCommand() != null) {
         Lynx.getChat().sendMessage(String.format(this.getCommand(), username), false);
      }

   }

   default boolean match(CommandChannel.ChannelMeta meta, String username, String key, String input, boolean repeat) {
      meta.note(key == null ? this.getCurrentRule(username) : key, input == null ? null : input.trim(), repeat);
      if (meta.getPosition() >= this.getRules().size()) {
         meta.complete(this.getPostProcess());
         return true;
      } else {
         return false;
      }
   }

   default Pattern getNext(String username) {
      int pos = ((CommandChannel.ChannelMeta)this.getMeta().get(username)).getPosition();
      Iterator<Entry<String, Pattern>> itr = this.getRules().entrySet().iterator();

      Pattern back;
      for(back = null; pos >= 0; --pos) {
         back = (Pattern)((Entry)itr.next()).getValue();
      }

      return back;
   }

   default String getCurrentRule(String username) {
      int pos = ((CommandChannel.ChannelMeta)this.getMeta().get(username)).getPosition();
      Iterator<Entry<String, Pattern>> itr = this.getRules().entrySet().iterator();

      String back;
      for(back = null; pos >= 0; --pos) {
         back = (String)((Entry)itr.next()).getKey();
      }

      return back;
   }

   Consumer<CommandChannel.Output> getPostProcess();

   default boolean canExpire() {
      return false;
   }

   default boolean ignoreColor() {
      return false;
   }

   default Map<String, Object> serialize() {
      Map<String, Object> back = new HashMap();
      back.put("command", this.getCommand());
      back.put("rules", this.getRules().entrySet().stream().collect(Collectors.toMap(Entry::getKey, (ent) -> {
         return ((Pattern)ent.getValue()).pattern();
      })));
      return back;
   }

   public static class Output {
      private final Map<String, List<String>> output = new HashMap();

      void note(String key, String value) {
         if (value != null) {
            this.nabActual(key).add(value);
         }
      }

      public String get(String key) {
         return (String)this.nab(key).stream().findFirst().orElse(null);
      }

      public List<String> getList(String key) {
         return this.nab(key);
      }

      public boolean hasMultipleValues(String key) {
         return this.nab(key).size() > 1;
      }

      private List<String> nab(String key) {
         return (List)this.output.getOrDefault(key, Collections.emptyList());
      }

      private List<String> nabActual(String key) {
         return (List)this.output.computeIfAbsent(key, (k) -> {
            return new ArrayList();
         });
      }

      public String remove(String key) {
         return (String)this.removeList(key).stream().findFirst().orElse(null);
      }

      public List<String> removeList(String key) {
         return (List)this.output.remove(key);
      }

      public Map<String, String> getOneToOneMap() {
         return (Map)this.output.entrySet().stream().filter((ent) -> {
            return ((List)ent.getValue()).size() > 0;
         }).collect(Collectors.toMap(Entry::getKey, (ent) -> {
            return (String)((List)ent.getValue()).stream().findFirst().orElse(null);
         }));
      }

      public Map<String, List<String>> getOneToManyMap() {
         return Collections.unmodifiableMap(this.output);
      }
   }

   public static class ChannelMeta {
      private static final int EXPIRATION_THRESHOLD_MS = 500;
      private final CommandChannel.Output output = new CommandChannel.Output();
      private final Consumer<CommandChannel.Output> onComplete;
      private final AtomicBoolean completed = new AtomicBoolean();
      private final CommandChannel chan;
      private final String username;
      private int position = 0;
      private ScheduledFuture<?> canceller = null;
      private long lastNoted = Long.MAX_VALUE;

      public ChannelMeta(CommandChannel chan, String username, Consumer<CommandChannel.Output> onComplete) {
         this.chan = chan;
         this.username = username;
         this.onComplete = onComplete;
      }

      public boolean isExpired() {
         return this.chan.canExpire() && this.lastNoted < System.currentTimeMillis() - 500L;
      }

      public void note(String rule, String value, boolean repeat) {
         this.output.note(rule, value);
         this.lastNoted = System.currentTimeMillis();
         if (this.chan.canExpire()) {
            if (this.canceller != null) {
               this.canceller.cancel(true);
            }

            this.canceller = Scheduler.getService().schedule(() -> {
               this.complete(this.chan.getPostProcess());
               this.chan.getMeta().remove(this.username);
            }, 500L, TimeUnit.MILLISECONDS);
         }

         if (!repeat) {
            this.incrementPosition();
         }

      }

      public void incrementPosition() {
         ++this.position;
      }

      public void complete(Consumer<CommandChannel.Output> postProcess) {
         if (!this.completed.getAndSet(true)) {
            try {
               if (postProcess != null) {
                  postProcess.accept(this.output);
               }

               this.onComplete.accept(this.output);
            } catch (Exception var3) {
               var3.printStackTrace(System.out);
            }
         }

      }

      public int getPosition() {
         return this.position;
      }
   }

   public interface Key {
      String KEY = "key";
      String DATA = "data";
      String IGNORE = "ignore";
      String IGNORE2 = "ignore2";
      String REPEAT = "repeat";
      String CANCEL = "cancel";
   }
}
