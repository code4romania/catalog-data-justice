package com.dna.comms.entities;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PoliticalPartyActivity {
	@JsonProperty("NumePartid")
	private String name;

	@JsonProperty("DataInceputAfiliere")
	private Date start;

	@JsonProperty("DataSfarsitAfiliere")
	private Date end;

	public PoliticalPartyActivity() {
		super();
	}

	public PoliticalPartyActivity(String name, Date start, Date end) {
		super();
		this.name = name;
		this.start = start;
		this.end = end;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}
}
