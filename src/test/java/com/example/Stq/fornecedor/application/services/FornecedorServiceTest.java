package com.example.Stq.fornecedor.application.services;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.fornecedor.application.dto.FornecedorRequest;
import com.example.Stq.fornecedor.application.dto.FornecedorResponse;
import com.example.Stq.fornecedor.domain.Fornecedor;
import com.example.Stq.fornecedor.domain.FornecedorRepository;
import com.example.Stq.fornecedor.domain.exception.CnpjDuplicadoException;
import com.example.Stq.fornecedor.domain.exception.CnpjInvalidoException;
import com.example.Stq.fornecedor.domain.exception.EdicaoCnpjNaoPermitidaException;
import com.example.Stq.fornecedor.domain.exception.FornecedorComPedidoEmAbertoException;
import com.example.Stq.fornecedor.domain.exception.FornecedorNotFoundException;
import com.example.Stq.fornecedor.domain.port.PedidoCompraReadPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FornecedorServiceImpl")
class FornecedorServiceTest {

    private static final String CNPJ_VALIDO = "11222333000181";
    private static final String CNPJ_VALIDO_FORMATADO = "11.222.333/0001-81";
    private static final String CNPJ_OUTRO_VALIDO = "60701190000104";

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private PedidoCompraReadPort pedidoCompraReadPort;

    @InjectMocks
    private FornecedorServiceImpl service;

    private FornecedorRequest requestValido(String cnpj) {
        return new FornecedorRequest("Razao Social Teste", cnpj, "email@teste.com", "11999990000", "Contato");
    }

    private Fornecedor fornecedorAtivo(UUID id, String cnpj) {
        return Fornecedor.builder()
                .id(id)
                .razaoSocial("Razao Social Teste")
                .cnpj(cnpj)
                .email("email@teste.com")
                .telefone("11999990000")
                .contato("Contato")
                .ativo(true)
                .build();
    }

    private Fornecedor fornecedorInativo(UUID id, String cnpj) {
        return Fornecedor.builder()
                .id(id)
                .razaoSocial("Razao Social Teste")
                .cnpj(cnpj)
                .email("email@teste.com")
                .ativo(false)
                .build();
    }

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("[FORN-C1] deve criar fornecedor com CNPJ formatado e persistir normalizado")
        void deveCriarFornecedorComCnpjFormatadoPersistindoNormalizado() {
            UUID usuarioId = UUID.randomUUID();
            FornecedorRequest request = requestValido(CNPJ_VALIDO_FORMATADO);
            Fornecedor fornecedorSalvo = fornecedorAtivo(UUID.randomUUID(), CNPJ_VALIDO);

            when(fornecedorRepository.existsByCnpj(CNPJ_VALIDO)).thenReturn(false);
            when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedorSalvo);

            service.criar(request, usuarioId);

