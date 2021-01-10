package org.george.auction.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtils {
  public static Properties loadProperties(String file) {
    OrderProperties p = new OrderProperties();
    FileInputStream io = null;
    try {
      io = new FileInputStream(file);
      p.load(io);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        io.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return p;
  }
}
