package com.dna.comms.http;

import com.dna.comms.DNACommException;
import com.dna.comms.StrangeUtils;
import com.dna.comms.entities.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DNAGrabber {
	private final String MONTHS_REGEX = "(ianuarie|februarie|martie|aprilie|mai|iunie|iulie|august|septembrie|octombrie|noiembrie|decembrie)";

	private final Pattern REGEX_COMM_NUMBER = Pattern.compile("(\\d+\\s?\\/?VIII\\/\\d+)");
	private final Pattern REGEX_COMM_NAME = Pattern.compile("([A-Z][A-Z\\s\\-]{5,}[A-Z])");
	private final Pattern REGEX_COMM_DATE = Pattern.compile("((\\d{1,2}\\s+)?" + MONTHS_REGEX + "\\s+\\d{4})");
	private final Pattern REGEX_COMM_MONEY_EUR = Pattern.compile("(\\d(\\d|\\.)+(,\\d*)?\\s+(euro|eur|EUR|EURO))");
	private final Pattern REGEX_COMM_MONEY_RON = Pattern.compile("(\\d(\\d|\\.)+(,\\d*)?\\s+(lei|ron|RON|LEI))");

	private final Pattern REGEX_RULING_NUMBER = Pattern.compile("(([\\d\\.])+(\\/.)?\\s*(din|\\/)\\s*(\\d{1,2}\\.\\d{1,2}\\.)?\\d{4})");
	private final Pattern REGEX_RULING_MONEY_RON = Pattern.compile("((\\d(\\d|\\.)+(,\\d*)?)\\s+(lei|ron|RON|LEI))");
	private final Pattern REGEX_RULING_MONEY_USD = Pattern.compile("((\\d(\\d|\\.)+(,\\d*)?)\\s+(dolari|usd|USD))");
	private final Pattern REGEX_RULING_MONEY_EUR = Pattern.compile("((\\d(\\d|\\.)+(,\\d*)?)\\s+(euro|eur|EUR|EURO))");

	private final Pattern REGEX_RULING_TIME = Pattern.compile(
			"(?:(?:(?:(?:\\d)+ (?:ani|an))|(?:(?:\\d)+ (?:luna|luni))|(?:(?:\\d)+ (?:zile|zi)))(?:(?:\\s?si\\s?)|(?:\\s?,?\\s))?)+");
	private final Pattern REGEX_RULING_NAMES = Pattern.compile("([\\(\\)\\.A-Z\\-]+[\\s,\\.]+){2,}");

	private static int suspectId = 1;
	private static int convictId = 1;

	public ProsecutionComm getCommByTitle(ProsecutionCommTitle title) throws DNACommException {

		ProsecutionComm result = new ProsecutionComm();
		result.setId(title.getId());
		result.setLink(title.getLink());
		result.setReleaseDate(title.getDate());
		result.setTitle(title.getTitle());
		result.setTitleUnicode(title.getTitleUnicode());

		try {
			File inputFile = new File("./project_files/Comunicate DNA/comm_" + title.getId() + ".html");
			String htmlContent = StrangeUtils.getFileContent(inputFile);

			htmlContent = cleanHTML(htmlContent);

			// htmlContent = StrangeUtils.deAccent(htmlContent);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(htmlContent.getBytes("utf-8"))));

			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList divNodes = (NodeList) xPath.evaluate("//div[@class='content-holder']/div[1]/div[2]", doc.getDocumentElement(),
					XPathConstants.NODESET);
			Element content = (Element) divNodes.item(0);

			// read 1 or 2 span elements, containing either a date, or a comm number
			NodeList spanNodes = content.getElementsByTagName("span");
			for (int i = 0; i < 2; i++) {
				Element span = (Element) spanNodes.item(i);
				if (span == null)
					continue;
				boolean parsed = false;

				try {
					Date d = readAsDate(span.getTextContent());
					// if successful, add as comm date
					result.setReleaseDate(d);
					parsed = true;
				} catch (DNACommException e) {
				}

				try {
					String number = readAsCommNumber(span.getTextContent());
					// if successful, add as comm number
					result.setNumber(number);
					parsed = true;
				} catch (DNACommException e) {
				}

				if (!parsed)
					System.err.println("Could not parse: " + span.getTextContent());
			}

			// extract names
			List<String> names = new ArrayList<>();
			NodeList boldNodes = content.getElementsByTagName("b");
			for (int i = 0; i < boldNodes.getLength(); i++) {
				Element bTag = (Element) boldNodes.item(i);
				String text = StrangeUtils.deAccent(bTag.getTextContent());

				String candidates[] = text.split(",");
				for (String candidate : candidates) {
					candidate = candidate.trim();
					names.addAll(getSuspectNames(candidate));
				}
			}
			for (String name : names) {
				CommSuspectLink csl = new CommSuspectLink();
				csl.setComm(result);
				csl.setFunctionAtTime("");
				csl.setPartyAtTime("");

				if (StrangeUtils.suspectsByName.containsKey(name)) {
					csl.setPerson(StrangeUtils.suspectsByName.get(name));
				} else {
					SuspectPerson person = new SuspectPerson(suspectId, name, "");
					person.getCommLinks().add(csl);

					suspectId++;
					StrangeUtils.suspectsByName.put(name, person);
					csl.setPerson(person);
				}
				result.getSuspects().add(csl);
			}

			// extract dates in the comm
			List<String> dates = new ArrayList<>();
			String textContentUnicode = content.getTextContent().trim();
			String textContent = StrangeUtils.deAccent(textContentUnicode);

			Matcher dateMatcher = REGEX_COMM_DATE.matcher(textContent);
			while (dateMatcher.find()) {
				if (!dates.contains(dateMatcher.group(1)))
					dates.add(dateMatcher.group(1));
			}
			Collections.sort(dates);
			result.setCommDates(dates);

			// extract values in the comm
			List<String> valuesRON = new ArrayList<>();
			Matcher ronMatcher = REGEX_COMM_MONEY_RON.matcher(textContent);
			while (ronMatcher.find()) {
				if (!valuesRON.contains(ronMatcher.group(1)))
					valuesRON.add(ronMatcher.group(1));
			}
			result.setValuesRON(valuesRON);

			List<String> valuesEUR = new ArrayList<>();
			Matcher eurMatcher = REGEX_COMM_MONEY_EUR.matcher(textContent);
			while (eurMatcher.find()) {
				if (!valuesEUR.contains(eurMatcher.group(1)))
					valuesEUR.add(eurMatcher.group(1));
			}
			result.setValuesEUR(valuesEUR);

			result.setContent(textContent);
			result.setContentUnicode(textContentUnicode);
		} catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException e) {
			throw new DNACommException("Exception raised while getting HTTP comm with id " + title.getId(), e);
		}

		return result;
	}

	public ProsecutionRuling getRulingByTitle(ProsecutionRulingTitle title) throws DNACommException {
		ProsecutionRuling result = new ProsecutionRuling();
		result.setId(title.getId());
		result.setLink(title.getLink());
		result.setTitle(title.getTitle());
		result.setReleaseDate(title.getDate());

		try {
			File inputFile = new File("./project_files/Condamnari DNA/condamnari_" + title.getId() + ".html");
			String htmlContent = StrangeUtils.getFileContent(inputFile);
			htmlContent = StrangeUtils.deAccent(cleanHTML(htmlContent));

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(htmlContent.getBytes("utf-8"))));

			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList divNodes = (NodeList) xPath.evaluate("//html/body/form/div[4]/div/div[2]", doc.getDocumentElement(),
					XPathConstants.NODESET);
			Element content = (Element) divNodes.item(0);

			// System.out.println(content.getTextContent());

			// first span contains the ruling number and court
			String firstSpan = content.getElementsByTagName("span").item(0).getTextContent();
			firstSpan = StrangeUtils.asDigitDate(firstSpan).toLowerCase();
			firstSpan = firstSpan.replace("din data de", "din");
			firstSpan = firstSpan.replaceAll("\\s+", " ");

			if (firstSpan.trim().startsWith("in cursul luni"))
				return null;
			if (getFirstMatch(REGEX_RULING_NUMBER, firstSpan) == null) {
				System.err.println("Comunicat ne-parsabil: " + title.getId());
				return null;
			}

			String rulingNumber = getFirstMatch(REGEX_RULING_NUMBER, firstSpan).replaceAll("\\s*din\\s*", "/");
			String rulingCourt = "Not found";
			if (firstSpan.contains("inalta curte de casatie si justitie") || firstSpan.contains("inaltei curti de casatie si justitie")) {
				rulingCourt = "Inalta Curte de Casatie si Justitie";
			} else if (firstSpan.contains("curtea de apel")) {
				int idx = firstSpan.indexOf("curtea de apel ") + "curtea de apel ".length();
				int endIdx = idx + firstSpan.substring(idx).indexOf(' ');
				rulingCourt = "Curtea de Apel " + firstSpan.substring(idx, endIdx).toUpperCase();
			} else if (firstSpan.contains("tribunalul")) {
				int idx = firstSpan.indexOf("tribunalul ") + "tribunalul ".length();
				int endIdx = idx + firstSpan.substring(idx).indexOf(' ');
				rulingCourt = "Tribunalul " + firstSpan.substring(idx, endIdx).toUpperCase();
			} else if (firstSpan.contains("judecatoria")) {
				int idx = firstSpan.indexOf("judecatoria ") + "judecatoria ".length();
				int endIdx = idx + firstSpan.substring(idx).indexOf(' ');
				rulingCourt = "Judecatoria " + firstSpan.substring(idx, endIdx).toUpperCase();
			} else if (firstSpan.contains("curtea militara de apel")) {
				rulingCourt = "Curtea Militara de Apel";
			} else
				System.err.println("Could not determine ruling court for " + firstSpan);

			System.out.println(result.getId());
			// System.out.println(rulingNumber + "|" + rulingCourt + "|");

			result.setRulingNumber(rulingNumber);
			result.setRulingCourt(rulingCourt);

			// extract the ruling date
			String rulingDateStr = rulingNumber.substring(rulingNumber.lastIndexOf("/") + 1);
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
				result.setRulingDate(sdf.parse(rulingDateStr));
			} catch (ParseException e) {
				System.err.println("Unparsable comm date: " + rulingDateStr);
			}

			// loop through the rest of the spans
			double prejudice = 0;
			result.setDNACommNumber("Not Found");
			NodeList spanNodes = (NodeList) xPath.evaluate("./span", content, XPathConstants.NODESET);
			for (int i = 0; i < spanNodes.getLength(); i++) {
				String text = spanNodes.item(i).getTextContent();
				String textLC = text.toLowerCase();

				if (textLC.contains("condamna") || textLC.contains("condamnat") || textLC.contains("inchisoare")) {
					result.getRulingLinks().add(parseConviction(spanNodes.item(i)));
				} else if (textLC.contains("obliga") || textLC.contains("dispune")) {
					prejudice += parseSums(REGEX_RULING_MONEY_RON, text);
					prejudice += parseSums(REGEX_RULING_MONEY_USD, text) * 4.1;
					prejudice += parseSums(REGEX_RULING_MONEY_EUR, text) * 4.5;
				} else if (getFirstMatch(REGEX_COMM_NUMBER, text) != null) {
					result.setDNACommNumber(getFirstMatch(REGEX_COMM_NUMBER, text));
				}
			}

			result.setPrejudice(new Double(prejudice).intValue() + " RON");
			// System.out.println(result.getPrejudice());

			return result;
		} catch (Exception e) {
			throw new DNACommException("Error reading ruling", e);
		}
	}

	private double parseSums(Pattern moneyPattern, String text) throws ParseException {
		Matcher matcher = moneyPattern.matcher(text);
		NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);

		double result = 0;
		while (matcher.find()) {
			Number number = format.parse(matcher.group(2));
			result += number.doubleValue();
		}

		return result;
	}

	private RulingConvictLink parseConviction(Node node) {
		RulingConvictLink result = new RulingConvictLink();
		List<ProsecutionRulingSentence> sentences = new ArrayList<>();
		String originalText = node.getTextContent().replace("\\s+", " ").replace(" ,", ",");
		String text = sanitizeTextToNumbers(originalText);
        String textlc = text.toLowerCase();

        if (textlc.contains("acordul de recunoastere a vinovatiei"))
			return null;
		if (text.replace(":", "").trim().toLowerCase().equals("Condamna pe inculpatii"))
			return null;

		List<String> names = extractNames(node);
		System.err.println(names + ": " + originalText);

		if (textlc.contains("interzi") && textlc.contains("drept")) {
			// System.err.println("Interzicearea unor drepturi: " + text);
			String subText = textlc.substring(textlc.indexOf("interzi"));
			addSentences(sentences, findFirstTime(subText), originalText, "Interzicerea unor drepturi", result, names);
		}

		if (textlc.contains("afis") && textlc.contains("hotara")) {
			// System.err.println("Afisarea hotararii: " + text);
			addSentences(sentences, "n/a", originalText, "Afisarea hotararii", result, names);
		}

		if (text.contains("zile amenda") || text.contains("zile-amenda")) {
			// System.err.println("Zile amenda: " + text);
			// TODO compute quantum
			addSentences(sentences, "n/a", originalText, "Zile amenda", result, names);
		}

		text = text.replaceAll("de\\s+", "").replace(" ,", ",");
		String time = findFirstTime(text);
		if (time != null) {
			int startIdx = findFirstTimeIndex(text);
			String rest = text.substring(startIdx + time.length()).toLowerCase().trim();
			if (rest.startsWith("inchisoare cu suspendare") || rest.startsWith("cu suspendare")) {
				// System.err.println("Inchisoare cu suspendare: " + text);
				addSentences(sentences, time, originalText, "Inchisoare cu suspendare", result, names);
			} else if (rest.startsWith("inchisoare") || rest.startsWith("in regim detentie") || rest.startsWith("pentru savarsirea")
					|| rest.startsWith("pentru infractiunea")) {
				// System.err.println("Inchisoare cu executare: " + text);
				addSentences(sentences, time, originalText, "Inchisoare cu executare", result, names);
			} else if (text.contains("munca neremunerata")) {
				// System.err.println("Munca neremunerata: " + text);
				addSentences(sentences, "n/a", originalText, "Munca neremunerata", result, names);
			} else {
				if (text.contains("amenz") || text.contains("amenda")) {
					// System.err.println("Amenda penala: " + text);
					// TODO quantum?
					addSentences(sentences, "n/a", originalText, "Amenda penala", result, names);
				}

				if (sentences.size() == 0) {
					System.out.println(text);
					System.err.println("Cannot read sentence: " + rest);
					return null;
				}
			}
		}

		return result;
	}

	private List<String> extractNames(Node node) {
		List<String> results = new ArrayList<>();
		NodeList boldTags = ((Element) node).getElementsByTagName("b");

		// look for bolded all-capitals text
		for (int i = 0; i < boldTags.getLength(); i++) {
			String text = boldTags.item(i).getTextContent().trim();
			text = text.replace("fosta ", "").replace("fost ", "");
			if (text.toUpperCase().equals(text)) {
				// separate by commas and "si"
				text = text.replace("si", ",");
				String names[] = text.split(",");
				for (String name : names)
					if (!results.contains(name.trim()))
						results.add(name.trim());
			}
		}

		// look for bolded non-capitals text, where each word starts with a capital letter
		for (int i = 0; i < boldTags.getLength(); i++) {
			// look for bolded all-capitals text
			String text = boldTags.item(i).getTextContent().trim();
			text = text.replace("fosta ", "").replace("fost ", "");

			// separate by commas and "si"
			text = text.replace("si", ",");
			String fullNames[] = text.split(",");
			boolean allStartWithCapitalLetter = true;
			for (String fullName : fullNames) {
				// each word of the name must start with capital letter
				String partNames[] = fullName.split(" ");
				for (String partName : partNames) {
					// note that some names will be in parenthesis (marriage/divorce names)
					partName = partName.trim().replace("(", "").replace(")", "");
					if (partName.isEmpty() || Character.isLowerCase(partName.charAt(0)))
						allStartWithCapitalLetter = false;
				}
			}

			if (allStartWithCapitalLetter) {
				for (String name : fullNames)
					if (!results.contains(name.trim().toUpperCase()))
						results.add(name.trim().toUpperCase());
			}
		}

		// find any company names
		for (int i = 0; i < boldTags.getLength(); i++) {
			String name = boldTags.item(i).getTextContent().trim();

			if (name.startsWith("S.C.") || name.startsWith("SC"))
				if (name.endsWith("S.R.L.") || name.endsWith("SRL") || name.endsWith("S.A.") || name.endsWith("SA")) {
					if (!results.contains(name.toUpperCase()))
						results.add(name.toUpperCase());
				}

		}

		// String text = node.getTextContent().replace("\\s+", " ");
		// text = text.replace("[\\s,\\.]SC[\\s,\\.]", " S.C. ").replace("[\\s,\\.]SA[\\s,\\.]", " S.A. ").replace("[\\s,\\.]SRL[\\s,\\.]",
		// " S.R.L. ");
		// int idx = text.indexOf("S.C.");
		// while (idx >= 0) {
		// String saName = getCompanyName(text, "S.C.", "S.A.");
		// String srlName = getCompanyName(text, "S.C.", "S.R.L.");
		// if (saName.length() == text.length() && srlName.length() == text.length()) {
		// System.err.println("Cannot read company name: " + text);
		// idx = -1;
		// } else {
		// String name = saName.length() < srlName.length() ? saName : srlName;
		// text = text.substring(text.indexOf(name) + name.length());
		//
		// name = name.trim().toUpperCase();
		// if (!results.contains(name))
		// results.add(name);
		// }
		// }

		// if nothing was found so far, look for all-caps groups of 2 or more words, that might be names
		if (results.isEmpty()) {
			String text = node.getTextContent();
			text = text.replace("fosta ", "").replace("fost ", "");

			Matcher matcher = REGEX_RULING_NAMES.matcher(text);
			while (matcher.find()) {
				String name = matcher.group(0);
				name = name.trim().toUpperCase();
				if (!results.contains(name))
					results.add(name);
			}
		}

		return results;
	}

	// private String getCompanyName(String text, String nameStart, String nameEnd) {
	// int startIdx = text.indexOf(nameStart);
	// if (startIdx < 0)
	// return text;
	//
	// int endIdx = text.indexOf(nameEnd);
	// if (endIdx < 0)
	// return text;
	//
	// return text.substring(startIdx, endIdx + nameEnd.length());
	// }

	private void addSentences(List<ProsecutionRulingSentence> sentences, String quantum, String originalText, String string,
			RulingConvictLink result, List<String> names) {
		for (String name : names) {
			ProsecutionRulingSentence sentence = new ProsecutionRulingSentence();
			sentence.setQuantum(quantum);
			sentence.setText(originalText);
			sentence.setType("Interzicerea unor drepturi");
			sentence.setRulingLink(result);

			if (!StrangeUtils.convictsByName.containsKey(name)) {
				ConvictedPerson convict = new ConvictedPerson();
				convict.setId(convictId++);
				convict.setName(name);
				StrangeUtils.convictsByName.put(name, convict);
			}
			StrangeUtils.convictsByName.get(name).getSentenceLinks().add(sentence);
			sentence.setConvict(StrangeUtils.convictsByName.get(name));

			sentences.add(sentence);
		}
	}

	private int findFirstTimeIndex(String text) {
		Matcher matcher = REGEX_RULING_TIME.matcher(text);
		if (matcher.find())
			return matcher.start(0);
		else
			return -1;
	}

	private String findFirstTime(String text) {
		Matcher matcher = REGEX_RULING_TIME.matcher(text);
		if (matcher.find())
			return matcher.group(0);
		else
			return null;
	}

	private Date readAsDate(String in) throws DNACommException {
		in = in.trim().toLowerCase();
		try {
			return StrangeUtils.convertRODateStringToDate(in);
		} catch (ParseException e) {
			try {
				return StrangeUtils.convertROMonthDateStringToDate(in);
			} catch (ParseException e1) {
				throw new DNACommException("Could not parse as date: " + in, e1);
			}
		}
	}

	private String readAsCommNumber(String in) throws DNACommException {
		Matcher matcher = REGEX_COMM_NUMBER.matcher(in.trim());
		if (matcher.find())
			return matcher.group(1);
		else
			throw new DNACommException("Could not parse as comm number: " + in);
	}

	private List<String> getSuspectNames(String candidateName) {
		List<String> names = new ArrayList<>();
		Matcher matcher = REGEX_COMM_NAME.matcher(candidateName.trim());
		if (matcher.find())
			for (int i = 1; i <= matcher.groupCount(); i++)
				names.add(matcher.group(i));

		return names;
	}

	private String getFirstMatch(Pattern pattern, String text) {
		Matcher matcher = pattern.matcher(text);
		if (matcher.find())
			return matcher.group(0);
		else
			return null;
	}

	private String cleanHTML(String htmlContent) {
		htmlContent = htmlContent.replace("&icirc;", "î");
		htmlContent = htmlContent.replace("&acirc;", "â");
		htmlContent = htmlContent.replace("&Icirc;", "Î");
		htmlContent = htmlContent.replace("&Acirc;", "Â");
		htmlContent = htmlContent.replace("&nbsp;", " ");
		htmlContent = htmlContent.replace("&deg;", "°");
		htmlContent = htmlContent.replace("&ouml;", "o");
		htmlContent = htmlContent.replace("&Ouml;", "O");
		htmlContent = htmlContent.replace("&oacute;", "o");
		htmlContent = htmlContent.replace("&Oacute;", "O");
		htmlContent = htmlContent.replace("&aacute;", "a");
		htmlContent = htmlContent.replace("&Aacute;", "A");
		htmlContent = htmlContent.replace("&iacute;", "i");
		htmlContent = htmlContent.replace("&Iacute;", "I");
		htmlContent = htmlContent.replace("&Uuml;", "U");
		htmlContent = htmlContent.replace("&uuml;", "u");
		htmlContent = htmlContent.replace("&atilde;", "a");
		htmlContent = htmlContent.replace("&laquo;", "«");
		htmlContent = htmlContent.replace("&raquo;", "»");
		htmlContent = htmlContent.replace("&ordm;", "");
		return htmlContent;
	}

	private String sanitizeTextToNumbers(String text) {
		text = text.replaceAll("\\s+", " ");
		text = text.replaceAll("([\\s\\.,])un([\\s\\.,])", " 1 ");
		text = text.replaceAll("([\\s\\.,])o([\\s\\.,])", " 1 ");
		text = text.replaceAll("([\\s\\.,])doi([\\s\\.,])", " 2 ");
		text = text.replaceAll("([\\s\\.,])doua([\\s\\.,])", " 2 ");
		text = text.replaceAll("([\\s\\.,])trei([\\s\\.,])", " 3 ");
		text = text.replaceAll("([\\s\\.,])patru([\\s\\.,])", " 4 ");
		text = text.replaceAll("([\\s\\.,])cinci([\\s\\.,])", " 5 ");
		text = text.replaceAll("([\\s\\.,])sase([\\s\\.,])", " 6 ");
		text = text.replaceAll("([\\s\\.,])sapte([\\s\\.,])", " 7 ");
		text = text.replaceAll("([\\s\\.,])opt([\\s\\.,])", " 8 ");
		text = text.replaceAll("([\\s\\.,])noua([\\s\\.,])", " 9 ");
		text = text.replaceAll("([\\s\\.,])zece([\\s\\.,])", " 10 ");
		text = text.replaceAll("([\\s\\.,])unsprezece([\\s\\.,])", " 11 ");
		text = text.replaceAll("([\\s\\.,])doisprezece([\\s\\.,])", " 12 ");

		return text;
	}
}
