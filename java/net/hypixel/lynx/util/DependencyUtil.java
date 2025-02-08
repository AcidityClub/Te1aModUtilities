package net.hypixel.lynx.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public class DependencyUtil {
   public static class Remapper {
      private static final String CSV_LOCATION = "C:/Users/Spencer/Desktop/mcp918/conf/";
      private static final File LIBRARY_FOLDER;

      public static void generateMappings() throws IOException {
         (new File("forge-mappings.srg")).delete();
         reverseMapping(new File("C:/Users/Spencer/Desktop/mcp918/conf/", "joined.srg"));
         getFQNSrg("fields", DependencyUtil.Remapper.PrefixAnalysisType.FIELD_MAPPING);
         getFQNSrg("methods", DependencyUtil.Remapper.PrefixAnalysisType.METHOD_MAPPING);
         reverseMapping("forge-mappings");
      }

      private static void reverseMapping(String orig) throws IOException {
         reverseMapping(new File(orig + ".srg"));
      }

      private static void reverseMapping(File orig) throws IOException {
         File o = new File(orig.getParentFile(), orig.getName().substring(0, orig.getName().lastIndexOf(".")) + "-reversed.srg");
         Scanner scan = new Scanner(orig);
         FileWriter out = new FileWriter(o);

         while(scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] syms = line.split("\\s");

            try {
               String fin = null;
               switch(DependencyUtil.Remapper.Type.valueOf(syms[0].split("\\:")[0])) {
               case MD:
                  if (syms.length > 3) {
                     fin = syms[0] + " " + syms[3] + " " + syms[4] + " " + syms[1] + " " + syms[2] + "\n";
                     break;
                  }
               case PK:
               case CL:
               case FD:
                  fin = syms[0] + " " + syms[2] + " " + syms[1] + "\n";
               }

               out.write(fin);
            } catch (Exception var7) {
               System.out.println("line caused error: " + line);
               var7.printStackTrace();
            }
         }

         out.flush();
      }

      public static Map<String, String> getObfMapping() {
         Map<String, String> back = new HashMap();
         Arrays.asList("methods", "fields").forEach((s) -> {
            try {
               Scanner scan = new Scanner(new File("C:/Users/Spencer/Desktop/mcp918/conf/", s + ".csv"));
               if (scan.hasNextLine()) {
                  scan.nextLine();
               }

               while(scan.hasNextLine()) {
                  String line = scan.nextLine();
                  String[] syms = line.split(",");
                  back.put(syms[0], syms[1]);
               }
            } catch (FileNotFoundException var5) {
               var5.printStackTrace();
            }

         });
         return back;
      }

      private static void getFQNSrg(String type, DependencyUtil.Remapper.PrefixAnalysisType kind) throws IOException {
         Map<String, Function<String, String>> joiner = getJoiner(kind);
         File f = new File("C:/Users/Spencer/Desktop/mcp918/conf/", type + ".csv");
         File o = new File("forge-mappings.srg");
         Scanner scan = new Scanner(f);
         FileWriter out = new FileWriter(o, true);
         int count = 0;
         if (scan.hasNextLine()) {
            scan.nextLine();
         }

         while(scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] syms = line.split(",");

            try {
               int i = Integer.parseInt(syms[2]);
               if (i != 0 && i != 2) {
                  continue;
               }
            } catch (NumberFormatException var13) {
               System.out.println("Tried to read bad side: " + syms[2]);
            }

            Function<String, String> trans = (Function)joiner.getOrDefault(syms[0], Function.identity());
            String s0 = (String)trans.apply(syms[0]);
            String s1 = (String)trans.apply(syms[1]);
            out.write(kind.getPrefix() + ": " + s0 + " " + s1 + "\n");
         }

         out.flush();
      }

      private static Map<String, Function<String, String>> getJoiner(DependencyUtil.Remapper.PrefixAnalysisType type) throws FileNotFoundException {
         Map<String, Function<String, String>> joins = new HashMap();
         File f = new File("C:/Users/Spencer/Desktop/mcp918/conf/", "joined-reversed.srg");
         Scanner scan = new Scanner(f);

         while(scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] syms = line.split(" ");
            line = syms[1];
            int index = line.lastIndexOf("/");
            if (index >= 0) {
               String orig = line.substring(index + 1);
               String to = line.substring(0, index + 1);
               Function<String, String> func = null;
               switch(type) {
               case FIELD_MAPPING:
                  func = (s) -> {
                     return to + s;
                  };
                  break;
               case METHOD_MAPPING:
                  func = (s) -> {
                     return to + s + " " + syms[2];
                  };
               }

               joins.put(orig, func);
            }
         }

         return joins;
      }

      static {
         LIBRARY_FOLDER = new File(System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator + "libraries" + File.separator);
      }

      private static enum PrefixAnalysisType {
         FIELD_MAPPING("FD"),
         METHOD_MAPPING("MD");

         private final String prefix;

         private PrefixAnalysisType(String prefix) {
            this.prefix = prefix;
         }

         public String getPrefix() {
            return this.prefix;
         }
      }

      private static enum Type {
         MD,
         PK,
         CL,
         FD;
      }
   }

   public static class Dependencies {
      public static final File LIBRARY_FOLDER;
      private static boolean done;

      public static void printDependencies() {
         Stream<? extends File> str = recurse(LIBRARY_FOLDER);
         System.out.println("<dependencies>");
         str.map((c) -> {
            return c.listFiles((j) -> {
               return j.getName().endsWith(".jar");
            })[0];
         }).forEach(DependencyUtil.Dependencies::parse);
         System.out.println("</dependencies>");
      }

      private static Stream<? extends File> recurse(File f) {
         Stream<? extends File> back = stream(f);
         if (folders(f).length > 0) {
            back = back.flatMap(DependencyUtil.Dependencies::recurse);
         }

         return back;
      }

      private static Stream<? extends File> stream(File f) {
         File[] fs = folders(f);
         return Arrays.stream(fs.length > 0 ? fs : new File[]{f});
      }

      private static File versions(File f) {
         String val = (String)Arrays.stream(folders(f)).map((v) -> {
            System.out.println("Testing: " + v);
            String t = v.toString();
            return t.substring(t.lastIndexOf("\\"));
         }).sorted().reduce((a, b) -> {
            return b;
         }).orElse((String) null);
         if (val == null) {
            return f;
         } else {
            File back = new File(f.toString() + File.separator + val);
            System.out.println("Returning max version: " + back.toString());
            return back;
         }
      }

      private static File[] folders(File f) {
         return f.listFiles(File::isDirectory);
      }

      private static void parse(File loc) {
         String[] vals = loc.toString().split("\\\\");
         String pkg = StringUtils.join(Arrays.asList(vals).subList(7, vals.length - 2), ".");
         String spot = loc.toString().substring(loc.toString().indexOf("libraries\\")).substring(10).replaceAll("\\\\", "/");
         format(spot, pkg, vals[vals.length - 3], vals[vals.length - 2]);

         try {
            Files.move(loc.toPath(), LIBRARY_FOLDER.toPath());
         } catch (IOException var5) {
         }

      }

      private static void format(String loc, String pkg, String name, String version) {
         System.out.println("    <dependency>\n        <groupId>" + pkg + "</groupId>\n        <artifactId>" + name + "</artifactId>\n        <version>" + version + "</version>\n        <scope>system</scope>\n        <systemPath>${env.APPDATA}/.minecraft/libraries/" + loc + "</systemPath>\n    </dependency>");
      }

      static {
         LIBRARY_FOLDER = new File(System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator + "libraries" + File.separator);
         done = false;
      }
   }

   @FunctionalInterface
   public interface TriConsumer<A, B, C> {
      void accept(A var1, B var2, C var3);
   }

   @FunctionalInterface
   public interface QuadConsumer<A, B, C, D> {
      void accept(A var1, B var2, C var3, D var4);
   }
}
