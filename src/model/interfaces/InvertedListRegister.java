package model.interfaces;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

  public InvertedListRegister(byte[] buffer) throws IOException {
    ByteArrayInputStream input = new ByteArrayInputStream(buffer);
    DataInputStream data = new DataInputStream(input);

    this.setKey(data.readUTF());

    int size = data.readInt();
    for (int i = 0; i < size; i++) {
      this.getIds().add(data.readInt());
    }
  }

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    DataOutputStream data = new DataOutputStream(output);

    data.writeUTF(this.getKey());
    data.writeInt(this.getIds().size());
    for (Integer id : ids) {
      data.writeInt(id);
    }

    return output.toByteArray();
  }
}
