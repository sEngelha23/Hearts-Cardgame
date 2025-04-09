package de.hso.cardgame.util;

import java.util.logging.*;
import java.io.*;

public class Logging {
      static {
          String path = Logging.class.getClassLoader()
                  .getResource("logging.properties")
                  .getFile();
          try {
              FileInputStream stream = new FileInputStream(path);
              LogManager.getLogManager().readConfiguration(stream);
              System.err.println("Successfully configured logging via file " + path);
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
      
      public static Logger getLogger(String name) {
          return Logger.getLogger(name);
      }
}
