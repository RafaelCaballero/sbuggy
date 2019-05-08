package model.tuples;

import java.util.Arrays;

public class Tuple {
	private Field[] fields;

	public Tuple(int n) {
		fields = new Field[n];
	}

	public void set(int i, Field f) {
		fields[i] = f;
	}

	public Field get(int i) {
		return fields[i];
	}

	public int size() {
		int result = 0;
		if (fields != null) {
			result = fields.length;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(fields);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tuple other = (Tuple) obj;
		if (!Arrays.equals(fields, other.fields))
			return false;
		return true;
	}

}
