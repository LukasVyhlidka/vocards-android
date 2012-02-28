package cz.cvut.fit.vyhliluk.vocards.core;

public class Dictionary {

	private Long id;
	
	private String name;
	
	private Language nativeLang;
	
	private Language foreignLang;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Language getNativeLang() {
		return nativeLang;
	}

	public void setNativeLang(Language nativeLang) {
		this.nativeLang = nativeLang;
	}

	public Language getForeignLang() {
		return foreignLang;
	}

	public void setForeignLang(Language foreignLang) {
		this.foreignLang = foreignLang;
	}
	
}
