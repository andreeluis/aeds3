package index.InvertedList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class InvertedListIndexRegister {
    private String key;
    private long position;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public InvertedListIndexRegister(String key, long position) {
        setKey(key);
        setPosition(position);
    }

    public InvertedListIndexRegister(byte[] buffer) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(buffer);
        DataInputStream data = new DataInputStream(input);

        this.setKey(data.readUTF());
        this.setPosition(data.readLong());
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(output);

        data.writeUTF(this.getKey());
        data.writeLong(this.getPosition());

        return output.toByteArray();
    }
}
