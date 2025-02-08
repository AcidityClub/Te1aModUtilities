package net.hypixel.lynx.chat.tab;

import com.codelanx.commons.util.ref.Box;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import net.hypixel.lynx.chat.ChatFacade;
import net.hypixel.lynx.config.ClientConfig;
import net.hypixel.lynx.ui.Regions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;

public class Window extends GuiNewChat {
   public static final int LINE_HEIGHT = 9;
   static final int DEFAULT_MAX_INPUT_LENGTH = 100;
   private static final int FADE_DURATION_MS = 5000;
   private static final int FADE_WAIT_BUFFER = 4000;
   private static int top;
   private static int left = 0;
   private static int right;
   private static int bottom;
   protected final List<Function<IChatComponent, IChatComponent>> messageMutators;
   private final List<ChatLine> splitChatLines;
   private final List<ChatLine> lines;
   private final List<String> sentMessages;
   private final Minecraft mc;
   protected int textLeftAlignment;
   protected int maxLines;
   protected int scroll;
   private int lastTab;
   private long windowFade;

   public Window() {
      this(Minecraft.getMinecraft());
   }

   public Window(Minecraft mc) {
      super(mc);
      this.messageMutators = new LinkedList();
      this.splitChatLines = new LinkedList();
      this.lines = new LinkedList();
      this.sentMessages = new LinkedList();
      this.textLeftAlignment = 0;
      this.maxLines = 100;
      this.scroll = 0;
      this.lastTab = Integer.MIN_VALUE;
      this.windowFade = Long.MIN_VALUE;
      this.mc = mc;
   }

   public static int getTop() {
      return top;
   }

   public static int getRight() {
      return right;
   }

   public static int getLeft() {
      return left;
   }

   public static int getBottom() {
      return bottom;
   }

   public static float getOpacity() {
      return Minecraft.getMinecraft().gameSettings.chatOpacity * 0.9F + 0.1F;
   }

   protected List<ChatLine> getSplitChatLines(String input) {
      return this.getSplitChatLines(input, (String)null);
   }

   public void addMessageMutator(Function<IChatComponent, IChatComponent> func) {
      this.messageMutators.add(func);
   }

   public IChatComponent applyChatRules(IChatComponent in) {
      if (this.messageMutators.isEmpty()) {
         return in;
      } else {
         Box<IChatComponent> box = new Box();
         box.value = in;
         this.messageMutators.forEach((r) -> {
            IChatComponent var10000 = (IChatComponent)(box.value = r.apply(box.value));
         });
         return (IChatComponent)box.value;
      }
   }

   public void setLastTab(int lastTab) {
      this.lastTab = lastTab;
   }

   public void drawChat(int tick) {
      if (this.mc.gameSettings.chatVisibility != EnumChatVisibility.HIDDEN) {
         this.renderChat(tick, false);
      }

   }

   public void clearChatMessages() {
      this.splitChatLines.clear();
      this.lines.clear();
      this.sentMessages.clear();
   }

   public void printChatMessageWithOptionalDeletion(IChatComponent chat, int index) {
      this.printChatMessageWithOptionalDeletion(chat, index, (String)null);
   }

   public void printChatMessageWithOptionalDeletion(IChatComponent chat, int index, String username) {
      this.vanillaSetChatLine(chat, index, this.mc.ingameGUI.getUpdateCounter(), false, username);
   }

   public void refreshChat() {
      this.splitChatLines.clear();
      this.resetScroll();

      for(int i = this.lines.size() - 1; i >= 0; --i) {
         ChatLine chatline = (ChatLine)this.lines.get(i);
         this.vanillaSetChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
      }

   }

   public List<String> getSentMessages() {
      return this.sentMessages;
   }

   public void addToSentMessages(String message) {
      String last = this.sentMessages.isEmpty() ? null : (String)this.sentMessages.get(this.sentMessages.size() - 1);
      if (!message.equals(last)) {
         this.sentMessages.add(message);
      }

   }

   public void resetScroll() {
      this.scroll = 0;
   }

