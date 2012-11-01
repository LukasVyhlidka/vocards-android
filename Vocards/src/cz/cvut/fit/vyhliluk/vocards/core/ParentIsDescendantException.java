package cz.cvut.fit.vyhliluk.vocards.core;

public class ParentIsDescendantException extends VocardsException {

	private static final long serialVersionUID = 1L;

	public ParentIsDescendantException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ParentIsDescendantException(String detailMessage) {
		super(detailMessage);
	}

}
