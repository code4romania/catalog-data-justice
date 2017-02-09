package com.dna.comms;

public class DNACommException extends Exception {
	private static final long serialVersionUID = 1L;

	public DNACommException(String message) {
		super(message);
	}

	public DNACommException(String message, Exception e) {
		super(message, e);
	}
}