   public void scroll(int linesMoved) {
      this.scroll += linesMoved;
      int i = this.getSplitChatLines((String)null).size();
      if (this.scroll > i - this.getLineCount()) {
         this.scroll = i - this.getLineCount();
      }

      if (this.scroll <= 0) {
         this.scroll = 0;
      }

   }

   private boolean isScrolled() {
      return this.scroll > 0;
   }

   public int getLineCount() {
      return this.getChatHeight() / 9;
   }

   public void hover(int x, int y) {
   }

   public void windowSwitched() {
      this.windowFade = System.currentTimeMillis() + 5000L;
      if (ChatFacade.get() != null) {
         InputWindow iw = ChatFacade.get().getInputWindow();
         if (iw != null) {
            iw.setMaxChatLength(this.getMaxStringLength());
         }

      }
   }

   private ChatFacade getFacade() {
      return ChatFacade.get();
   }

   public boolean isActive() {
      ChatFacade cf = this.getFacade();
      return cf.getActiveTab().getWindow() == this;
   }

   public IChatComponent getChatComponent(int width, int height) {
      return this.vanillaGetChatComponent(width, height);
   }

   public int getMaxStringLength() {
      int len = ChatFacade.get().getActiveTab().applyChatRules("").length();
      return 100 - len;
   }

   public void deleteChatLine(int messageID) {
      this.splitChatLines.removeIf((l) -> {
         return l.getChatLineID() == messageID;
      });
      this.lines.removeIf((l) -> {
         return l.getChatLineID() == messageID;
      });
   }

   public void click(int x, int y, boolean rightClick) {
   }

   public int getMaxLines() {
      return this.maxLines;
   }

   public void setMaxLines(int lines) {
      this.maxLines = lines;
   }

   private int getAlphaFade() {
      long curr = System.currentTimeMillis();
      if (curr >= this.windowFade) {
         this.windowFade = Long.MIN_VALUE;
         return -1;
      } else {
         long diff = this.windowFade - curr;
         if (diff > 1000L) {
            return 255;
         } else {
            diff -= 4000L;
            float percentage = (float)diff / 1000.0F;
            return (int)(255.0F * percentage);
         }
      }
   }

