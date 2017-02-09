package com.dna.comms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.dna.comms.entities.ConvictedPerson;
import com.dna.comms.entities.Politician;
import com.dna.comms.entities.SuspectPerson;

public class StrangeUtils {
	private static final Map<String, String> MONTHS_RO_TO_INT = new HashMap<>();
	public static final SimpleDateFormat DEFAULT_DATE_SDF = new SimpleDateFormat("dd.MM.yyyy");

	// FIXME this is a dirty fix, should use a repository instead
	public static Map<String, SuspectPerson> suspectsByName = new HashMap<>();
	public static Map<String, Politician> politiciansByName = new HashMap<>();
	public static Map<String, ConvictedPerson> convictsByName = new HashMap<>();

	static {
		MONTHS_RO_TO_INT.put("ianuarie", "01");
		MONTHS_RO_TO_INT.put("februarie", "02");
		MONTHS_RO_TO_INT.put("martie", "03");
		MONTHS_RO_TO_INT.put("aprilie", "04");
		MONTHS_RO_TO_INT.put("mai", "05");
		MONTHS_RO_TO_INT.put("iunie", "06");
		MONTHS_RO_TO_INT.put("iulie", "07");
		MONTHS_RO_TO_INT.put("august", "08");
		MONTHS_RO_TO_INT.put("septembrie", "09");
		MONTHS_RO_TO_INT.put("octombrie", "10");
		MONTHS_RO_TO_INT.put("noiembrie", "11");
		MONTHS_RO_TO_INT.put("decembrie", "12");
		MONTHS_RO_TO_INT.put(" ", ".");
	}

	public static String deAccent(String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

	public static String asDigitDate(String input) {
		for (String key : MONTHS_RO_TO_INT.keySet()) {
			if (key.trim().isEmpty())
				continue;
			while (input.contains(" " + key + " "))
				input = input.replace(" " + key + " ", "." + MONTHS_RO_TO_INT.get(key) + ".");
		}
		return input;
	}

	public static Date convertROMonthDateStringToDate(String roDateStr) throws java.text.ParseException {
		for (String key : MONTHS_RO_TO_INT.keySet()) {
			roDateStr = roDateStr.replace(key, MONTHS_RO_TO_INT.get(key));
			if (key.length() > 2)
				roDateStr = roDateStr.replace(key.substring(0, 3), MONTHS_RO_TO_INT.get(key));
		}

		return DEFAULT_DATE_SDF.parse("15." + roDateStr);
	}

	public static Date convertRODateStringToDate(String roDateStr) throws java.text.ParseException {
		for (String key : MONTHS_RO_TO_INT.keySet()) {
			roDateStr = roDateStr.replace(key, MONTHS_RO_TO_INT.get(key));
			if (key.length() > 2)
				roDateStr = roDateStr.replace(key.substring(0, 3), MONTHS_RO_TO_INT.get(key));
		}

		return DEFAULT_DATE_SDF.parse(roDateStr);
	}

	public static String getHTTPContent(String url) throws MalformedURLException, IOException {
		return getHTTPContent(url, "UTF-8");
	}

	public static String getHTTPContent(String url, String encoding) throws MalformedURLException, IOException {
		URLConnection connection = new URL(url).openConnection();
		connection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		connection.connect();

		BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName(encoding)));

		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			sb.append(line);
		}

		r.close();
		return sb.toString();
	}

	public static String getFileContent(File input) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(input)));

		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			sb.append(line);
		}

		r.close();
		return sb.toString();
	}
}
