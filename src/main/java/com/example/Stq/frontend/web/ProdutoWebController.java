package com.example.Stq.frontend.web;

import com.example.Stq.frontend.form.CategoriaForm;
import com.example.Stq.frontend.form.ProdutoForm;
import com.example.Stq.produto.application.dto.CategoriaCreateRequest;
import com.example.Stq.produto.application.dto.ProdutoCreateRequest;
import com.example.Stq.produto.application.dto.ProdutoUpdateRequest;
import com.example.Stq.produto.application.dto.VariacaoCreateRequest;
import com.example.Stq.produto.application.services.CategoriaService;
import com.example.Stq.produto.application.services.ProdutoService;
import com.example.Stq.produto.application.services.VariacaoProdutoService;
import com.example.Stq.produto.domain.UnidadeMedida;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/web/produtos")
@RequiredArgsConstructor
public class ProdutoWebController {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final VariacaoProdutoService variacaoProdutoService;

    @GetMapping
    public String listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable,
            Model model) {
        model.addAttribute("produtos", produtoService.listar(categoriaId, ativo, nome, pageable));
        model.addAttribute("categorias", categoriaService.listar(null, true, PageRequest.of(0, 200)).getContent());
        model.addAttribute("filtroNome", nome);
        model.addAttribute("filtroCategoriaId", categoriaId);
        model.addAttribute("filtroAtivo", ativo);
        return "produto/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable UUID id, Model model) {
        model.addAttribute("produto", produtoService.buscarPorId(id));
        return "produto/detalhe";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("produtoForm", new ProdutoForm(null, null, null, null, null));
        model.addAttribute("categorias", categoriaService.listar(null, true, PageRequest.of(0, 200)).getContent());
        model.addAttribute("unidades", UnidadeMedida.values());
        model.addAttribute("editando", false);
        return "produto/form";
    }

    @PostMapping
    public String criar(
            @Valid ProdutoForm produtoForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaService.listar(null, true, PageRequest.of(0, 200)).getContent());
            model.addAttribute("unidades", UnidadeMedida.values());
            model.addAttribute("editando", false);
            return "produto/form";
        }
        var request = new ProdutoCreateRequest(
                produtoForm.nome(),
                produtoForm.descricao(),
                produtoForm.categoriaId(),
                UnidadeMedida.valueOf(produtoForm.unidadeMedida()),
                produtoForm.estoqueMinimo()
        );
        var response = produtoService.criar(request);
        redirectAttributes.addFlashAttribute("sucesso", "Produto criado com sucesso.");
        return "redirect:/web/produtos/" + response.id();
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable UUID id, Model model) {
        var produto = produtoService.buscarPorId(id);
        var form = new ProdutoForm(
                produto.nome(),
                produto.descricao(),
                produto.categoriaId(),
                produto.unidadeMedida().name(),
                produto.estoqueMinimo()
        );
        model.addAttribute("produtoForm", form);
        model.addAttribute("produtoId", id);
        model.addAttribute("categorias", categoriaService.listar(null, true, PageRequest.of(0, 200)).getContent());
        model.addAttribute("unidades", UnidadeMedida.values());
        model.addAttribute("editando", true);
        return "produto/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable UUID id,
            @Valid ProdutoForm produtoForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("produtoId", id);
            model.addAttribute("categorias", categoriaService.listar(null, true, PageRequest.of(0, 200)).getContent());
            model.addAttribute("unidades", UnidadeMedida.values());
            model.addAttribute("editando", true);
            return "produto/form";
        }
        var request = new ProdutoUpdateRequest(
                produtoForm.nome(),
                produtoForm.descricao(),
                produtoForm.categoriaId(),
                UnidadeMedida.valueOf(produtoForm.unidadeMedida()),
                produtoForm.estoqueMinimo(),
                null
        );
        produtoService.atualizar(id, request);
        redirectAttributes.addFlashAttribute("sucesso", "Produto atualizado com sucesso.");
        return "redirect:/web/produtos/" + id;
    }

    @PostMapping("/{id}/desativar")
    public String desativar(
            @PathVariable UUID id,
            RedirectAttributes redirectAttributes) {
        try {
            produtoService.desativar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Produto desativado com sucesso.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/produtos";
    }

    @GetMapping("/categorias")
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", categoriaService.listar(null, null, PageRequest.of(0, 200)).getContent());
        model.addAttribute("categoriaForm", new CategoriaForm(null, null));
        return "produto/categorias";
    }

    @PostMapping("/categorias")
    public String criarCategoria(
            @Valid CategoriaForm categoriaForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaService.listar(null, null, PageRequest.of(0, 200)).getContent());
            return "produto/categorias";
        }
        var request = new CategoriaCreateRequest(categoriaForm.nome(), categoriaForm.categoriaPaiId());
        categoriaService.criar(request);
        redirectAttributes.addFlashAttribute("sucesso", "Categoria criada com sucesso.");
        return "redirect:/web/produtos/categorias";
    }

    @PostMapping("/{id}/variacoes")
    public String adicionarVariacao(
            @PathVariable UUID id,
            @RequestParam String atributo,
            @RequestParam String valor,
            RedirectAttributes redirectAttributes) {
        variacaoProdutoService.adicionar(id, new VariacaoCreateRequest(atributo, valor));
        redirectAttributes.addFlashAttribute("sucesso", "Variação adicionada com sucesso.");
        return "redirect:/web/produtos/" + id;
    }
}
