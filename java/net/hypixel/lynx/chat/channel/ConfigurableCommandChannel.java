package net.hypixel.lynx.chat.channel;

import com.codelanx.commons.data.FileSerializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigurableCommandChannel implements CommandChannel, FileSerializable {
   private final String command;
   private final Map<String, Pattern> rules = new LinkedHashMap<>();
   private final Map<String, CommandChannel.ChannelMeta> meta = new HashMap<>();

   @SuppressWarnings("unchecked")
   public ConfigurableCommandChannel(Map<String, Object> config) {
      this.command = (String) config.get("command");

      if (config.containsKey("rules") && config.get("rules") instanceof Map<?, ?>) {
         Map<String, String> rulesMap = (Map<String, String>) config.get("rules");
         this.rules.putAll(rulesMap.entrySet().stream()
                 .collect(Collectors.toMap(Map.Entry::getKey, entry -> Pattern.compile(entry.getValue()))));
      }
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

   public Consumer<CommandChannel.Output> getPostProcess() {
      return null;
   }
}
