package com.dna.comms.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PoliticalMandate {
	@JsonProperty("Link")
	private String link;

	@JsonProperty("Camera")
	private String chamber;

	@JsonProperty("SesiuneLegislativ")
	private String legislativeSession;

	@JsonProperty("LuariDeCuvant")
	private int speechCount;

	@JsonProperty("Declaratii")
	private int declarationsCount;

	@JsonProperty("LegiPropuse")
	private int lawsProposed;

	@JsonProperty("LegiPromulgate")
	private int lawsPassed;

	@JsonProperty("Interpelari")
	private int questionings;

	@JsonProperty("Motiuni")
	private int motions;

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getChamber() {
		return chamber;
	}

	public void setChamber(String chamber) {
		this.chamber = chamber;
	}

	public String getLegislativeSession() {
		return legislativeSession;
	}

	public void setLegislativeSession(String legislativeSession) {
		this.legislativeSession = legislativeSession;
	}

	public int getSpeechCount() {
		return speechCount;
	}

	public void setSpeechCount(int speechCount) {
		this.speechCount = speechCount;
	}

	public int getDeclarationsCount() {
		return declarationsCount;
	}

	public void setDeclarationsCount(int declarationsCount) {
		this.declarationsCount = declarationsCount;
	}

	public int getLawsProposed() {
		return lawsProposed;
	}

	public void setLawsProposed(int lawsProposed) {
		this.lawsProposed = lawsProposed;
	}

	public int getLawsPassed() {
		return lawsPassed;
	}

	public void setLawsPassed(int lawsPassed) {
		this.lawsPassed = lawsPassed;
	}

	public int getQuestionings() {
		return questionings;
	}

	public void setQuestionings(int questionings) {
		this.questionings = questionings;
	}

	public int getMotions() {
		return motions;
	}

	public void setMotions(int motions) {
		this.motions = motions;
	}

}
