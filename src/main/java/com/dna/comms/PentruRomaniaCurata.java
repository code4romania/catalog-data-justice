package com.dna.comms;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.dna.comms.entities.CommSuspectLink;
import com.dna.comms.entities.ProsecutionComm;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PentruRomaniaCurata {
	public static void main(String[] args) {
		try {
			String json = FileUtils.readFileToString(new File("e:/USR/DNA & Imunitati/Output/comms_raw.json"), "UTF-8");
			List<ProsecutionComm> comms = new ObjectMapper().readValue(json, new TypeReference<List<ProsecutionComm>>() {
			});

			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
			XSSFWorkbook book = new XSSFWorkbook();
			XSSFSheet sheet = book.createSheet("Toate");
			int i = 0;
			for (ProsecutionComm comm : comms) {
				for (CommSuspectLink suspectLink : comm.getSuspects()) {
					xlsWrite(sheet, i, 0, suspectLink.getPerson().getName()); // suspect name
					xlsWrite(sheet, i, 1, sdf.format(comm.getReleaseDate())); // comm date
					xlsWrite(sheet, i, 2, comm.getTitle()); // comm title
					xlsWrite(sheet, i, 3, comm.getLink()); // comm link
					i++;
				}
			}

			book.write(new FileOutputStream("e:/USR/DNA & Imunitati/Output/roCurata.xlsx"));
			book.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void xlsWrite(XSSFSheet sheet, int i, int j, String value) {
		if (sheet.getRow(i) == null)
			sheet.createRow(i);
		if (sheet.getRow(i).getCell(j) == null)
			sheet.getRow(i).createCell(j);
		sheet.getRow(i).getCell(j).setCellValue(value);
	}

	private static void xlsWrite(XSSFSheet sheet, int i, int j, int value) {
		if (sheet.getRow(i) == null)
			sheet.createRow(i);
		if (sheet.getRow(i).getCell(j) == null)
			sheet.getRow(i).createCell(j);
		sheet.getRow(i).getCell(j).setCellValue(value);
	}
}
