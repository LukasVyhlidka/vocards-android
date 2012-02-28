package cz.cvut.fit.vyhliluk.vocards.core;

public class Card {
	
	private Long id;
	
	private Integer factor;
	
	private Word nativeWord;
	
	private Word foreignWord;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getFactor() {
		return factor;
	}

	public void setFactor(Integer factor) {
		this.factor = factor;
	}

	public Word getNativeWord() {
		return nativeWord;
	}

	public void setNativeWord(Word nativeWord) {
		this.nativeWord = nativeWord;
	}

	public Word getForeignWord() {
		return foreignWord;
	}

	public void setForeignWord(Word foreignWord) {
		this.foreignWord = foreignWord;
	}

}
