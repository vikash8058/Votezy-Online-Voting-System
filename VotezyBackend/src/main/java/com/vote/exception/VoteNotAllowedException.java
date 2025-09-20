package com.vote.exception;

public class VoteNotAllowedException extends RuntimeException{

	public VoteNotAllowedException(String message) {
		super(message);
	}
	
}
