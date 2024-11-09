package db.compression.huffman;

public class HuffmanNode implements Comparable<HuffmanNode> {
	private byte data;
	private int frequency;
	private HuffmanNode left;
	private HuffmanNode right;

	// data
	public byte getData() {
		return data;
	}

	public void setData(byte data) {
		this.data = data;
	}

	// frequency
	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	// left
	public HuffmanNode getLeft() {
		return left;
	}

	public void setLeft(HuffmanNode left) {
		this.left = left;
	}

	// right
	public HuffmanNode getRight() {
		return right;
	}

	public void setRight(HuffmanNode right) {
		this.right = right;
	}

	// constructors
	public HuffmanNode(byte data, int frequency) {
		this.data = data;
		this.frequency = frequency;
	}

	public HuffmanNode(HuffmanNode left, HuffmanNode right) {
		this.left = left;
		this.right = right;
		this.frequency = left.frequency + right.frequency;
	}

	public boolean isLeaf() {
		return left == null && right == null;
	}

	@Override
	public int compareTo(HuffmanNode other) {
		return frequency - other.frequency;
	}
}
