package br.ce.wcaquino.matchers;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import br.ce.wcaquino.utils.DataUtils;

public class DataMatcher extends TypeSafeMatcher<Date> {
	
	private Integer diasAdicionais;
	
	public DataMatcher(Integer diasAdicionais) {
		this.diasAdicionais = diasAdicionais;
	}

	public void describeTo(Description description) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, diasAdicionais);
		String dataExtenso =  calendar.getDisplayName(Calendar.DAY_OF_MONTH, Calendar.LONG, new Locale("pt","BR"));
		description.appendText(dataExtenso);

	}

	@Override
	protected boolean matchesSafely(Date dataAtual) {
		return DataUtils.isMesmaData(dataAtual, DataUtils.obterDataComDiferencaDias(diasAdicionais));
	}

}
