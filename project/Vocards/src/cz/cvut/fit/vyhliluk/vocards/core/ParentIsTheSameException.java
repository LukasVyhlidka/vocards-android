package cz.cvut.fit.vyhliluk.vocards.core;

public class ParentIsTheSameException extends VocardsException {

	private static final long serialVersionUID = 1L;

	public ParentIsTheSameException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ParentIsTheSameException(String detailMessage) {
		super(detailMessage);
	}
	

}