   protected void renderChat(int tick, boolean pmWindow) {
      int lineCount = this.getLineCount();
      boolean input = this.getChatOpen();
      int shownLines = 0;
      List<ChatLine> chatLines = this.getSplitChatLines((String)null);
      int lines = chatLines.size();
      boolean maximized = pmWindow || (Boolean)ClientConfig.KEEP_CHATWINDOW_MAXIMIZED.as(Boolean.TYPE);
      if (pmWindow || lines > 0) {
         float scale = this.getChatScale();
         int rightBuff = MathHelper.ceiling_float_int((float)this.getChatWidth() / scale);
         GlStateManager.pushMatrix();
         GlStateManager.translate(2.0F, 20.0F, 0.0F);
         GlStateManager.scale(scale, scale, 1.0F);
         int left = 0;
         float leftText = (float)(left + this.textLeftAlignment);
         int right = left + rightBuff + 4;
         int tabDuration = tick - this.lastTab;
         if (tabDuration > 200) {
            tabDuration = Integer.MIN_VALUE;
         }

         int textAlpha = (int)(255.0F * getOpacity());
         int textColor = Regions.colorFromRGB(255, 255, 255, textAlpha);
         int windowColor = Regions.colorFromRGB(0, 0, 0, textAlpha / 2);
         List<Runnable> renders = null;
         int i;
         ChatLine chatline;
         int elapsedTicks;
         if (!input && !maximized) {
            int alpha;
            if (this.windowFade <= 0L) {
               if (!pmWindow) {
                  for(i = 0; i + this.scroll < chatLines.size() && i < lineCount; ++i) {
                     chatline = (ChatLine)chatLines.get(i + this.scroll);
                     elapsedTicks = tick - chatline.getUpdatedCounter();
                     if (elapsedTicks < 200) {
                        alpha = this.getAlphaFade((double)(tabDuration > 0 ? Math.max(elapsedTicks, tabDuration) : elapsedTicks) / 200.0D);
                        if (alpha > 3) {
                           alpha = (int)((float)alpha * getOpacity());
                           ++shownLines;
                           windowColor = Regions.colorFromRGB(0, 0, 0, alpha / 2);
                           int txtColor = Regions.colorFromRGB(255, 255, 255, alpha);
                           int bottom = -i * 9 - 9;
                           Gui.drawRect(left, bottom + 9, right, bottom, windowColor);
                           this.renderChatText(chatline.getChatComponent().getFormattedText(), leftText, (float)bottom + 1.0F, txtColor);
                        }
                     }
                  }
               }
            } else {
               renders = new LinkedList();
               textAlpha = (int)((float)this.getAlphaFade() * getOpacity());
               i = Regions.colorFromRGB(255, 255, 255, textAlpha);
               windowColor = Regions.colorFromRGB(0, 0, 0, textAlpha / 2);
               shownLines = Math.min(chatLines.size() - this.scroll, lineCount);

               for(int i2 = 0; i2 < shownLines; ++i2) {
                  ChatLine chatline2 = (ChatLine)chatLines.get(i + this.scroll);
                  float alpha2 = -i * 9 - 9 + 1;
                  int finalI = i2;
                  renders.add(() -> {
                     this.renderChatText(chatline2.getChatComponent().getFormattedText(), leftText, (float)alpha2, finalI);
                  });
               }

               maximized = true;
            }
         } else {
            renders = new LinkedList();
            shownLines = Math.min(chatLines.size() - this.scroll, lineCount);

            for(i = 0; i < shownLines; ++i) {
               ChatLine chatline2 = (ChatLine)chatLines.get(i + this.scroll);
               float elapsedTicks2 = -i * 9 - 9 + 1;
               renders.add(() -> {
                  this.renderChatText(chatline2.getChatComponent().getFormattedText(), leftText, (float)elapsedTicks2, textColor);
               });
            }
         }

         i = -shownLines * 9;
         int top = 0;
         if (maximized) {
            i = -this.getLineCount() * 9;
         }

         if (maximized || input && shownLines > 0) {
            Gui.drawRect(left, top, right, i, windowColor);
            if (renders != null) {
               renders.forEach(Runnable::run);
            }
         }

         if (input && lines > 0) {
            this.renderScrollbar(lines, shownLines);
         }

         Window.bottom = i;
         Window.right = right;
         Window.top = top;
         GlStateManager.popMatrix();
      }

   }

   private void renderChatText(String s, float left, float top, int color) {
      GlStateManager.enableBlend();
      this.mc.fontRendererObj.drawStringWithShadow(s, left, top, color);
      GlStateManager.disableAlpha();
      GlStateManager.disableBlend();
   }

   protected void renderScrollbar(int lines, int shownLines) {
      int fontHeight = this.mc.fontRendererObj.FONT_HEIGHT;
      GlStateManager.translate(-3.0F, 0.0F, 0.0F);
      int openHeight = lines * fontHeight + lines;
      int shownHeight = shownLines * fontHeight + shownLines;
      int scrollbarPos = this.scroll * shownHeight / lines;
      int k1 = shownHeight * shownHeight / openHeight;
      if (openHeight != shownHeight) {
         int alpha = scrollbarPos > 0 ? 170 : 96;
         Gui.drawRect(0, -scrollbarPos, 2, -scrollbarPos - k1, (this.isScrolled() ? 13382451 : 3355562) + (alpha << 24));
         Gui.drawRect(2, -scrollbarPos, 1, -scrollbarPos - k1, 13421772 + (alpha << 24));
      }

   }

   private int getAlphaFade(double percentage) {
      percentage = 1.0D - percentage;
      percentage *= 10.0D;
      percentage = MathHelper.clamp_double(percentage, 0.0D, 1.0D);
      percentage *= percentage;
      return (int)(255.0D * percentage);
   }

