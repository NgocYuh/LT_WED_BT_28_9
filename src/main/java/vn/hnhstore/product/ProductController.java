package vn.hnhstore.product;

import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.hnhstore.security.StorePrincipal;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }

    @GetMapping
    String list(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page,
                @AuthenticationPrincipal StorePrincipal actor, Model model) {
        model.addAttribute("products", service.search(q, page));
        model.addAttribute("q", q);
        model.addAttribute("totalProducts", service.count());
        model.addAttribute("myProducts", service.countForUser(actor.id()));
        model.addAttribute("actorId", actor.id());
        model.addAttribute("isAdmin", "ADMIN".equals(actor.role()));
        return "products/list";
    }

    @GetMapping("/new")
    String newForm(Model model) {
        if (!model.containsAttribute("form")) model.addAttribute("form", new ProductForm());
        model.addAttribute("title", "Thêm sản phẩm"); model.addAttribute("action", "/products");
        return "products/form";
    }

    @PostMapping
    String create(@Valid @ModelAttribute("form") ProductForm form, BindingResult result,
                  @AuthenticationPrincipal StorePrincipal actor, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try { service.create(form, actor); }
            catch (IOException | IllegalArgumentException | IllegalStateException ex) { result.reject("product.invalid", ex.getMessage()); }
        }
        if (result.hasErrors()) {
            model.addAttribute("title", "Thêm sản phẩm"); model.addAttribute("action", "/products");
            return "products/form";
        }
        redirect.addFlashAttribute("success", "Đã thêm sản phẩm");
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    String editForm(@PathVariable long id, @AuthenticationPrincipal StorePrincipal actor, Model model) {
        ProductView product = service.editable(id, actor);
        ProductForm form = new ProductForm();
        form.setName(product.name()); form.setDescription(product.description()); form.setPrice(product.price());
        model.addAttribute("form", form); model.addAttribute("product", product);
        model.addAttribute("title", "Sửa sản phẩm #" + id);
        model.addAttribute("action", "/products/" + id);
        return "products/form";
    }

    @PostMapping("/{id}")
    String update(@PathVariable long id, @Valid @ModelAttribute("form") ProductForm form, BindingResult result,
                  @AuthenticationPrincipal StorePrincipal actor, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try { service.update(id, form, actor); }
            catch (IOException | IllegalArgumentException | IllegalStateException ex) { result.reject("product.invalid", ex.getMessage()); }
        }
        if (result.hasErrors()) {
            model.addAttribute("title", "Sửa sản phẩm #" + id); model.addAttribute("action", "/products/" + id);
            model.addAttribute("product", service.editable(id, actor));
            return "products/form";
        }
        redirect.addFlashAttribute("success", "Đã cập nhật sản phẩm");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    String delete(@PathVariable long id, @AuthenticationPrincipal StorePrincipal actor, RedirectAttributes redirect) {
        service.delete(id, actor);
        redirect.addFlashAttribute("success", "Đã xóa sản phẩm");
        return "redirect:/products";
    }
}
