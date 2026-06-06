package com.example.Stq.config;

import com.example.Stq.autenticacao.domain.exception.ContaDesativadaException;
import com.example.Stq.autenticacao.domain.exception.CredenciaisInvalidasException;
import com.example.Stq.fornecedor.domain.exception.CnpjDuplicadoException;
import com.example.Stq.fornecedor.domain.exception.CnpjInvalidoException;
import com.example.Stq.fornecedor.domain.exception.EdicaoCnpjNaoPermitidaException;
import com.example.Stq.fornecedor.domain.exception.FornecedorComPedidoEmAbertoException;
import com.example.Stq.fornecedor.domain.exception.FornecedorNotFoundException;
import com.example.Stq.produto.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- autenticacao ---

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ProblemDetail handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.");
    }

    @ExceptionHandler(ContaDesativadaException.class)
    public ProblemDetail handleContaDesativada(ContaDesativadaException ex) {
        return problema(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // --- fornecedor ---

    @ExceptionHandler(FornecedorNotFoundException.class)
    public ProblemDetail handleFornecedorNotFound(FornecedorNotFoundException ex) {
        return problema(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CnpjInvalidoException.class)
    public ProblemDetail handleCnpjInvalido(CnpjInvalidoException ex) {
        return problema(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CnpjDuplicadoException.class)
    public ProblemDetail handleCnpjDuplicado(CnpjDuplicadoException ex) {
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(EdicaoCnpjNaoPermitidaException.class)
    public ProblemDetail handleEdicaoCnpjNaoPermitida(EdicaoCnpjNaoPermitidaException ex) {
        return problema(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(FornecedorComPedidoEmAbertoException.class)
    public ProblemDetail handleFornecedorComPedidoEmAberto(FornecedorComPedidoEmAbertoException ex) {
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    // --- produto: categoria ---

    @ExceptionHandler(CategoriaNotFoundException.class)
    public ProblemDetail handleCategoriaNotFound(CategoriaNotFoundException ex) {
        return problema(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CategoriaInativaException.class)
    public ProblemDetail handleCategoriaInativa(CategoriaInativaException ex) {
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CategoriaComProdutosException.class)
    public ProblemDetail handleCategoriaComProdutos(CategoriaComProdutosException ex) {
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CategoriaComFilhasException.class)
    public ProblemDetail handleCategoriaComFilhas(CategoriaComFilhasException ex) {
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CategoriaNomeDuplicadoException.class)
    public ProblemDetail handleCategoriaNomeDuplicado(CategoriaNomeDuplicadoException ex) {
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    // --- produto: produto ---

    @ExceptionHandler(ProdutoNotFoundException.class)
    public ProblemDetail handleProdutoNotFound(ProdutoNotFoundException ex) {
        return problema(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ProdutoComPedidoEmAbertoException.class)
    public ProblemDetail handleProdutoComPedido(ProdutoComPedidoEmAbertoException ex) {
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    // --- produto: variacao ---

    @ExceptionHandler(VariacaoNotFoundException.class)
    public ProblemDetail handleVariacaoNotFound(VariacaoNotFoundException ex) {
        return problema(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(VariacaoDuplicadaException.class)
    public ProblemDetail handleVariacaoDuplicada(VariacaoDuplicadaException ex) {
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(SkuColisaoException.class)
    public ProblemDetail handleSkuColisao(SkuColisaoException ex) {
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    // --- validacao ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidacao(MethodArgumentNotValidException ex) {
        String detalhe = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return problema(HttpStatus.BAD_REQUEST, detalhe);
    }

    private ProblemDetail problema(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("about:blank"));
        return problem;
    }
}
