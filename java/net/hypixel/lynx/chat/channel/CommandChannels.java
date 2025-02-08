package net.hypixel.lynx.chat.channel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.hypixel.lynx.config.ClientConfig;
import net.hypixel.lynx.util.Symbols;

public enum CommandChannels implements CommandChannel {
   USERINFO("/userinfo -f %s", new LinkedHashMap<String, String>() {
      {
         String opts = "(\u00A7r)?(\u00A7f)?";
         String prefix = opts + "\u00A7" + "6";
         String data = "(?<data>.*?)";
         String form = prefix + "%s: (" + "\u00A7" + "f)?" + data + opts;
         this.put("display-name", prefix + "--- Info about " + data + prefix + " ---" + opts);
         this.put("recent-name", String.format(form, "Most Recent Name"));
         this.put("uuid", String.format(form, "UUID"));
         this.put("rank", String.format(form, "Rank"));
         this.put("package-rank", String.format(form, "PackageRank"));
         this.put("old-package-rank", String.format(form, "OldPackageRank"));
         this.put("network-level", String.format(form, "Network Level"));
         this.put("network-exp", String.format(form, "Network EXP"));
         this.put("guild", String.format(form, "Guild"));
         this.put("current-server", String.format(form, "Current Server"));
         this.put("first-login", String.format(form, "First Login"));
         this.put("last-login", String.format(form, "Last Login"));
         this.put("packages", String.format(form, "Packages"));
         this.put("boosters", String.format(form, "Boosters"));
         this.put("punishments", String.format(form, "Punishments"));
         this.put("actions", prefix + "Actions: .*");
      }
   }, (output) -> {
      String val = output.remove("punishments");
      val = Symbols.stripChatColor(val);
      Arrays.stream(val.split("\\-")).map(String::trim).map((s) -> {
         return s.split("\\s");
      }).forEach((arr) -> {
         output.note(arr[0].toLowerCase(), arr[1]);
      });
   }),
   /*
   VIEW_BANS("/bans %s", new LinkedHashMap<String, String>() {
      {
         String date = "\\d{4}\\-\\d{2}\\-\\d{2} \\d{2}:\\d{2} [A-Za-z]{1,5}";
         this.put("header", "(?<ignore>--- Bans for [A-Za-z0-9_]{3,16} \\(Page \\d+ of \\d+\\) ---)");
         this.put("infraction", "(?<repeat>\\d+\\. (?<key>(Kick|Ban|Tempban)) \\- " + date + " (?<data>((?!\\[WATCHDOG).*?|\\[WATCHDOG CHEAT DETECTION\\].*))(( by [A-Za-z0-9_]{3,16})( for ((?<ignore>0D:0H:0M)|\\d+D:\\d+H:\\d+M)(( unbanned " + date + " (?<ignore2>(?!Tempban expired)[A-Za-z0-9\\-_\\s]+)?))?))?)");
      }
   }, (output) -> {
      List<String> bans = output.getList("Ban");
      List<String> temps = output.getList("Tempban");
      Stream.of(bans, temps).forEach((l) -> {
         l.replaceAll((r) -> {
            return r.contains("WATCHDOG") ? "\u00A7c[WATCHDOG]" : r;
         });
         l.replaceAll((r) -> {
            return r.split(" by [A-Za-z0-9_]{3,16}( unbanned)?")[0];
         });
      });
   }),
    */
   PRIVATE_MESSAGES((String)null, new LinkedHashMap<String, String>() {
      {
         String anyColor = "[\u00A7crd]+";
         this.put("offline", "(?<data>" + anyColor + "That player isn't online!)|(?<cancel>" + anyColor + "d(From|To).*>)");
      }
   }),
   TARGETED((String)null, new LinkedHashMap<String, String>() {
      {
         this.put("target", "Teleporting you to (?<data>[A-Za-z0-9_]{3,16})");
      }
   }),
   /*
   VIEW_BANS_2("/bans -a %s", new LinkedHashMap<String, String>() {
      {
         String delimiter = "Ω";
         this.put("data", "\\[Lynx\\] (?<repeat>(?<data>.*?" + delimiter + "(true|false)" + delimiter + "(true|false)" + delimiter + "(null|\\d+)" + delimiter + "[a-zA-Z0-9\\-]{32,36}" + delimiter + "\\d+))");
      }
   }),
    */
   STAFF_IN_INSTANCE("/staff", new LinkedHashMap<String, String>() {
   });

   private static final CommandChannels[] VALUES = values();
   private final String command;
   private final Map<String, Pattern> rules;
   private final Map<String, CommandChannel.ChannelMeta> meta;
   private final Consumer<CommandChannel.Output> postProcess;

   private CommandChannels(String command, Map<String, String> rules) {
      this(command, rules, (Consumer)null);
   }

   private CommandChannels(String command, Map<String, String> rules, Consumer<CommandChannel.Output> postProcess) {
      this.rules = new LinkedHashMap();
      this.meta = new HashMap();
      this.command = command;
      rules.entrySet().forEach((ent) -> {
         this.rules.put(ent.getKey(), Pattern.compile((String)ent.getValue()));
      });
      this.postProcess = postProcess;
   }

   public static CommandChannel of(String name) {
      return (CommandChannel)ClientConfig.CHAT_CHANNELS.as(Map.class, String.class, ConfigurableCommandChannel.class).get(name);
   }

   public static boolean tryInput(String message) {
      AtomicBoolean intercept = new AtomicBoolean(false);
      Map<String, ConfigurableCommandChannel> chan = ClientConfig.CHAT_CHANNELS.as(Map.class, String.class, ConfigurableCommandChannel.class);
      CommandChannel[] vals = (CommandChannel[])chan.values().toArray(new CommandChannel[chan.size()]);
      String[] split = message.split("\n");
      Stream.of(VALUES, vals).flatMap(Arrays::stream).forEach((cc) -> {
         Stream test;
         if (cc.testPerNewLine()) {
            test = Arrays.stream(split);
         } else {
            test = Stream.of(message);
         }

         test.forEach((s) -> {
            if (cc.input((String) s)) {
               intercept.set(true);
            }

         });
      });
      return intercept.get();
   }

   public Consumer<CommandChannel.Output> getPostProcess() {
      return this.postProcess;
   }

   public Map<String, CommandChannel.ChannelMeta> getMeta() {
      return this.meta;
   }

   public Map<String, Pattern> getRules() {
      return this.rules;
   }

   public String getCommand() {
      return this.command;
   }

   public boolean canExpire() {
      return false;
   }

   public boolean ignoreColor() {
      return this == TARGETED;
   }

   public boolean testPerNewLine() {
      return true;
   }
}
