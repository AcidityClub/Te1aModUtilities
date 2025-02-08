package net.hypixel.lynx.chat.tab;

import java.io.IOException;
import java.util.function.Consumer;

import com.codelanx.commons.util.RNG;
import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.ChatFacade;
import net.hypixel.lynx.chat.Tab;
import net.hypixel.lynx.chat.channel.CommandChannels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class InputWindow extends GuiChat {
   private final String ourOwnDefault;
   private final int defaultCursorPosition;
   private Consumer<String> onSubmit;
   private boolean locked;

   public InputWindow(String def) {
      this(def, def.length());
   }

   public InputWindow(String def, int cursorPosition) {
      this.onSubmit = null;
      this.locked = false;
      this.ourOwnDefault = def;
      this.defaultCursorPosition = cursorPosition;
   }

   public InputWindow(String def, int cursorPosition, boolean locked) {
      this.onSubmit = null;
      this.locked = false;
      this.ourOwnDefault = def;
      this.defaultCursorPosition = cursorPosition;
      this.locked = locked;
   }

   public void initGui() {
      super.initGui();
      this.inputField.setMaxStringLength(100);
      this.inputField.writeText(this.ourOwnDefault);
      if (this.defaultCursorPosition >= 0) {
         this.inputField.setCursorPosition(this.defaultCursorPosition);
      }

   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (keyCode == 1) {
         Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
      } else {
         if (keyCode != 28 && keyCode != 156) {
            if (this.locked) {
               if (keyCode == 15) {
                  this.locked = false;
               }

               return;
            }

            super.keyTyped(typedChar, keyCode);
         } else {
            String s = this.inputField.getText().trim();
            if (s.length() > 0) {
               this.sendChatMessage(s);
            }

            if (Lynx.getChat().getActiveTab().getType() == TabType.PRIVATE_MESSAGES && s.length() > 0) {
               Minecraft.getMinecraft().displayGuiScreen(new InputWindow(""));
            } else {
               Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
            }
         }

      }
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      ChatFacade.get().click(mouseX, mouseY, mouseButton == 1);
   }

   public void setMaxChatLength(int max) {
      this.inputField.setMaxStringLength(max);
   }

   public InputWindow onSubmit(Consumer<String> onSubmit) {
      this.onSubmit = onSubmit;
      return this;
   }

   public void sendChatMessage(String msg, boolean addToChat) {
      this.determineSend(msg, addToChat);
   }

   private void determineSend(String s, boolean add) {
      Tab t = ChatFacade.get().getActiveTab();
      if (s.toLowerCase().startsWith("/target ")) {
         Lynx.target(s.substring(8));
         Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages(s);
         Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
      } else if (s.toLowerCase().contains("/targetrandom") || s.toLowerCase().contains("/tr") ) {
         EntityPlayer random = mc.theWorld.playerEntities.get(RNG.THREAD_LOCAL.current().nextInt(0, mc.theWorld.playerEntities.size() - 1));
         Lynx.target(random.getName());
         Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages(s);
         Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
      }
      else if (s.toLowerCase().contains("/followrandom") || s.toLowerCase().contains("/fr") ) {
         EntityPlayer random = mc.theWorld.playerEntities.get(RNG.THREAD_LOCAL.current().nextInt(0, mc.theWorld.playerEntities.size() - 1));
         Lynx.target(random.getName());

         ChatFacade.get().sendMessage("/fol " + random.getName(), false);

         Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages(s);
         Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
      }
      else {
         if (!s.startsWith("/")) {
            s = ChatFacade.get().getActiveTab().applyChatRules(s).trim();
         }

         if (t.getType() == TabType.PRIVATE_MESSAGES && !s.startsWith("/")) {
            String name = ((PMWindow)t.getWindow()).getCurrentChatter();
            if (name != null) {
               CommandChannels.PRIVATE_MESSAGES.execute(name, (map) -> {
                  String val = map.get("offline");
                  if (val != null) {
                     ChatFacade.get().getTabs().forEach((tab) -> {
                        if (tab.getType() == TabType.PRIVATE_MESSAGES) {
                           PMWindow window = (PMWindow)tab.getWindow();
                           window.printChatMessageWithOptionalDeletion(new ChatComponentText(val), 0, name);
                        }

                     });
                  }

               });
               s = "/msg " + name + " " + s;
            }
         }

         if (s.length() > 0) {
            ChatFacade.get().sendMessage(s, add);
            if (this.onSubmit != null) {
               this.onSubmit.accept(s);
            }
         }

      }
   }
}