            ArgumentCaptor<Fornecedor> captor = ArgumentCaptor.forClass(Fornecedor.class);
            verify(fornecedorRepository).save(captor.capture());
            assertThat(captor.getValue().getCnpj()).isEqualTo(CNPJ_VALIDO);
            assertThat(captor.getValue().getCnpj()).hasSize(14);
        }

        @Test
        @DisplayName("[FORN-C2] deve lançar CnpjInvalidoException quando CNPJ tem dígito verificador inválido")
        void deveLancarCnpjInvalidoExceptionQuandoCnpjComDigitoErrado() {
            UUID usuarioId = UUID.randomUUID();
            FornecedorRequest request = requestValido("11222333000182");

            assertThatThrownBy(() -> service.criar(request, usuarioId))
                    .isInstanceOf(CnpjInvalidoException.class);

            verify(fornecedorRepository, never()).save(any());
        }

        @Test
        @DisplayName("[FORN-C3] deve lançar CnpjDuplicadoException quando CNPJ já está cadastrado")
        void deveLancarCnpjDuplicadoExceptionQuandoCnpjJaCadastrado() {
            UUID usuarioId = UUID.randomUUID();
            FornecedorRequest request = requestValido(CNPJ_VALIDO);

            when(fornecedorRepository.existsByCnpj(CNPJ_VALIDO)).thenReturn(true);

            assertThatThrownBy(() -> service.criar(request, usuarioId))
                    .isInstanceOf(CnpjDuplicadoException.class);

            verify(fornecedorRepository, never()).save(any());
        }

        @Test
        @DisplayName("[FORN-C4] deve setar atualizadoPor com o usuarioId recebido")
        void deveSetarAtualizadoPorComUsuarioId() {
            UUID usuarioId = UUID.randomUUID();
            FornecedorRequest request = requestValido(CNPJ_VALIDO);
            Fornecedor fornecedorSalvo = fornecedorAtivo(UUID.randomUUID(), CNPJ_VALIDO);

            when(fornecedorRepository.existsByCnpj(CNPJ_VALIDO)).thenReturn(false);
            when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedorSalvo);

            service.criar(request, usuarioId);

            ArgumentCaptor<Fornecedor> captor = ArgumentCaptor.forClass(Fornecedor.class);
            verify(fornecedorRepository).save(captor.capture());
            assertThat(captor.getValue().getAtualizadoPor()).isEqualTo(usuarioId);
        }
    }

    @Nested
    @DisplayName("atualizar")
    class Atualizar {

        @Test
        @DisplayName("[FORN-U1] OPERADOR deve atualizar campos sem alterar CNPJ com sucesso")
        void operadorDeveAtualizarCamposSemAlterarCnpj() {
            UUID id = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            Fornecedor fornecedor = fornecedorAtivo(id, CNPJ_VALIDO);
            FornecedorRequest request = new FornecedorRequest("Novo Nome", CNPJ_VALIDO, "novo@email.com", "11888880000", "Novo Contato");

            when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedor));
            when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedor);

            service.atualizar(id, request, usuarioId, Perfil.OPERADOR);

            verify(fornecedorRepository).save(fornecedor);
            verify(pedidoCompraReadPort, never()).existePedidoEmAbertoParaFornecedor(any());
        }

        @Test
        @DisplayName("[FORN-U2] OPERADOR deve lançar EdicaoCnpjNaoPermitidaException ao tentar alterar CNPJ")
        void operadorDeveLancarExcecaoAoTentarAlterarCnpj() {
            UUID id = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            Fornecedor fornecedor = fornecedorAtivo(id, CNPJ_VALIDO);
            FornecedorRequest request = requestValido(CNPJ_OUTRO_VALIDO);

            when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedor));

            assertThatThrownBy(() -> service.atualizar(id, request, usuarioId, Perfil.OPERADOR))
                    .isInstanceOf(EdicaoCnpjNaoPermitidaException.class);

            verify(fornecedorRepository, never()).save(any());
        }

        @Test
        @DisplayName("[FORN-U3] ADMIN deve alterar CNPJ sem pedido em aberto com sucesso")
        void adminDeveAlterarCnpjSemPedidoEmAberto() {
            UUID id = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            Fornecedor fornecedor = fornecedorAtivo(id, CNPJ_VALIDO);
            FornecedorRequest request = requestValido(CNPJ_OUTRO_VALIDO);

            when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedor));
            when(pedidoCompraReadPort.existePedidoEmAbertoParaFornecedor(id)).thenReturn(false);
            when(fornecedorRepository.existsByCnpjAndIdNot(CNPJ_OUTRO_VALIDO, id)).thenReturn(false);
            when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedor);

            service.atualizar(id, request, usuarioId, Perfil.ADMIN);

            verify(fornecedorRepository).save(fornecedor);
            assertThat(fornecedor.getCnpj()).isEqualTo(CNPJ_OUTRO_VALIDO);
        }

        @Test
        @DisplayName("[FORN-U4] ADMIN deve lançar FornecedorComPedidoEmAbertoException ao alterar CNPJ com pedido em aberto")
        void adminDeveLancarExcecaoAoAlterarCnpjComPedidoEmAberto() {
            UUID id = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            Fornecedor fornecedor = fornecedorAtivo(id, CNPJ_VALIDO);
            FornecedorRequest request = requestValido(CNPJ_OUTRO_VALIDO);

            when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedor));
            when(pedidoCompraReadPort.existePedidoEmAbertoParaFornecedor(id)).thenReturn(true);

            assertThatThrownBy(() -> service.atualizar(id, request, usuarioId, Perfil.ADMIN))
                    .isInstanceOf(FornecedorComPedidoEmAbertoException.class);

            verify(fornecedorRepository, never()).save(any());
        }

        @Test
        @DisplayName("[FORN-U5] ADMIN deve lançar CnpjDuplicadoException ao alterar CNPJ para valor já usado por outro fornecedor")
        void adminDeveLancarExcecaoAoAlterarCnpjParaValorJaUsado() {
            UUID id = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            Fornecedor fornecedor = fornecedorAtivo(id, CNPJ_VALIDO);
            FornecedorRequest request = requestValido(CNPJ_OUTRO_VALIDO);

            when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedor));
            when(pedidoCompraReadPort.existePedidoEmAbertoParaFornecedor(id)).thenReturn(false);
            when(fornecedorRepository.existsByCnpjAndIdNot(CNPJ_OUTRO_VALIDO, id)).thenReturn(true);

            assertThatThrownBy(() -> service.atualizar(id, request, usuarioId, Perfil.ADMIN))
                    .isInstanceOf(CnpjDuplicadoException.class);

            verify(fornecedorRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("desativar")
    class Desativar {

        @Test
        @DisplayName("[FORN-D1] deve desativar fornecedor ativo sem pedido em aberto")
        void deveDesativarFornecedorAtivoSemPedido() {
            UUID id = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            Fornecedor fornecedor = fornecedorAtivo(id, CNPJ_VALIDO);

            when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedor));
            when(pedidoCompraReadPort.existePedidoEmAbertoParaFornecedor(id)).thenReturn(false);
            when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedor);

            service.desativar(id, usuarioId);

            assertThat(fornecedor.isAtivo()).isFalse();
            verify(fornecedorRepository).save(fornecedor);
        }

        @Test
        @DisplayName("[FORN-D2] deve lançar FornecedorComPedidoEmAbertoException ao desativar com pedido em aberto")
        void deveLancarExcecaoAoDesativarComPedidoEmAberto() {
            UUID id = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            Fornecedor fornecedor = fornecedorAtivo(id, CNPJ_VALIDO);

            when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedor));
            when(pedidoCompraReadPort.existePedidoEmAbertoParaFornecedor(id)).thenReturn(true);

            assertThatThrownBy(() -> service.desativar(id, usuarioId))
                    .isInstanceOf(FornecedorComPedidoEmAbertoException.class);

            verify(fornecedorRepository, never()).save(any());
        }

        @Test
        @DisplayName("[FORN-D3] deve retornar sem erro e sem salvar quando fornecedor já está inativo")
        void deveRetornarSemErroQuandoFornecedorJaInativo() {
            UUID id = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            Fornecedor fornecedor = fornecedorInativo(id, CNPJ_VALIDO);

            when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedor));

            service.desativar(id, usuarioId);

            verify(fornecedorRepository, never()).save(any());
            verify(pedidoCompraReadPort, never()).existePedidoEmAbertoParaFornecedor(any());
        }
    }

    @Nested
    @DisplayName("reativar")
    class Reativar {

        @Test
        @DisplayName("[FORN-R1] deve reativar fornecedor inativo")
        void deveReativarFornecedorInativo() {
            UUID id = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            Fornecedor fornecedor = fornecedorInativo(id, CNPJ_VALIDO);

            when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedor));
            when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedor);

            service.reativar(id, usuarioId);

            assertThat(fornecedor.isAtivo()).isTrue();
            verify(fornecedorRepository).save(fornecedor);
        }

        @Test
        @DisplayName("[FORN-R2] deve retornar estado atual sem salvar quando fornecedor já está ativo")
        void deveRetornarEstadoAtualSemSalvarQuandoFornecedorJaAtivo() {
            UUID id = UUID.randomUUID();
            UUID usuarioId = UUID.randomUUID();
            Fornecedor fornecedor = fornecedorAtivo(id, CNPJ_VALIDO);

            when(fornecedorRepository.findById(id)).thenReturn(Optional.of(fornecedor));

            FornecedorResponse resposta = service.reativar(id, usuarioId);

            assertThat(resposta).isNotNull();
            verify(fornecedorRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("[FORN-N1] deve lançar FornecedorNotFoundException quando id não existe")
        void deveLancarFornecedorNotFoundExceptionQuandoIdNaoExiste() {
            UUID id = UUID.randomUUID();

            when(fornecedorRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(id))
                    .isInstanceOf(FornecedorNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }
}
