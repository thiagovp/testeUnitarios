package br.ce.wcaquino.matchers;

import java.util.Calendar;
import java.util.Date;

import org.hamcrest.Matcher;

public class MatchersProprios {
	
	public static DiaSemanaMatcher caiEm(Integer diaSemana) {
		return new DiaSemanaMatcher(diaSemana);
	}
	
	public static DiaSemanaMatcher caiNumaSegunda() {
		return new DiaSemanaMatcher(Calendar.MONDAY);
	}

	public static Matcher<Date> ehHoje() {
		return new DataMatcher(0);
	}

	public static Matcher<Date> ehHojeComDiferencaDeDias(Integer diasAdicionais) {
		return new DataMatcher(diasAdicionais);
	}

}
