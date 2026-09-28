package vn.hnhstore.product;

import java.io.IOException;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import vn.hnhstore.security.StorePrincipal;
import vn.hnhstore.user.User;
import vn.hnhstore.user.UserRepository;

@Service
@PreAuthorize("isAuthenticated()")
public class ProductService {
    private static final Set<String> TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final ProductRepository products;
    private final UserRepository users;
    private final ProductMapper mapper;
    private final ProductImageStore images;

    public ProductService(ProductRepository products, UserRepository users, ProductMapper mapper, ProductImageStore images) {
        this.products = products; this.users = users; this.mapper = mapper; this.images = images;
    }

    @Transactional(readOnly = true)
    public Page<ProductView> search(String q, int page) {
        return products.search(q == null ? "" : q.trim(), PageRequest.of(Math.max(0, page), 10,
                Sort.by("id").descending())).map(mapper::toView);
    }
    @Transactional(readOnly = true)
    public long count() { return products.count(); }
    @Transactional(readOnly = true)
    public long countForUser(long id) { return products.countByUserId(id); }

    @Transactional(readOnly = true)
    public ProductView editable(long id, StorePrincipal actor) {
        Product product = products.findById(id).orElseThrow();
        requireOwnerOrAdmin(product, actor);
        return mapper.toView(product);
    }

    @Transactional
    public void create(ProductForm form, StorePrincipal actor) throws IOException {
        User owner = users.findById(actor.id()).orElseThrow();
        Product product = new Product(form.getName().trim(), form.getDescription(), form.getPrice(), owner);
        applyImage(product, form);
        products.save(product);
    }

    @Transactional
    public void update(long id, ProductForm form, StorePrincipal actor) throws IOException {
        Product product = products.findById(id).orElseThrow();
        requireOwnerOrAdmin(product, actor);
        product.update(form.getName().trim(), form.getDescription(), form.getPrice());
        applyImage(product, form);
    }

    @Transactional
    public void delete(long id, StorePrincipal actor) {
        Product product = products.findById(id).orElseThrow();
        requireOwnerOrAdmin(product, actor);
        String oldId = product.getImagePublicId();
        products.delete(product);
        afterCommitDelete(oldId);
    }

    private void requireOwnerOrAdmin(Product product, StorePrincipal actor) {
        if (!product.getUser().getId().equals(actor.id()) && !"ADMIN".equals(actor.role()))
            throw new AccessDeniedException("Bạn không sở hữu sản phẩm này");
    }

    private void applyImage(Product product, ProductForm form) throws IOException {
        MultipartFile file = form.getImage();
        if (file != null && !file.isEmpty()) {
            if (file.getSize() > 5L * 1024 * 1024 || !TYPES.contains(file.getContentType()))
                throw new IllegalArgumentException("Ảnh phải là JPG, PNG hoặc WebP và không quá 5 MB");
            ProductImageStore.Image image = images.upload(file);
            String oldId = product.getImagePublicId();
            product.setImage(image.url(), image.publicId());
            afterRollbackDelete(image.publicId());
            afterCommitDelete(oldId);
        } else if (form.isRemoveImage()) {
            String oldId = product.getImagePublicId();
            product.setImage(null, null);
            afterCommitDelete(oldId);
        }
    }

    private void afterCommitDelete(String publicId) {
        if (publicId == null) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                try { images.delete(publicId); }
                catch (IOException ex) { System.err.println("Cloudinary cleanup failed for product image"); }
            }
        });
    }
    private void afterRollbackDelete(String publicId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try { images.delete(publicId); }
                    catch (IOException ex) { System.err.println("Cloudinary rollback cleanup failed"); }
                }
            }
        });
    }
}
