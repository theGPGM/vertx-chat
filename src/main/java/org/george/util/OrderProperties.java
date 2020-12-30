package org.george.util;

import java.util.*;

/**
 * 继承 Properties，实现了顺序输入、输出配置文件
 */
public class OrderProperties extends Properties{

  private static final long serialVersionUID = -4627607243846121965L;

  private final LinkedHashSet<Object> keys = new LinkedHashSet<Object>();

  public Enumeration<Object> keys() {
    return Collections.<Object> enumeration(keys);
  }

  public Object put(Object key, Object value) {
    keys.add(key);
    return super.put(key, value);
  }

  public Set<Object> keySet() {
    return keys;
  }

  public Set<String> stringPropertyNames() {
    Set<String> set = new LinkedHashSet<String>();

    for (Object key : this.keys) {
      set.add((String) key);
    }

    return set;
  }
}
