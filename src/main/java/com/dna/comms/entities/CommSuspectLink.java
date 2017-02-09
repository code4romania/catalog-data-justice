package com.dna.comms.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "CommSuspectLink")
public class CommSuspectLink {
	@Column(name = "partyAtTime")
	private String partyAtTime;

	@Column(name = "functionAtTime")
	private String functionAtTime;

	@JsonBackReference
	@ManyToOne
	private ProsecutionComm comm;

	@OneToOne
	private SuspectPerson person;

	public ProsecutionComm getComm() {
		return comm;
	}

	public void setComm(ProsecutionComm comm) {
		this.comm = comm;
	}

	public SuspectPerson getPerson() {
		return person;
	}

	public void setPerson(SuspectPerson person) {
		this.person = person;
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

}
