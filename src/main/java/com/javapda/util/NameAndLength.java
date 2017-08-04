package com.javapda.util;


import java.io.Serializable;

public class NameAndLength implements Serializable {
	
	private static final long serialVersionUID = -1753835309960382069L;
	private String name;
	private Long length;
	protected NameAndLength(String name2, Long length2) {
		this.name = name2;
		this.length = length2;
	}
	public static NameAndLength create(String name2, Long length2) {
		return new NameAndLength(name2, length2);
	}
	public String getName() {
		return name;
	}
	public Long getLength() {
		return length;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((length == null) ? 0 : length.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NameAndLength other = (NameAndLength) obj;
		if (length == null) {
			if (other.length != null)
				return false;
		} else if (!length.equals(other.length))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "NameAndLength [name=" + name + ", length=" + length + "]";
	}
}