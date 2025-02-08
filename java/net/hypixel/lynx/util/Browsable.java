package net.hypixel.lynx.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Browsable {
   private static final String GOLIATH_USERNAME_FORMAT = "https://goliath.hypixel.net/userinfo?username=%s&uuid=";

   public static void goliath(String username) {
      try {
         Desktop.getDesktop().browse(new URI(String.format("https://goliath.hypixel.net/userinfo?username=%s&uuid=", username)));
      } catch (URISyntaxException | IOException var2) {
         Util.error(var2, "Error opening goliath browser page for '%s'", username);
      }

   }
}
