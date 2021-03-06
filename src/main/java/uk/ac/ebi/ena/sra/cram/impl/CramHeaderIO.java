package uk.ac.ebi.ena.sra.cram.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;

import uk.ac.ebi.ena.sra.cram.format.CramHeader;
import uk.ac.ebi.ena.sra.cram.format.CramHeaderRecord;
import uk.ac.ebi.ena.sra.cram.format.CramReadGroup;
import uk.ac.ebi.ena.sra.cram.format.CramReferenceSequence;
import uk.ac.ebi.ena.sra.cram.format.ReadAnnotation;

public class CramHeaderIO {
	private static final String MAGIC = "CRAM";

	public static void write(CramHeader header, OutputStream os) throws IOException {
		DataOutputStream headerDOS = new DataOutputStream(os);
		headerDOS.writeUTF(MAGIC);
		headerDOS.writeUTF(header.getVersion());

		headerDOS.writeInt(header.getReferenceSequences().size());
		for (CramReferenceSequence s : header.getReferenceSequences()) {
			headerDOS.writeUTF(s.getName());
			headerDOS.writeInt(s.getLength());
		}

		if (header.getReadAnnotations() == null || header.getReadAnnotations().isEmpty())
			headerDOS.writeInt(0);
		else {
			headerDOS.writeInt(header.getReadAnnotations().size());
			for (ReadAnnotation ra : header.getReadAnnotations())
				headerDOS.writeUTF(ra.getKey());
		}

		if (header.getReadGroups() == null || header.getReadGroups().isEmpty())
			headerDOS.writeInt(0);
		else {
			headerDOS.writeInt(header.getReadGroups().size());
			for (CramReadGroup rg : header.getReadGroups()) {
				if (rg.getId() == null)
					headerDOS.writeUTF("");
				else
					headerDOS.writeUTF(rg.getId());

				if (rg.getSample() == null)
					headerDOS.writeUTF("");
				else
					headerDOS.writeUTF(rg.getSample());
			}
		}

		List<CramHeaderRecord> records = header.getRecords();
		if (records == null || records.isEmpty()) {
			headerDOS.writeInt(0);
		} else {
			headerDOS.writeInt(records.size());
			for (CramHeaderRecord record : records) {
				byte[] tagBytes = record.getTag().getBytes();
				if (tagBytes.length != 2)
					throw new RuntimeException("Header tag must be 2 bytes long: " + record.getTag());
				headerDOS.write(tagBytes);

				Set<String> keySet = record.getKeySet();
				headerDOS.writeInt(keySet.size());
				for (String key : keySet) {
					byte[] keyBytes = key.getBytes();
					if (keyBytes.length != 2)
						throw new RuntimeException("Header tag key must be 2 bytes long: " + key);
					headerDOS.write(keyBytes);

					String value = record.getValue(key);
					headerDOS.writeUTF(value);
				}
			}
		}

		headerDOS.flush();
	}

	public static CramHeader read(InputStream is) throws IOException {
		CramHeader header = new CramHeader();
		DataInputStream dis = null;
		if (is instanceof DataInputStream)
			dis = (DataInputStream) is;
		else
			dis = new DataInputStream(is);

		String magick = dis.readUTF();
		if (!MAGIC.equals(magick))
			throw new RuntimeException("Not recognized as CRAM format.");
		header.setVersion(dis.readUTF());
		int seqCount = dis.readInt();
		ArrayList<CramReferenceSequence> list = new ArrayList<CramReferenceSequence>();
		for (int i = 0; i < seqCount; i++) {
			CramReferenceSequence cramSeq = new CramReferenceSequence();
			cramSeq.setName(dis.readUTF());
			cramSeq.setLength(dis.readInt());
			list.add(cramSeq);
		}
		header.setReferenceSequences(list);

		int annSize = dis.readInt();
		List<ReadAnnotation> annotations = new ArrayList<ReadAnnotation>(annSize);
		for (int i = 0; i < annSize; i++) {
			String annKey = dis.readUTF();
			annotations.add(new ReadAnnotation(annKey));
		}
		header.setReadAnnotations(annotations);

		int rgSize = dis.readInt();
		List<CramReadGroup> rgList = new ArrayList<CramReadGroup>(rgSize);
		for (int i = 0; i < rgSize; i++) {
			String id = dis.readUTF();
			if (id.length() == 0)
				id = null;
			String sample = dis.readUTF();
			if (sample.length() == 0)
				sample = null;
			rgList.add(new CramReadGroup(id, sample));
		}
		header.setReadGroups(rgList);

		int recordCount = dis.readInt();
		List<CramHeaderRecord> records = header.getRecords();
		for (int recordIndex = 0; recordIndex < recordCount; recordIndex++) {
			byte[] tagBytes = new byte[2];
			dis.readFully(tagBytes);
			CramHeaderRecord record = new CramHeaderRecord(new String(tagBytes));
			records.add(record);

			int recordSize = dis.readInt();
			for (int keyIndex = 0; keyIndex < recordSize; keyIndex++) {
				byte[] keyBytes = new byte[2];
				dis.readFully(keyBytes);
				record.setValue(new String(keyBytes), dis.readUTF());
			}
		}

		return header;
	}
}
