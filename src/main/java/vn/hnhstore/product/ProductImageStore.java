package vn.hnhstore.product;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface ProductImageStore {
    record Image(String url, String publicId) { }
    Image upload(MultipartFile file) throws IOException;
    void delete(String publicId) throws IOException;
}