   private void vanillaSetChatLine(IChatComponent message, int index, int ticks, boolean skipLinesCollection) {
      this.vanillaSetChatLine(message, index, ticks, skipLinesCollection, (String)null);
   }

   public List<ChatLine> getSplitChatLines(String message, String username) {
      return this.splitChatLines;
   }

   private void vanillaSetChatLine(IChatComponent message, int index, int ticks, boolean skipLinesCollection, String username) {
      if (index != 0) {
         this.deleteChatLine(index);
      }

      List<ChatLine> chatLines = this.getSplitChatLines(message.getFormattedText());
      message = this.applyChatRules(message);
      int custWidth = this.getCustomChatWidth();
      int width = MathHelper.floor_float((float)custWidth / this.getChatScale());
      List<IChatComponent> list = GuiUtilRenderComponents.func_178908_a(message, width, this.mc.fontRendererObj, false, false);
      boolean input = this.getChatOpen();

      IChatComponent ichatcomponent;
      for(Iterator var11 = list.iterator(); var11.hasNext(); chatLines.add(0, new ChatLine(ticks, ichatcomponent, index))) {
         ichatcomponent = (IChatComponent)var11.next();
         if (input && this.isScrolled()) {
            this.scroll(1);
         }
      }

      while(chatLines.size() > this.maxLines) {
         chatLines.remove(chatLines.size() - 1);
      }

      if (!skipLinesCollection) {
         this.lines.add(0, new ChatLine(ticks, message, index));

         while(this.lines.size() > this.maxLines) {
            this.lines.remove(this.lines.size() - 1);
         }
      }

   }

   public int getCustomChatWidth() {
      return super.getChatWidth();
   }

   private IChatComponent vanillaGetChatComponent(int width, int height) {
      if (!this.getChatOpen()) {
         return null;
      } else {
         ScaledResolution scaledresolution = new ScaledResolution(this.mc);
         int heightMod = this.heightScale(height, scaledresolution);
         int widthMod = this.widthScale(width, scaledresolution);
         if (widthMod >= 0 && heightMod >= 0) {
            List<ChatLine> chatLines = this.getSplitChatLines((String)null);
            int linesCap = Math.min(this.getLineCount(), chatLines.size());
            int floored = MathHelper.floor_float((float)this.getCustomChatWidth() / this.getChatScale());
            int linesFuckery = this.mc.fontRendererObj.FONT_HEIGHT * linesCap + linesCap;
            if (widthMod > this.textLeftAlignment && widthMod <= floored + this.textLeftAlignment && heightMod < linesFuckery) {
               int i = heightMod / this.mc.fontRendererObj.FONT_HEIGHT + this.scroll;
               if (i >= 0 && i < chatLines.size()) {
                  ChatLine chatline = (ChatLine)chatLines.get(i);
                  AtomicInteger position = new AtomicInteger(this.textLeftAlignment);
                  return (IChatComponent)StreamSupport.stream(chatline.getChatComponent().spliterator(), false).filter((o) -> {
                     return o instanceof ChatComponentText;
                  }).filter((comp) -> {
                     String val = GuiUtilRenderComponents.func_178909_a(((ChatComponentText)comp).getChatComponentText_TextValue(), false);
                     position.set(position.get() + this.mc.fontRendererObj.getStringWidth(val));
                     return position.get() > widthMod;
                  }).findFirst().orElse(null);
               } else {
                  return null;
               }
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   protected int widthScale(int relWidth, ScaledResolution res) {
      int resScale = res.getScaleFactor();
      float scale = this.getChatScale();
      int widthMod = relWidth / resScale - 3;
      widthMod = MathHelper.floor_float((float)widthMod / scale);
      return widthMod;
   }

   protected int heightScale(int relHeight, ScaledResolution res) {
      int resScale = res.getScaleFactor();
      float scale = this.getChatScale();
      int heightMod = relHeight / resScale - 27;
      heightMod = MathHelper.floor_float((float)heightMod / scale);
      return heightMod;
   }
}
