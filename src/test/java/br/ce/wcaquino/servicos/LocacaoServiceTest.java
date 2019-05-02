package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.FilmeBuilder.umFilmeSemEstoque;
import static br.ce.wcaquino.builders.LocacaoBuilder.umLocacao;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHojeComDiferencaDeDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ LocacaoService.class })
public class LocacaoServiceTest {

	@InjectMocks
	private LocacaoService service;

	@Mock
	private SPCService spc;
	@Mock
	private LocacaoDAO dao;
	@Mock
	private EmailService emailService;

	@Rule
	public ErrorCollector error = new ErrorCollector();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		service = PowerMockito.spy(service);

	}

	@Test
	public void deveAlugarFilme() throws Exception {
		// cenario
		Usuario usuario = umUsuario().agora();
		Filme filme = umFilme().comValor(5.0).agora();
		List<Filme> filmes = new ArrayList<Filme>();
		filmes.add(filme);

//		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(3,5,2019));
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 3);
		calendar.set(Calendar.MONTH, Calendar.MAY);
		calendar.set(Calendar.YEAR, 2019);

		PowerMockito.mockStatic(Calendar.class);
		PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);

		// acao
		Locacao locacao = service.alugarFilme(usuario, filmes);

		// verificacao
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
//		error.checkThat(locacao.getDataLocacao(), ehHoje());
//		error.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDeDias(1));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(3, 5, 2019)), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(4, 5, 2019)), is(true));

	}

	@Test(expected = FilmeSemEstoqueException.class)
	public void naoDeveAlugarFilmeSemEstoque() throws Exception {
		// cenario
		Usuario usuario = umUsuario().agora();
		Filme filme = umFilmeSemEstoque().agora();
		List<Filme> filmes = new ArrayList<Filme>();
		filmes.add(filme);

		// acao
		service.alugarFilme(usuario, filmes);
	}

	@Test
	public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
		// cenario
		Filme filme = umFilme().agora();
		List<Filme> filmes = new ArrayList<Filme>();
		filmes.add(filme);
		// acao
		try {
			service.alugarFilme(null, filmes);
			Assert.fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuário vazio"));
		}
	}

	@Test
	public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException {
		// cenario
		Usuario usuario = umUsuario().agora();
		exception.expect(LocadoraException.class);
		exception.expectMessage("Filme vazio");
		// acao
		service.alugarFilme(usuario, null);
	}

	@Test
	public void deveDevolverNaSegundaAoAlugarNosSabados() throws Exception {

		// cenario
		Usuario usuario = umUsuario().agora();
		Filme filme = umFilme().agora();
		List<Filme> filmes = new ArrayList<Filme>();
		filmes.add(filme);

//		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(4,5,2019));

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 4);
		calendar.set(Calendar.MONTH, Calendar.MAY);
		calendar.set(Calendar.YEAR, 2019);

		PowerMockito.mockStatic(Calendar.class);
		PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);

		// acao
		Locacao locacao = service.alugarFilme(usuario, filmes);

		// verificacao

		boolean ehSegunda = DataUtils.verificarDiaSemana(locacao.getDataRetorno(), Calendar.MONDAY);
		Assert.assertTrue(ehSegunda);
		assertThat(locacao.getDataRetorno(), caiNumaSegunda());

//		PowerMockito.verifyNew(Date.class, Mockito.times(2)).withNoArguments();

		PowerMockito.verifyStatic(Mockito.times(2));
		Calendar.getInstance();
	}

	@Test
	public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception {
		// cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(umFilme().agora());
		when(spc.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);

		// acao
		try {
			service.alugarFilme(usuario, filmes);
			// verificacao
			Assert.fail();
		} catch (LocadoraException e) {
			Assert.assertThat(e.getMessage(), is("Usuário Negativado"));
		}

		verify(spc).possuiNegativacao(usuario);

	}

	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas() {
		// cenario
		Usuario usuario = umUsuario().agora();
		Usuario usuario2 = umUsuario().comNome("Usuario em dia").agora();
		Usuario usuario3 = umUsuario().comNome("Outro atrasado").agora();

		List<Locacao> locacoes = Arrays.asList(umLocacao().atrasado().comUsuario(usuario).agora(),
				umLocacao().comUsuario(usuario2).agora(), umLocacao().atrasado().comUsuario(usuario3).agora(),
				umLocacao().atrasado().comUsuario(usuario3).agora());
		Mockito.when(dao.obterLocacoesPendentes()).thenReturn(locacoes);
		// acao
		service.notificarAtrasos();

		// verificacao
		verify(emailService, Mockito.times(3)).notificarAtrasos(Mockito.any(Usuario.class));
		verify(emailService).notificarAtrasos(usuario);
		verify(emailService, Mockito.atLeastOnce()).notificarAtrasos(usuario3);
		verify(emailService, never()).notificarAtrasos(usuario2);
		verifyNoMoreInteractions(emailService);
	}

	@Test
	public void deveTratarErronoSPC() throws Exception {
		// cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(umFilme().agora());

		when(spc.possuiNegativacao(usuario)).thenThrow(new Exception("Falha catastrófica"));

		exception.expect(LocadoraException.class);
		exception.expectMessage("Problemas com SPC, tente novamente");

		// acao
		service.alugarFilme(usuario, filmes);

		// verificacao

	}

	@Test
	public void deveProrrogarUmaLocacao() {
		// cenario
		Locacao locacao = umLocacao().agora();

		// acao
		service.prorrogarLocacao(locacao, 3);

		// verificacao
		ArgumentCaptor<Locacao> argCapt = ArgumentCaptor.forClass(Locacao.class);

		Mockito.verify(dao).salvar(argCapt.capture());
		Locacao locacaoRetornada = argCapt.getValue();

		error.checkThat(locacaoRetornada.getValor(), is(12.0));
		error.checkThat(locacaoRetornada.getDataLocacao(), ehHoje());
		error.checkThat(locacaoRetornada.getDataRetorno(), ehHojeComDiferencaDeDias(3));
	}

	@Test
	public void deveAlugarFilme_SemCalcularValor() throws Exception {
		// cenario
		Usuario usuario = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(umFilme().agora());

		PowerMockito.doReturn(1.0).when(service, "calcularValorLocacao", filmes);

		// acao
		Locacao locacao = service.alugarFilme(usuario, filmes);

		// verificacao
		Assert.assertThat(locacao.getValor(), is(1.0));
		PowerMockito.verifyPrivate(service).invoke("calcularValorLocacao", filmes);
	}

	@Test
	public void deveCalcularValorLocacao() throws Exception {

		// cenario
		List<Filme> filmes = Arrays.asList(umFilme().agora());

		// acao
		Double valor =(Double) Whitebox.invokeMethod(service, "calcularValorLocacao",filmes);

		// verificacao
		Assert.assertThat(valor, is(4.0));
	}

}
