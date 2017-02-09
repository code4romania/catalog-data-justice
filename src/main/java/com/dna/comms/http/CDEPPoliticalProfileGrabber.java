package com.dna.comms.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dna.comms.DNACommException;
import com.dna.comms.StrangeUtils;
import com.dna.comms.entities.PoliticalGroupActivity;
import com.dna.comms.entities.PoliticalMandate;
import com.dna.comms.entities.PoliticalPartyActivity;
import com.dna.comms.entities.Politician;

public class CDEPPoliticalProfileGrabber {
	private static final Map<String, String> LEGISLATIVE_SESSIONS_STARTS = new HashMap<>();
	private static final Map<String, String> LEGISLATIVE_SESSIONS_ENDS = new HashMap<>();

	static {
		LEGISLATIVE_SESSIONS_STARTS.put("1990-1992", "mai 1990");
		LEGISLATIVE_SESSIONS_STARTS.put("1992-1996", "sep 1992");
		LEGISLATIVE_SESSIONS_STARTS.put("1996-2000", "noi 1996");
		LEGISLATIVE_SESSIONS_STARTS.put("2000-2004", "noi 2000");
		LEGISLATIVE_SESSIONS_STARTS.put("2004-2008", "noi 2004");
		LEGISLATIVE_SESSIONS_STARTS.put("2008-2012", "noi 2008");
		LEGISLATIVE_SESSIONS_STARTS.put("2012-prezent", "dec 2012");

		LEGISLATIVE_SESSIONS_ENDS.put("1990-1992", "sep 1992");
		LEGISLATIVE_SESSIONS_ENDS.put("1992-1996", "noi 1996");
		LEGISLATIVE_SESSIONS_ENDS.put("1996-2000", "noi 2000");
		LEGISLATIVE_SESSIONS_ENDS.put("2000-2004", "noi 2004");
		LEGISLATIVE_SESSIONS_ENDS.put("2004-2008", "noi 2008");
		LEGISLATIVE_SESSIONS_ENDS.put("2008-2012", "dec 2012");
		LEGISLATIVE_SESSIONS_ENDS.put("2012-prezent", "dec 2016");
	}

	public Politician getPolitician(String inputFile) throws DNACommException {
		try {
			// get the content
			String htmlContent = StrangeUtils.getFileContent(new File(inputFile));

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(htmlContent.getBytes("ISO-8859-2"))));

			// get his name
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nameTD = (NodeList) xPath.evaluate("/html/body/table/tbody/tr/td[2]/table[2]/tbody/tr/td[2]/table/tbody/tr[2]/td",
					doc.getDocumentElement(), XPathConstants.NODESET);
			String nameAndDOB = ((Element) nameTD.item(0)).getTextContent();

			while (StringUtils.countMatches(nameAndDOB, ".") > 2)
				nameAndDOB = nameAndDOB.replaceFirst("\\.", "");

			// get the link
			NodeList linkTagNodes = (NodeList) xPath.evaluate(
					"/html/body/table/tbody/tr/td[2]/table/tbody/tr[2]/td[2]/table/tbody/tr/td/div/a[3]", doc.getDocumentElement(),
					XPathConstants.NODESET);
			Element linkEl = (Element) linkTagNodes.item(0);
			int linkIdx = linkEl.getAttribute("href").lastIndexOf("&idl");
			String link = "http://www.cdep.ro" + linkEl.getAttribute("href").substring(0, linkIdx);

			int nDotIdx = nameAndDOB.indexOf("n.");
			String name, dob = "";
			if (nDotIdx > 0) {
				name = nameAndDOB.substring(0, nDotIdx).trim();
				dob = nameAndDOB.substring(nDotIdx + 2).trim().replace(".", "");
			} else {
				name = nameAndDOB.trim();
			}
			Politician result;

			// search if this guy already exists, create him otherwise
			if (StrangeUtils.politiciansByName.containsKey(name))
				result = StrangeUtils.politiciansByName.get(name);
			else {
				result = new Politician();
				StrangeUtils.politiciansByName.put(name, result);
				result.setName(name);

				// add date of birth
				if (dob.isEmpty())
					result.setDateOfBirth(null);
				else
					result.setDateOfBirth(StrangeUtils.convertRODateStringToDate(dob));
			}

