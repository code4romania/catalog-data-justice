package com.dna.comms.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Politician {
	@JsonProperty("Nume")
	private String name;

	@JsonProperty("DataNasterii")
	private Date dateOfBirth;

	@JsonProperty("Mandate")
	private List<PoliticalMandate> mandates = new ArrayList<>();

	@JsonProperty("Partide")
	private List<PoliticalPartyActivity> politicalParties = new ArrayList<>();

	@JsonProperty("Grupuri Parlamentare")
	private List<PoliticalGroupActivity> politicalGroups = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public List<PoliticalMandate> getMandates() {
		return mandates;
	}

	public void setMandates(List<PoliticalMandate> mandates) {
		this.mandates = mandates;
	}

	public List<PoliticalPartyActivity> getPoliticalParties() {
		return politicalParties;
	}

	public void setPoliticalParties(List<PoliticalPartyActivity> politicalParties) {
		this.politicalParties = politicalParties;
	}

	public List<PoliticalGroupActivity> getPoliticalGroups() {
		return politicalGroups;
	}

	public void setPoliticalGroups(List<PoliticalGroupActivity> politicalGroups) {
		this.politicalGroups = politicalGroups;
	}
}
