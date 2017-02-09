package com.dna.comms.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

public class ProsecutionRulingSentence {
	private String type;
	private String text;
	private String quantum;

	@JsonBackReference
	private RulingConvictLink rulingLink;

	@JsonManagedReference
	private ConvictedPerson convict;

	public RulingConvictLink getRulingLink() {
		return rulingLink;
	}

	public void setRulingLink(RulingConvictLink rulingLink) {
		this.rulingLink = rulingLink;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getQuantum() {
		return quantum;
	}

	public void setQuantum(String quantum) {
		this.quantum = quantum;
	}

	public ConvictedPerson getConvict() {
		return convict;
	}

	public void setConvict(ConvictedPerson convict) {
		this.convict = convict;
	}
}
