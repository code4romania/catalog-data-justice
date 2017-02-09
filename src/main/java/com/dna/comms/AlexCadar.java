package com.dna.comms;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.dna.comms.codecs.DNACommsJSONCodec;
import com.dna.comms.entities.ProsecutionComm;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AlexCadar {
	public static void main(String[] args) {
		try {
			List<ProsecutionComm> comms = new ArrayList<>();
			DNACommsJSONCodec codec = new DNACommsJSONCodec();
			String jsonComms = StrangeUtils.getFileContent(new File("e:/USR/DNA & Imunitati/Output/comms_raw.json"));
			comms = new ObjectMapper().readValue(jsonComms, new TypeReference<List<ProsecutionComm>>() {
			});

			Map<String, List<Integer>> penali = new HashMap<>();

			String folder = "e:/USR/DNA & Imunitati/Parlament/";
			// read the excel spreadsheet
			XSSFWorkbook book = new XSSFWorkbook(new FileInputStream(folder + "Senat.xlsx"));
			XSSFSheet sheet = book.getSheet("2012");

			for (Row row : sheet) {
				if (row.getCell(1).getHyperlink() == null)
					continue;

				String name = row.getCell(1).getStringCellValue();
				name = StrangeUtils.deAccent(name).toUpperCase();

				for (ProsecutionComm comm : comms) {
					String suspectNames = codec.encodeAny(comm.getSuspects(), false);
					if (suspectNames.toUpperCase().contains(name)) {
						// System.out.println(comm.getId() + "\t" + name);

						if (!penali.containsKey(name))
							penali.put(name, new ArrayList<>());

						penali.get(name).add(comm.getId());
					}
				}
			}
			book.close();

			for (String name : penali.keySet()) {
				System.out.println(name + ": " + penali.get(name));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
