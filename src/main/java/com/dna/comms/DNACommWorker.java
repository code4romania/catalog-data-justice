package com.dna.comms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;

import com.dna.comms.codecs.DNACommsJSONCodec;
import com.dna.comms.entities.PoliticalGroupActivity;
import com.dna.comms.entities.PoliticalMandate;
import com.dna.comms.entities.PoliticalPartyActivity;
import com.dna.comms.entities.Politician;
import com.dna.comms.entities.ProsecutionComm;
import com.dna.comms.entities.ProsecutionCommTitle;
import com.dna.comms.entities.ProsecutionRuling;
import com.dna.comms.entities.ProsecutionRulingTitle;
import com.dna.comms.http.CDEPPoliticalProfileGrabber;
import com.dna.comms.http.DNAGrabber;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DNACommWorker {
	public static void main(String[] args) {
		try {
			buildRulingsJSON();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void exportPoliticiansToExcel() throws IOException, JsonParseException, JsonMappingException, FileNotFoundException {
		String json = StrangeUtils.getFileContent(new File("e:/USR/DNA & Imunitati/Parlament/parlamentari.json"));
		List<Politician> politicians = new ObjectMapper().readValue(json, new TypeReference<List<Politician>>() {
		});

		Collections.sort(politicians, new Comparator<Politician>() {
			@Override
			public int compare(Politician p1, Politician p2) {
				return p1.getName().compareTo(p2.getName());
			}
		});

		XSSFWorkbook book = new XSSFWorkbook();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", new Locale("RO_ro"));

		int row = 1;
		XSSFSheet mandatesSheet = book.createSheet("Mandate");
		xlsWrite(mandatesSheet, 0, 0, "Nume");
		xlsWrite(mandatesSheet, 0, 1, "Camera");
		xlsWrite(mandatesSheet, 0, 2, "Sesiune");
		xlsWrite(mandatesSheet, 0, 3, "Luari de cuvant");
		xlsWrite(mandatesSheet, 0, 4, "Declaratii");
		xlsWrite(mandatesSheet, 0, 5, "Legi propuse");
		xlsWrite(mandatesSheet, 0, 6, "Legi promulgate");
		xlsWrite(mandatesSheet, 0, 7, "Interpelari");
		xlsWrite(mandatesSheet, 0, 8, "Motiuni");
		xlsWrite(mandatesSheet, 0, 9, "Link");

		for (Politician p : politicians) {
			for (PoliticalMandate mandate : p.getMandates()) {
				xlsWrite(mandatesSheet, row, 0, p.getName());
				xlsWrite(mandatesSheet, row, 1, mandate.getChamber());
				xlsWrite(mandatesSheet, row, 2, mandate.getLegislativeSession());
				xlsWrite(mandatesSheet, row, 3, mandate.getSpeechCount());
				xlsWrite(mandatesSheet, row, 4, mandate.getDeclarationsCount());
				xlsWrite(mandatesSheet, row, 5, mandate.getLawsProposed());
				xlsWrite(mandatesSheet, row, 6, mandate.getLawsPassed());
				xlsWrite(mandatesSheet, row, 7, mandate.getQuestionings());
				xlsWrite(mandatesSheet, row, 8, mandate.getMotions());
				xlsWrite(mandatesSheet, row, 9, mandate.getLink());

				row++;
			}
		}

		row = 1;
		XSSFSheet partiesSheet = book.createSheet("Partide");
		xlsWrite(partiesSheet, 0, 0, "Nume");
		xlsWrite(partiesSheet, 0, 1, "Partid");
		xlsWrite(partiesSheet, 0, 2, "Din");
		xlsWrite(partiesSheet, 0, 3, "Pana");

		for (Politician p : politicians) {
			for (PoliticalPartyActivity party : p.getPoliticalParties()) {
				xlsWrite(partiesSheet, row, 0, p.getName());
				xlsWrite(partiesSheet, row, 1, party.getName());
				xlsWrite(partiesSheet, row, 2, sdf.format(party.getStart()));
				xlsWrite(partiesSheet, row, 3, sdf.format(party.getEnd()));

				row++;
			}
		}

		row = 1;
		XSSFSheet groupsSheet = book.createSheet("Grupuri parlamentare");
		xlsWrite(groupsSheet, 0, 0, "Nume");
		xlsWrite(groupsSheet, 0, 1, "Grup Parlamentar");
		xlsWrite(groupsSheet, 0, 2, "Din");
		xlsWrite(groupsSheet, 0, 3, "Pana");

		for (Politician p : politicians) {
			for (PoliticalGroupActivity group : p.getPoliticalGroups()) {
				xlsWrite(groupsSheet, row, 0, p.getName());
				xlsWrite(groupsSheet, row, 1, group.getName());
				xlsWrite(groupsSheet, row, 2, sdf.format(group.getStart()));
				xlsWrite(groupsSheet, row, 3, sdf.format(group.getEnd()));

				row++;
			}
		}

		book.write(new FileOutputStream("e:/USR/DNA & Imunitati/Parlament/parlamentari.xlsx"));
		book.close();
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

	public static void buildPoliticiansJSON() throws DNACommException, IOException, JsonGenerationException, JsonMappingException {
		String folder = "e:/USR/DNA & Imunitati/Parlament/";
		CDEPPoliticalProfileGrabber grabber = new CDEPPoliticalProfileGrabber();
		File inputFolder = new File(folder);

		for (File f : inputFolder.listFiles()) {
			if (f.isDirectory()) {
				for (String htmlFileName : f.list()) {
					// System.out.println("Processing " + htmlFileName);
					grabber.getPolitician(f.getAbsolutePath() + "/" + htmlFileName);
				}
			}
		}

		// order by date and merge parties & political groups
		for (Politician politician : StrangeUtils.politiciansByName.values()) {
			// order political belonging by date
			Collections.sort(politician.getPoliticalParties(), new Comparator<PoliticalPartyActivity>() {
				@Override
				public int compare(PoliticalPartyActivity o1, PoliticalPartyActivity o2) {
					return o1.getStart().compareTo(o2.getStart());
				}
			});

			// merge political parties
			for (int i = 0; i < politician.getPoliticalParties().size() - 1; i++) {
				PoliticalPartyActivity party1 = politician.getPoliticalParties().get(i);
				PoliticalPartyActivity party2 = politician.getPoliticalParties().get(i + 1);

				String name1 = party1.getName().toUpperCase().replaceAll("-", " ");
				String name2 = party2.getName().toUpperCase().replaceAll("-", " ");

				// same name & end of first=start of second
				if (name1.equals(name2) && party1.getEnd().equals(party2.getStart())) {
					party1.setEnd(party2.getEnd());
					politician.getPoliticalParties().remove(i + 1);
					i--;
				}
			}

			// order political groups by date
			Collections.sort(politician.getPoliticalGroups(), new Comparator<PoliticalGroupActivity>() {
				@Override
				public int compare(PoliticalGroupActivity o1, PoliticalGroupActivity o2) {
					return o1.getStart().compareTo(o2.getStart());
				}
			});

			// merge political groups
			for (int i = 0; i < politician.getPoliticalGroups().size() - 1; i++) {
				PoliticalGroupActivity group1 = politician.getPoliticalGroups().get(i);
				PoliticalGroupActivity group2 = politician.getPoliticalGroups().get(i + 1);

				String name1 = group1.getName().toUpperCase().replaceAll("-", " ");
				String name2 = group2.getName().toUpperCase().replaceAll("-", " ");

				// same name & end of first=start of second
				if (name1.equals(name2) && group1.getEnd().equals(group2.getStart())) {
					group1.setEnd(group2.getEnd());
					politician.getPoliticalGroups().remove(i + 1);
					i--;
				}
			}
		}

		PrintWriter pw = new PrintWriter(new FileWriter("e:/USR/DNA & Imunitati/Parlament/parlamentari.json"));
		DNACommsJSONCodec jsonCodec = new DNACommsJSONCodec();
		pw.write(jsonCodec.encodeAny(StrangeUtils.politiciansByName.values(), true));
		pw.close();
	}

	public static void fetchPoliticianFiles(String type) throws IOException, FileNotFoundException, MalformedURLException {
		String folder = "e:/USR/DNA & Imunitati/Parlament/";
		// read the excel spreadsheet
		XSSFWorkbook book = new XSSFWorkbook(new FileInputStream(folder + type + ".xlsx"));
		for (int sheetIndex = 0; sheetIndex < book.getNumberOfSheets(); sheetIndex++) {
			XSSFSheet sheet = book.getSheetAt(sheetIndex);

			String currentFolder = folder + type + " " + sheet.getSheetName();
			new File(currentFolder).mkdir();

			for (Row row : sheet) {
				if (row.getCell(1).getHyperlink() == null)
					continue;
				String url = row.getCell(1).getHyperlink().getAddress();
				String name = row.getCell(1).getStringCellValue();

				System.out.println("Fething html for " + name + " from " + url);
				String content = StrangeUtils.getHTTPContent(url, "ISO-8859-2");
				content = StrangeUtils.deAccent(content);

				// clean the HTML because it's broken (non-XML)
				HtmlCleaner cleaner = new HtmlCleaner();
				CleanerProperties props = cleaner.getProperties();
				TagNode node = cleaner.clean(content);
				String fileName = currentFolder + "/" + name + ".html";
				new PrettyXmlSerializer(props, "\t").writeToFile(node, fileName, "ISO-8859-2");
			}
		}
		book.close();
	}

	public static List<ProsecutionRulingTitle> fetchRulingTitles() throws DNACommException, IOException {
		// read titles from HAR archive
		List<ProsecutionRulingTitle> titles;
		DNACommsJSONCodec jsonCodec = new DNACommsJSONCodec();
		titles = jsonCodec.parseDNARulingTitlesFromHAR(new File("e:/USR/DNA & Imunitati/www.pna_rulings.ro.har"));

		// sort them by date
		titles.sort(new Comparator<ProsecutionRulingTitle>() {
			@Override
			public int compare(ProsecutionRulingTitle title0, ProsecutionRulingTitle title1) {
				return title0.getDate().compareTo(title1.getDate());
			}
		});
		// write them to a file, as JSONs
		PrintWriter jsonTitles = new PrintWriter(new BufferedWriter(new FileWriter("e:/USR/DNA & Imunitati/jsonRulingTitles.txt")));
		// PrintStream jsonTitles = System.out;
		jsonTitles.println(jsonCodec.encodeAny(titles, true));
		jsonTitles.close();

		return titles;
	}

	public static void fetchCommTitles() throws DNACommException, IOException {
		// read titles from HAR archive
		List<ProsecutionCommTitle> titles;
		DNACommsJSONCodec jsonCodec = new DNACommsJSONCodec();
		titles = jsonCodec.parseDNACommTitlesFromHAR(new File("e:/USR/DNA & Imunitati/www.pna_comm.ro.har"));

		// sort them by date
		titles.sort(new Comparator<ProsecutionCommTitle>() {
			@Override
			public int compare(ProsecutionCommTitle title0, ProsecutionCommTitle title1) {
				return title0.getDate().compareTo(title1.getDate());
			}
		});
		// write them to a file, as JSONs
		PrintWriter jsonTitles = new PrintWriter(new BufferedWriter(new FileWriter("./output/jsonTitles.txt")));
		// PrintStream jsonTitles = System.out;
		jsonTitles.println(jsonCodec.encodeAny(titles, true));
		jsonTitles.close();
	}

	public static void buildRulingsJSON() throws DNACommException, IOException {
		List<ProsecutionRulingTitle> titles;
		DNACommsJSONCodec jsonCodec = new DNACommsJSONCodec();
		titles = jsonCodec.parseDNARulingTitlesFromHAR(new File("e:/USR/DNA & Imunitati/www.pna_rulings.ro.har"));

		List<ProsecutionRuling> comms = new ArrayList<>();
		DNAGrabber grabber = new DNAGrabber();
		for (ProsecutionRulingTitle title : titles) {
			// if (title.getId()==7625)
			comms.add(grabber.getRulingByTitle(title));

			// if (title.getId() < 7620)
			// return;
		}

		PrintWriter commWriter = new PrintWriter(new BufferedWriter(new FileWriter("e:/USR/DNA & Imunitati/Output/rulings_raw.json")));
		commWriter.println(jsonCodec.encodeAny(comms, true));
		commWriter.close();
	}

	public static void buildCommsJSON() throws DNACommException, IOException {
		List<ProsecutionCommTitle> titles;
		DNACommsJSONCodec jsonCodec = new DNACommsJSONCodec();
		titles = jsonCodec.parseDNACommTitlesFromHAR(new File("e:/USR/DNA & Imunitati/www.pna_comm.ro.har"));

		List<ProsecutionComm> comms = new ArrayList<>();
		DNAGrabber grabber = new DNAGrabber();
		for (ProsecutionCommTitle title : titles)
			comms.add(grabber.getCommByTitle(title));
		PrintWriter commWriter = new PrintWriter(new BufferedWriter(new FileWriter("e:/USR/DNA & Imunitati/Output/comms_raw.json")));
		commWriter.println(jsonCodec.encodeAny(comms, true));
		commWriter.close();
	}

	public static void fetchDNARulingsHTML() throws DNACommException, IOException, MalformedURLException {
		List<ProsecutionRulingTitle> titles = fetchRulingTitles();

		for (ProsecutionRulingTitle title : titles) {
			String outputFile = "e:/USR/DNA & Imunitati/Condamnari DNA/condamnari_" + title.getId() + ".html";
			System.out.println("Proccessing " + outputFile);

			CleanerProperties props = new CleanerProperties();
			TagNode tagNode = new HtmlCleaner(props).clean(new URL(title.getLink()));
			new PrettyXmlSerializer(props).writeToFile(tagNode, outputFile, "utf-8");
		}
	}

}
