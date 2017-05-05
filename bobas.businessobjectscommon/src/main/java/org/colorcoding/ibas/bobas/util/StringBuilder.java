package org.colorcoding.ibas.bobas.util;

public class StringBuilder {

	public StringBuilder() {
		stringBuilder = new java.lang.StringBuilder();
	}

	private java.lang.StringBuilder stringBuilder = null;

	@Override
	public String toString() {
		return this.stringBuilder.toString();
	}

	public int length() {
		return this.stringBuilder.length();
	}

	public void setLength(int newLength) {
		this.stringBuilder.setLength(newLength);
	}

	public void append(String str) {
		this.stringBuilder.append(str);
	}

	public void append(int str) {
		this.stringBuilder.append(str);
	}

	public void insert(int offset, String str) {
		this.stringBuilder.insert(offset, str);
	}

	public void appendFormat(String str, Object... args) {
		if (args.length == 0) {
			this.stringBuilder.append(str);
		} else {
			this.stringBuilder.append(String.format(str, args));
		}
	}

	public void insertFormat(int offset, String str, Object... args) {
		if (args.length == 0) {
			this.stringBuilder.insert(offset, str);
		} else {
			this.stringBuilder.insert(offset, String.format(str, args));
		}
	}

	public void append(char c) {
		this.stringBuilder.append(c);
	}

	public int lastIndexOf(String str) {
		return this.stringBuilder.lastIndexOf(str);
	}

	public int lastIndexOf(String str, int fromIndex) {
		return this.stringBuilder.lastIndexOf(str, fromIndex);
	}
}
