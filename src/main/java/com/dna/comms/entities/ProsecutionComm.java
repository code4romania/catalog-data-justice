package com.dna.comms.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class ProsecutionComm {
	private int id;
	private String number;
	private String link;
	private Date releaseDate;
	private List<String> valuesEUR;
	private List<String> valuesRON;
	private List<String> commDates = new ArrayList<>();
	private String title;
	private String titleUnicode;
	private String content;
	private String contentUnicode;
	private String type;

	@OneToMany(mappedBy = "comm")
	private List<CommSuspectLink> suspects = new ArrayList<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitleUnicode() {
		return titleUnicode;
	}

	public void setTitleUnicode(String titleUnicode) {
		this.titleUnicode = titleUnicode;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentUnicode() {
		return contentUnicode;
	}

	public void setContentUnicode(String contentUnicode) {
		this.contentUnicode = contentUnicode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<CommSuspectLink> getSuspects() {
		return suspects;
	}

	public void setSuspects(List<CommSuspectLink> suspects) {
		this.suspects = suspects;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date commDate) {
		this.releaseDate = commDate;
	}

	public List<String> getValuesEUR() {
		return valuesEUR;
	}

	public void setValuesEUR(List<String> valuesEUR) {
		this.valuesEUR = valuesEUR;
	}

	public List<String> getValuesRON() {
		return valuesRON;
	}

	public void setValuesRON(List<String> valuesRON) {
		this.valuesRON = valuesRON;
	}

	public List<String> getCommDates() {
		return commDates;
	}

	public void setCommDates(List<String> commDates) {
		this.commDates = commDates;
	}
}
