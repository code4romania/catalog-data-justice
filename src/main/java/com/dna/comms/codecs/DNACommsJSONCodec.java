package com.dna.comms.codecs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dna.comms.DNACommException;
import com.dna.comms.StrangeUtils;
import com.dna.comms.entities.ProsecutionCommTitle;
import com.dna.comms.entities.ProsecutionRulingTitle;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class DNACommsJSONCodec {
	public List<ProsecutionCommTitle> parseDNACommTitlesFromHAR(File input) throws DNACommException {
		List<ProsecutionCommTitle> result = new ArrayList<ProsecutionCommTitle>();

		try {
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(input));
			JSONArray entries = (JSONArray) ((JSONObject) jsonObject.get("log")).get("entries");

			for (Object entry : entries) {
				JSONObject jsonEntry = (JSONObject) entry;

				String text = ((JSONObject) ((JSONObject) jsonEntry.get("response")).get("content")).get("text").toString();

				int start = text.indexOf("![CDATA[") + 8;
				int end = text.indexOf("]]></update><update");
				String relevant = text.substring(start, end);

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(relevant.getBytes("utf-8"))));

				NodeList trList = doc.getElementsByTagName("tr");
				for (int i = 0; i < trList.getLength(); i++) {
					NodeList tdList = ((Element) trList.item(i)).getElementsByTagName("td");
					String link = ((Element) tdList.item(0).getChildNodes().item(0)).getAttribute("href");
					link = "http://www.pna.ro" + link;
					String dateStr = tdList.item(0).getTextContent();
					String titleUnicode = tdList.item(1).getTextContent();
					String title = StrangeUtils.deAccent(titleUnicode);

					int idStart = link.indexOf("=");
					int id = Integer.parseInt(link.substring(idStart + 1));
					Date date = StrangeUtils.convertRODateStringToDate(dateStr);

					// System.out.println(link + dateStr + title);

					ProsecutionCommTitle commTitle = new ProsecutionCommTitle(id, date, link, title, titleUnicode);
					result.add(commTitle);
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException | ParseException | java.text.ParseException e) {
			throw new DNACommException("Error reading comm titles from HAR file", e);
		}

		return result;
	}

	public List<ProsecutionRulingTitle> parseDNARulingTitlesFromHAR(File input) throws DNACommException {
		List<ProsecutionRulingTitle> result = new ArrayList<>();

		try {
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(input));
			JSONArray entries = (JSONArray) ((JSONObject) jsonObject.get("log")).get("entries");

			for (Object entry : entries) {
				JSONObject jsonEntry = (JSONObject) entry;

				String text = ((JSONObject) ((JSONObject) jsonEntry.get("response")).get("content")).get("text").toString();

				int start = text.indexOf("![CDATA[") + 8;
				int end = text.indexOf("]]></update><update");
				String relevant = text.substring(start, end);
				relevant = StrangeUtils.deAccent(relevant);

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(relevant.getBytes("utf-8"))));

				NodeList trList = doc.getElementsByTagName("tr");
				for (int i = 0; i < trList.getLength(); i++) {
					NodeList tdList = ((Element) trList.item(i)).getElementsByTagName("td");
					String link = ((Element) tdList.item(0).getChildNodes().item(0)).getAttribute("href");
					link = "http://www.pna.ro" + link;
					String dateStr = tdList.item(0).getTextContent();
					String title = tdList.item(1).getTextContent();

					int idStart = link.indexOf("=");
					int id = Integer.parseInt(link.substring(idStart + 1));
					Date date = StrangeUtils.convertRODateStringToDate(dateStr);

					// System.out.println(link + dateStr + title);

					ProsecutionRulingTitle commTitle = new ProsecutionRulingTitle(id, date, link, title);
					result.add(commTitle);
				}
			}
		} catch (Exception e) {
			throw new DNACommException("Error reading ruling titles from HAR file", e);
		}

		return result;
	}

	public String encodeAny(Object obj, boolean indent) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		if (indent)
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
		return mapper.writeValueAsString(obj);
	}

}
