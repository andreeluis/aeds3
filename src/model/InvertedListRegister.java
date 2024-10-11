package model;

import java.util.List;

public class InvertedListRegister {
  private String key;
  private List<Integer> ids;

  // key
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  // ids
  public List<Integer> getIds() {
    return ids;
  }

  public void setIds(List<Integer> ids) {
    this.ids = ids;
  }

  // constructors
  public InvertedListRegister(String key, List<Integer> ids) {
    this.setIds(ids);
    this.setKey(key);
  }

  public InvertedListRegister(byte[] buffer) {

  }

  public byte[] toByteArray() {
    return null;
  }
}
