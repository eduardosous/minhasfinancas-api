package com.dsousa.minhasfinancas.service;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dsousa.minhasfinancas.exception.ErroAutenticao;
import com.dsousa.minhasfinancas.exception.RegraNegocioException;
import com.dsousa.minhasfinancas.model.entity.Usuario;
import com.dsousa.minhasfinancas.model.repository.UsuarioRepository;
import com.dsousa.minhasfinancas.service.impl.UsuarioServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
public class UsuarioServiceTest {

	@SpyBean
	UsuarioServiceImpl service;

	@MockBean
	UsuarioRepository repository;

	// @BeforeAll
	/*
	 * public void setUp() {
	 * 
	 * service = Mockito.spy(UsuarioServiceImpl.class);
	 * 
	 * // repository = Mockito.mock(UsuarioRepository.class); // service = new
	 * UsuarioServiceImpl(repository); }
	 */

	@Test
	public void deveSalvarUmUsuario() {
		// cenário
		Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
		Usuario usuario = Usuario.builder().id(1L).nome("nome").email("email@email.com").senha("senha").build();

		Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

		// acao
		Usuario usuarioSalvo = service.salvarUsuario(new Usuario());

		// verificacao
		Assertions.assertThat(usuarioSalvo).isNotNull();
		Assertions.assertThat(usuarioSalvo.getId()).isEqualTo(1L);
		Assertions.assertThat(usuarioSalvo.getNome()).isEqualTo("nome");
		Assertions.assertThat(usuarioSalvo.getEmail()).isEqualTo("email@email.com");
		Assertions.assertThat(usuarioSalvo.getSenha()).isEqualTo("senha");

	}

	@Test
	public void naoDeveSalvarUmUsuarioComEmailJaCadastrado() {

		// cenario
		String email = "email@email.com";
		Usuario usuario = Usuario.builder().email(email).build();
		Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);

		// acao
		service.salvarUsuario(usuario);

		// verificacao
		Mockito.verify(repository, Mockito.never()).save(usuario);

	}

	@Test
	public void deveAutenticarUmUsuarioComSucesso() {
		// cenário
		String email = "email@email.com";
		String senha = "senha";

		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
		Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));

		// acao
		Usuario result = service.autenticar(email, senha);

		// verificacao
		Assertions.assertThat(result).isNotNull();
	}

	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado() {

		// cenario
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

		// acao
		Throwable exception = Assertions.catchThrowable(() -> service.autenticar("email@email.com", "senha"));

		// verificacao
		Assertions.assertThat(exception).isInstanceOf(ErroAutenticao.class)
				.hasMessage("Usuário não encontrado para o e-mail informado.");

	}

	@Test
	public void deveLancarErroQuandoSenhaNaoBater() {

		// cenario
		String senha = "senha";
		Usuario usuario = Usuario.builder().email("email@email.com").senha(senha).build();
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));

		// acao
		Throwable exception = Assertions.catchThrowable(() -> service.autenticar("email@email.com", "123"));

		// verificacao
		Assertions.assertThat(exception).isInstanceOf(ErroAutenticao.class).hasMessage("Senha inválida.");

	}

	@Test
	public void deveValidarEmail() {
		// cenario
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);

		// acao
		service.validarEmail("email@email.com");

	}

	@Test
	public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado() {

		// cenario
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);

		// acao
		Throwable exception = Assertions.catchThrowable(() -> service.validarEmail("email@email.com"));

		// verificacao
		Assertions.assertThat(exception).isInstanceOf(RegraNegocioException.class)
				.hasMessage("Já existe um usuário cadastrado com este e-mail.");

	}

}
