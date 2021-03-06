package uk.ac.ebi.ena.sra.cram.mask;

import java.util.Arrays;

public class ArrayPositionMask implements PositionMask {
	private int[] array;

	public ArrayPositionMask(int[] array) {
		if (array == null)
			throw new NullPointerException("Expecting a non-null array.");
		this.array = array;
		Arrays.sort(array);
	}

	@Override
	public boolean isMasked(int position) {
		return Arrays.binarySearch(array, position) > -1;
	}

	@Override
	public int[] getMaskedPositions() {
		return array;
	}

	@Override
	public boolean isEmpty() {
		return array.length == 0;
	}

	@Override
	public int getMaskedCount() {
		return array.length;
	}

	@Override
	public int getMinMaskedPosition() {
		return array.length > 0 ? array[0] : -1;
	}

	@Override
	public int getMaxMaskedPosition() {
		return array.length > 0 ? array[array.length - 1] : -1;
	}

	public static final PositionMask EMPTY_INSTANCE = new ArrayPositionMask(
			new int[] {});

	@Override
	public byte[] toByteArrayUsing(byte mask, byte nonMask) {
		byte[] ba = new byte[array[array.length-1]] ;
		Arrays.fill(ba, nonMask) ;
		for (int pos:array) 
			ba[pos-1] = mask ;
		
		return ba;
	}
}
