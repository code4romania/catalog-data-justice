package com.dna.comms.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

public class ConvictedPerson {
	private int id;
	private String name;
	private String currentParty;

	@JsonBackReference
	private List<ProsecutionRulingSentence> sentenceLinks = new ArrayList<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCurrentParty() {
		return currentParty;
	}

	public void setCurrentParty(String currentParty) {
		this.currentParty = currentParty;
	}

	public List<ProsecutionRulingSentence> getSentenceLinks() {
		return sentenceLinks;
	}

	public void setSentenceLinks(List<ProsecutionRulingSentence> sentenceLinks) {
		this.sentenceLinks = sentenceLinks;
	}

}
