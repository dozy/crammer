package uk.ac.ebi.ena.sra.cram.io;

import java.io.ByteArrayOutputStream;

public class ExposedByteArrayOutputStream extends
		ByteArrayOutputStream {

	public ExposedByteArrayOutputStream(int size) {
		super(size);
	}

	public byte[] getBuffer() {
		return buf;
	}
}