			// get legislature
			NodeList titleNodes = (NodeList) xPath.evaluate("/html/head/title", doc.getDocumentElement(), XPathConstants.NODESET);
			Element titleEl = (Element) titleNodes.item(0);
			String legislativeSession = titleEl.getTextContent().trim().substring(33);

			NodeList contentTableNodes = (NodeList) xPath.evaluate("/html/body/table/tbody/tr/td[2]/table[2]/tbody/tr/td[3]/table[3]",
					doc.getDocumentElement(), XPathConstants.NODESET);
			Element contentTable = (Element) contentTableNodes.item(0);

			// add mandates
			PoliticalMandate mandate = new PoliticalMandate();
			mandate.setLegislativeSession(legislativeSession);
			mandate.setLink(link);
			if (inputFile.contains("Senat"))
				mandate.setChamber("Senat");
			else
				mandate.setChamber("CD");
			result.getMandates().add(mandate);

			NodeList mandateTableNodes = (NodeList) xPath.evaluate("./tbody/tr/td/table", contentTable, XPathConstants.NODESET);
			for (int i = 0; i < mandateTableNodes.getLength(); i++) {
				Element el = (Element) mandateTableNodes.item(i);
				String str = el.getTextContent().trim().replaceAll("\\s+", " ");

				// System.out.println(str);

				if (str.startsWith("Formatiunea politica") || str.startsWith("Organizatia minoritatilor nationale")) {
					// add political parties
					result.getPoliticalParties().addAll(parsePoliticalParties(el, legislativeSession));
				} else if (str.startsWith("Grupul parlamentar")) {
					// add political groups
					result.getPoliticalGroups().addAll(parseGroupActivities(el, legislativeSession));
				} else if (str.startsWith("Activitatea parlamentara in cifre")) {
					// fill in mandate activity
					parseMandateActivity(el, mandate);
				}
			}

