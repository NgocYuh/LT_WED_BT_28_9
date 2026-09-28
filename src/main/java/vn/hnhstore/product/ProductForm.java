package vn.hnhstore.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;

public class ProductForm {
    @NotBlank @Size(max = 200)
    private String name;
    @Size(max = 1000)
    private String description;
    @NotNull @DecimalMin("0.00")
    private BigDecimal price;
    private MultipartFile image;
    private boolean removeImage;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }
    public boolean isRemoveImage() { return removeImage; }
    public void setRemoveImage(boolean removeImage) { this.removeImage = removeImage; }
}
