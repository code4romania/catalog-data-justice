package com.dna.comms.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

public class SuspectPerson {
	private int id;
	private String name;
	private String currentParty;

	@JsonBackReference
	private List<CommSuspectLink> commLinks;

	public SuspectPerson() {
	}

	public SuspectPerson(int id, String name, String currentParty) {
		this.id = id;
		this.name = name;
		this.currentParty = currentParty;
		this.commLinks = new ArrayList<CommSuspectLink>();
	}

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

	public List<CommSuspectLink> getCommLinks() {
		return commLinks;
	}

	public void setCommLinks(List<CommSuspectLink> commLink) {
		this.commLinks = commLink;
	}

}