			return result;
		} catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException | ParseException e) {
			throw new DNACommException("", e);
		}
	}

	private List<PoliticalGroupActivity> parseGroupActivities(Element el, String legislativeSession)
			throws XPathExpressionException, ParseException {
		List<PoliticalGroupActivity> result = new ArrayList<>();

		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList rowElements = (NodeList) xPath.evaluate("./tbody/tr[2]/td[2]/table/tbody/tr", el, XPathConstants.NODESET);

		String name = "Group name not found";
		for (int i = 0; i < rowElements.getLength(); i++) {
			Element rowEl = (Element) rowElements.item(i);

			String start = "", end = "";
			NodeList tdNodes = (NodeList) xPath.evaluate("./td", rowEl, XPathConstants.NODESET);
			for (int j = 0; j < tdNodes.getLength(); j++) {
				String text = ((Element) tdNodes.item(j)).getTextContent().trim();

				if (text.contains("Grupul parlamentar"))
					name = text;
				if (text.contains(" din ") && start.isEmpty())
					start = getStartDateStr(text, legislativeSession);
				if (text.contains(" pana in ") && end.isEmpty())
					end = getEndDateStr(text, legislativeSession);
			}

			if (start.isEmpty())
				start = LEGISLATIVE_SESSIONS_STARTS.get(legislativeSession);
			if (end.isEmpty())
				end = LEGISLATIVE_SESSIONS_ENDS.get(legislativeSession);

			PoliticalGroupActivity activity = new PoliticalGroupActivity();
			activity.setName(name);
			activity.setStart(StrangeUtils.convertROMonthDateStringToDate(start));
			activity.setEnd(StrangeUtils.convertROMonthDateStringToDate(end));

			result.add(activity);
		}

		return result;
	}

	private List<PoliticalPartyActivity> parsePoliticalParties(Element el, String legislativeSession)
			throws XPathExpressionException, ParseException {
		List<PoliticalPartyActivity> result = new ArrayList<>();

		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList rowElements = (NodeList) xPath.evaluate("./tbody/tr[2]/td[2]/table/tbody/tr", el, XPathConstants.NODESET);

		for (int i = 0; i < rowElements.getLength(); i++) {
			Element rowEl = (Element) rowElements.item(i);
			String rest = rowEl.getTextContent().trim();

			NodeList nameTD = (NodeList) xPath.evaluate("./td[2]/table/tbody/tr/td[1]", rowEl, XPathConstants.NODESET);
			if (nameTD.item(0) == null) {
				System.err.println("Could not parse political party for " + rest);
				continue;
			}
			String name = ((Element) nameTD.item(0)).getTextContent().trim();

			String start = getStartDateStr(rest, legislativeSession);
			String end = getEndDateStr(rest, legislativeSession);

			if (start.isEmpty())
				start = LEGISLATIVE_SESSIONS_STARTS.get(legislativeSession);
			if (end.isEmpty())
				end = LEGISLATIVE_SESSIONS_ENDS.get(legislativeSession);

			PoliticalPartyActivity activity = new PoliticalPartyActivity();
			activity.setName(name);
			activity.setStart(StrangeUtils.convertROMonthDateStringToDate(start));
			activity.setEnd(StrangeUtils.convertROMonthDateStringToDate(end));

			result.add(activity);
		}

		return result;
	}

	private void parseMandateActivity(Element el, PoliticalMandate mandate) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList rowElements = (NodeList) xPath.evaluate("./tbody/tr[2]/td[2]/table/tbody/tr", el, XPathConstants.NODESET);

		for (int i = 0; i < rowElements.getLength(); i++) {
			Element trEl = (Element) rowElements.item(i);
			if (!trEl.hasChildNodes())
				continue;
			String itemName = trEl.getElementsByTagName("td").item(0).getTextContent();

			NodeList mainNrEl = (NodeList) xPath.evaluate("./td[2]/a/b", trEl, XPathConstants.NODESET);
			int nr = Integer.parseInt(mainNrEl.item(0).getTextContent().trim());
			NodeList otherTextEl = (NodeList) xPath.evaluate("./td[2]", trEl, XPathConstants.NODESET);
			String otherText = otherTextEl.item(0).getTextContent().trim().replace("\\s+", " ");

			if (itemName.contains("Luari de cuvant")) {
				mandate.setSpeechCount(nr);
			} else if (itemName.contains("Declaratii politice")) {
				mandate.setDeclarationsCount(nr);
			} else if (itemName.contains("Propuneri legislative initiate")) {
				mandate.setLawsProposed(nr);
				int idx = otherText.indexOf("din care");
				if (idx < 0)
					mandate.setLawsPassed(0);
				else {
					idx += 9;
					int idxSapce = otherText.substring(idx).indexOf(" ") + idx;
					mandate.setLawsPassed(Integer.parseInt(otherText.substring(idx, idxSapce)));
				}
			} else if (itemName.contains("Intrebari si interpelari")) {
				mandate.setQuestionings(nr);
			} else if (itemName.contains("Motiuni")) {
				mandate.setMotions(nr);
			} else
				System.err.println("Unmatched activity: " + itemName);
		}
	}

	private String getStartDateStr(String input, String legislativeSession) {
		String dates = input.trim().replaceAll("\\s+", " ").replaceAll("\\.", "");

		int fromIdx = dates.indexOf("- din ");
		if (fromIdx == -1)
			return LEGISLATIVE_SESSIONS_STARTS.get(legislativeSession);
		else
			return dates.substring(fromIdx + 6, fromIdx + 14);
	}

	private String getEndDateStr(String input, String legislativeSession) {
		String dates = input.trim().replaceAll("\\s+", " ").replaceAll("\\.", "");

		int toIdx = dates.indexOf("- pana in ");
		if (toIdx == -1) {
			return LEGISLATIVE_SESSIONS_ENDS.get(legislativeSession);
		} else {
			return dates.substring(toIdx + 10, toIdx + 18);
		}
	}
}
