package com.dna.comms.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

public class RulingConvictLink {
	@JsonBackReference
	private ProsecutionRuling ruling;

	@JsonManagedReference
	private List<ProsecutionRulingSentence> sentences = new ArrayList<>();

	private String partyAtTime;
	private String functionAtTime;

	public ProsecutionRuling getRuling() {
		return ruling;
	}

	public void setRuling(ProsecutionRuling ruling) {
		this.ruling = ruling;
	}

	public String getPartyAtTime() {
		return partyAtTime;
	}

	public void setPartyAtTime(String partyAtTime) {
		this.partyAtTime = partyAtTime;
	}

	public String getFunctionAtTime() {
		return functionAtTime;
	}

	public void setFunctionAtTime(String functionAtTime) {
		this.functionAtTime = functionAtTime;
	}

	public List<ProsecutionRulingSentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<ProsecutionRulingSentence> sentences) {
		this.sentences = sentences;
	}

}
