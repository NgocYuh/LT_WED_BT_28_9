package vn.hnhstore.product;

import com.cloudinary.Cloudinary;
import java.io.IOException;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryImageStore implements ProductImageStore {
    private final Environment env;
    public CloudinaryImageStore(Environment env) { this.env = env; }

    private Cloudinary client() {
        String name = env.getProperty("CLOUDINARY_CLOUD_NAME", "");
        String key = env.getProperty("CLOUDINARY_API_KEY", "");
        String secret = env.getProperty("CLOUDINARY_API_SECRET", "");
        if (name.isBlank() || key.isBlank() || secret.isBlank())
            throw new IllegalStateException("Cloudinary chưa được cấu hình");
        return new Cloudinary(Map.of("cloud_name", name, "api_key", key, "api_secret", secret, "secure", true));
    }

    @Override
    public Image upload(MultipartFile file) throws IOException {
        Map<?, ?> result = client().uploader().upload(file.getBytes(),
                Map.of("folder", "hnhstore/products", "resource_type", "image"));
        return new Image((String) result.get("secure_url"), (String) result.get("public_id"));
    }

    @Override
    public void delete(String publicId) throws IOException {
        if (publicId != null && !publicId.isBlank())
            client().uploader().destroy(publicId, Map.of("resource_type", "image"));
    }
}
