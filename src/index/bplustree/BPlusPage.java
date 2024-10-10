package index.bplustree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BPlusPage {
	private int order;
	public ArrayList<BPlusRegister> registers;
	public ArrayList<Long> childrens;
	private long next;

	// order
  public int getOrder() {
    return this.order;
  }

  public void setOrder(int order) {
    if (order >= 3) {
      this.order = order;
    } else {
      System.out.println("A ordem precisa ser no minimo 3!");
      this.order = 3;
    }
  }

	// registers
  public ArrayList<BPlusRegister> getRegisters() {
    return this.registers;
  }

  public BPlusRegister getRegister(int index) {
    return this.registers.get(index);
  }

	public int getMaxRegisters() {
		return getMaxRegisters(this.order);
	}

	public static int getMaxRegisters(int order) {
		return order - 1;
	}

	public int getRegistersSize() {
		return this.registers.size();
	}

  public void setRegisters(ArrayList<BPlusRegister> registers) {
    this.registers = registers;
  }

  private void setRegisters() {
    this.registers = new ArrayList<>(this.order - 1);
  }

  public void addRegister(BPlusRegister register) {
    this.registers.add(register);
  }

  public void addRegister(BPlusRegister register, int index) {
    this.registers.add(index, register);
  }

  public BPlusRegister removeRegister(int index) {
    return this.registers.remove(index);
  }

  public BPlusRegister removeLastRegister() {
    return this.registers.remove(this.registers.size() - 1);
  }

	// childrens
  public ArrayList<Long> getChildrens() {
    return this.childrens;
  }

  public long getChild(int index) {
		return this.childrens.get(index);
  }

	public int getMaxChildrens() {
		return this.order;
	}

  public void setChildrens(ArrayList<Long> childrens) {
    this.childrens = childrens;
  }

  private void setChildrens() {
    this.childrens = new ArrayList<>(this.order);
  }

  public void addChild(long child) {
    this.childrens.add(child);
  }

  public void addChild(long child, int index) {
    this.childrens.add(index, child);
  }

  public long removeChild(int index) {
    return this.childrens.remove(index);
  }

  public long removeLastChild() {
    return this.childrens.remove(this.childrens.size() - 1);
  }

	// next
  public long getNext() {
    return this.next;
  }

	public boolean hasNext() {
		return this.next != -1;
	}

  public void setNext(long next) {
    this.next = next;
  }

	// constructors
	public BPlusPage(int order) throws IOException {
		this.setOrder(order);
		this.setNext(-1);

		this.setRegisters();
		this.setChildrens();
	}

	public BPlusPage(byte[] buffer, int order) throws IOException {
		this.setOrder(order);

    this.setRegisters();
    this.setChildrens();

		ByteArrayInputStream input = new ByteArrayInputStream(buffer);
    DataInputStream data = new DataInputStream(input);

		// Lê a quantidade de elementos presentes na página
		int registers = data.readInt();

		int i = 0;
		while (i < registers) {
			this.addChild(data.readLong());

			byte[] registerBuffer = new byte[BPlusRegister.getSize()];
			data.read(registerBuffer);
			this.addRegister(new BPlusRegister(registerBuffer));

			i++;
		}

		this.addChild(data.readLong());
		data.skipBytes((this.getMaxRegisters() - i) * (BPlusRegister.getSize() + Long.BYTES));

		this.setNext(data.readLong());
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
    DataOutputStream data = new DataOutputStream(output);

		int registers = this.getRegistersSize();

		data.writeInt(registers);

		int i = 0;
		while (i < registers) {
			data.writeLong(this.getChild(i));
			data.write(this.getRegister(i).toByteArray());

			i++;
		}

		if (this.childrens.size() > 0) {
			data.writeLong(this.childrens.get(i).longValue());
		} else {
			data.writeLong(-1L);
		}

		// Completa o restante da página com registros vazios
    byte[] emptyRegister = new byte[BPlusRegister.getSize()];
		while (i < this.getMaxRegisters()) {
      data.write(emptyRegister);
      data.writeLong(-1L);

      i++;
    }

		data.writeLong(this.getNext());

    return output.toByteArray();
	}

	public boolean isFull() {
		return this.registers.size() >= this.getMaxRegisters();
	}

	public boolean isLeaf() {
		return this.getChild(0) == -1;
	}

	public static int getSize(int order) {
		// numOfRegisters + (register.SIZE + child.SIZE)(order - 1) + lastChild + next
    return Integer.BYTES + (Long.BYTES + BPlusRegister.getSize()) * (getMaxRegisters(order)) + 2 * Long.BYTES;
	}
}
