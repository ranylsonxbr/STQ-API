package com.example.Stq.fornecedor.application.services;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.fornecedor.application.dto.FornecedorRequest;
import com.example.Stq.fornecedor.application.dto.FornecedorResponse;
import com.example.Stq.fornecedor.domain.CnpjValidator;
import com.example.Stq.fornecedor.domain.Fornecedor;
import com.example.Stq.fornecedor.domain.FornecedorFiltro;
import com.example.Stq.fornecedor.domain.FornecedorRepository;
import com.example.Stq.fornecedor.domain.exception.CnpjDuplicadoException;
import com.example.Stq.fornecedor.domain.exception.CnpjInvalidoException;
import com.example.Stq.fornecedor.domain.exception.EdicaoCnpjNaoPermitidaException;
import com.example.Stq.fornecedor.domain.exception.FornecedorComPedidoEmAbertoException;
import com.example.Stq.fornecedor.domain.exception.FornecedorNotFoundException;
import com.example.Stq.fornecedor.domain.port.PedidoCompraReadPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FornecedorServiceImpl implements FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final PedidoCompraReadPort pedidoCompraReadPort;

    @Override
    @Transactional
    public FornecedorResponse criar(FornecedorRequest request, UUID usuarioId) {
        String cnpj = CnpjValidator.normalizar(request.cnpj());
        if (!CnpjValidator.isValido(cnpj)) {
            throw new CnpjInvalidoException();
        }
        if (fornecedorRepository.existsByCnpj(cnpj)) {
            throw new CnpjDuplicadoException();
        }
        Fornecedor fornecedor = Fornecedor.builder()
                .razaoSocial(request.razaoSocial())
                .cnpj(cnpj)
                .email(request.email())
                .telefone(request.telefone())
                .contato(request.contato())
                .atualizadoPor(usuarioId)
                .build();
        return FornecedorResponse.de(fornecedorRepository.save(fornecedor));
    }

    @Override
    public Page<FornecedorResponse> listar(FornecedorFiltro filtro, Pageable pageable) {
        return fornecedorRepository.findAll(filtro, pageable).map(FornecedorResponse::de);
    }

    @Override
    public FornecedorResponse buscarPorId(UUID id) {
        return fornecedorRepository.findById(id)
                .map(FornecedorResponse::de)
                .orElseThrow(() -> new FornecedorNotFoundException(id));
    }

    @Override
    @Transactional
    public FornecedorResponse atualizar(UUID id, FornecedorRequest request, UUID usuarioId, Perfil perfil) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new FornecedorNotFoundException(id));

        String novoCnpj = CnpjValidator.normalizar(request.cnpj());
        if (!novoCnpj.equals(fornecedor.getCnpj())) {
            if (perfil != Perfil.ADMIN) {
                throw new EdicaoCnpjNaoPermitidaException();
            }
            if (pedidoCompraReadPort.existePedidoEmAbertoParaFornecedor(id)) {
                throw new FornecedorComPedidoEmAbertoException();
            }
            if (!CnpjValidator.isValido(novoCnpj)) {
                throw new CnpjInvalidoException();
            }
            if (fornecedorRepository.existsByCnpjAndIdNot(novoCnpj, id)) {
                throw new CnpjDuplicadoException();
            }
            fornecedor.setCnpj(novoCnpj);
        }

        fornecedor.setRazaoSocial(request.razaoSocial());
        fornecedor.setEmail(request.email());
        fornecedor.setTelefone(request.telefone());
        fornecedor.setContato(request.contato());
        fornecedor.setAtualizadoPor(usuarioId);

        return FornecedorResponse.de(fornecedorRepository.save(fornecedor));
    }

    @Override
    @Transactional
    public void desativar(UUID id, UUID usuarioId) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new FornecedorNotFoundException(id));
        if (!fornecedor.isAtivo()) {
            return;
        }
        if (pedidoCompraReadPort.existePedidoEmAbertoParaFornecedor(id)) {
            throw new FornecedorComPedidoEmAbertoException();
        }
        fornecedor.setAtivo(false);
        fornecedor.setAtualizadoPor(usuarioId);
        fornecedorRepository.save(fornecedor);
    }

    @Override
    @Transactional
    public FornecedorResponse reativar(UUID id, UUID usuarioId) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new FornecedorNotFoundException(id));
        if (fornecedor.isAtivo()) {
            return FornecedorResponse.de(fornecedor);
        }
        fornecedor.setAtivo(true);
        fornecedor.setAtualizadoPor(usuarioId);
        return FornecedorResponse.de(fornecedorRepository.save(fornecedor));
    }
}
