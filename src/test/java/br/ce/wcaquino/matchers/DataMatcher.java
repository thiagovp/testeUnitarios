package br.ce.wcaquino.matchers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import br.ce.wcaquino.utils.DataUtils;

public class DataMatcher extends TypeSafeMatcher<Date> {
	
	private Integer diasAdicionais;
	
	public DataMatcher(Integer diasAdicionais) {
		this.diasAdicionais = diasAdicionais;
	}

	public void describeTo(Description description) {
		Date dataEsperada = DataUtils.obterDataComDiferencaDias(diasAdicionais);
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		description.appendText(format.format(dataEsperada));

	}

	@Override
	protected boolean matchesSafely(Date dataAtual) {
		return DataUtils.isMesmaData(dataAtual, DataUtils.obterDataComDiferencaDias(diasAdicionais));
	}

}
