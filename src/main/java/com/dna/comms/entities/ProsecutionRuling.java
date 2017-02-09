package com.dna.comms.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProsecutionRuling {
	private int id;
	private String rulingNumber;
	private String DNACommNumber;
	private String link;
	private String title;
	private Date releaseDate;
	private Date rulingDate;
	private String rulingCourt;
	private String prejudice;

	private List<RulingConvictLink> rulingLinks = new ArrayList<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public Date getRulingDate() {
		return rulingDate;
	}

	public void setRulingDate(Date rulingDate) {
		this.rulingDate = rulingDate;
	}

	public String getRulingCourt() {
		return rulingCourt;
	}

	public void setRulingCourt(String rulingCourt) {
		this.rulingCourt = rulingCourt;
	}

	public String getPrejudice() {
		return prejudice;
	}

	public void setPrejudice(String prejudice) {
		this.prejudice = prejudice;
	}

	public List<RulingConvictLink> getRulingLinks() {
		return rulingLinks;
	}

	public void setRulingLinks(List<RulingConvictLink> rulingLinks) {
		this.rulingLinks = rulingLinks;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRulingNumber() {
		return rulingNumber;
	}

	public void setRulingNumber(String rulingNumber) {
		this.rulingNumber = rulingNumber;
	}

	public String getDNACommNumber() {
		return DNACommNumber;
	}

	public void setDNACommNumber(String dNACommNumber) {
		DNACommNumber = dNACommNumber;
	}

}
