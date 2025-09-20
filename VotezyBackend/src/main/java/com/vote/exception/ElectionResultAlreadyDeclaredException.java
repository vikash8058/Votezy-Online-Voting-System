package com.vote.exception;

public class ElectionResultAlreadyDeclaredException extends RuntimeException {
	public ElectionResultAlreadyDeclaredException(String messege) {
		super(messege);
	}
}
