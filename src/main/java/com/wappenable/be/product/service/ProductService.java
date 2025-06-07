    package com.wappenable.be.product.service;

import com.wappenable.be.product.domain.Product;
import com.wappenable.be.product.domain.ProductImage;
import com.wappenable.be.product.domain.Like;
import com.wappenable.be.product.repository.LikeRepository;
import com.wappenable.be.product.repository.ProductRepository;
import com.wappenable.be.product.infrastructure.LocalFileUploader;
import com.wappenable.be.product.infrastructure.S3Uploader;
import com.wappenable.be.custom.domain.CustomizedImage;
import com.wappenable.be.custom.repository.CustomizedImageRepository;
import com.wappenable.be.product.dto.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final LocalFileUploader fileUploader;
    private final S3Uploader s3Uploader;
    private final CustomizedImageRepository customizedImageRepository;
    private final LikeRepository likeRepository;

    public Product createProduct(String name, int price, int stock, String category, String description, MultipartFile[] images, Long sellerId) {
        List<ProductImage> productImageEntities = new ArrayList<>();
        Product product = Product.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .sellerId(sellerId)
                .category(category)
                .description(description)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        for (MultipartFile file : images) {
            if (file != null && !file.isEmpty()) {
                String imageUrl = fileUploader.upload(file);
                ProductImage pi = ProductImage.builder()
                        .images(imageUrl)
                        .product(product)
                        .build();
                productImageEntities.add(pi);
            }
        }
        product.setProductImages(productImageEntities);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, String name, int price, int stock, String category, String description, MultipartFile[] images, Long sellerId) {
        Product product = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("공통이 없습니다."));
        if (!product.getSellerId().equals(sellerId)) throw new SecurityException("수정 권한이 없습니다.");

        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);
        product.setDescription(description);
        product.setUpdatedAt(LocalDateTime.now());

        if (images != null && images.length > 0 && !images[0].isEmpty()) {
            List<ProductImage> productImageEntities = new ArrayList<>();
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = fileUploader.upload(file);
                    ProductImage pi = ProductImage.builder().images(imageUrl).product(product).build();
                    productImageEntities.add(pi);
                }
            }
            product.getProductImages().clear();
            product.getProductImages().addAll(productImageEntities);
        }
        return productRepository.save(product);
    }

    public void deleteProduct(Long id, Long sellerId) {
        Product product = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("공통이 없습니다."));
        if (!product.getSellerId().equals(sellerId)) throw new SecurityException("삭제 권한이 없습니다.");
        if (product.getProductImages() != null) {
            product.getProductImages().forEach(image -> fileUploader.delete(image.getImages()));
        }
        productRepository.delete(product);
    }

    public Page<Product> getProduct(String keyword, String sortBy, String direction, Pageable pageable) {
        if (keyword == null) keyword = "";
        return productRepository.searchByConditions(keyword.toLowerCase(), sortBy, direction, pageable);
    }

    public Product getProductDetail(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("공통이 없습니다."));
    }

    public List<Map<String, Object>> bulkUpload(MultipartFile csvFile, MultipartFile zipFile, Long sellerId) {
        List<Map<String, Object>> logs = new ArrayList<>();
        Map<String, byte[]> imageMap = extractZip(zipFile);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                row++;
                if (row == 1 && line.contains("name,")) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) {
                    logs.add(Map.of("row", row, "success", false, "message", "여름 개수 부족"));
                    continue;
                }

                String name = parts[0].trim();
                int price, stock;
                String imageName = parts[3].trim();

                try {
                    price = Integer.parseInt(parts[1].trim());
                    stock = Integer.parseInt(parts[2].trim());
                } catch (Exception e) {
                    logs.add(Map.of("row", row, "success", false, "message", "숫자 변환 오류"));
                    continue;
                }

                try {
                    List<ProductImage> productImageEntities = new ArrayList<>();
                    Product product = Product.builder().name(name).price(price).stock(stock).sellerId(sellerId)
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

                    if (imageMap.containsKey(imageName)) {
                        String imageUrl = fileUploader.upload(imageName, imageMap.get(imageName));
                        productImageEntities.add(ProductImage.builder().images(imageUrl).product(product).build());
                    }
                    product.setProductImages(productImageEntities);
                    productRepository.save(product);
                    logs.add(Map.of("row", row, "success", true, "productId", product.getId(), "message", "등록 성공"));

                } catch (Exception e) {
                    logs.add(Map.of("row", row, "success", false, "message", e.getMessage()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV 읽기 오류", e);
        }
        return logs;
    }

    private Map<String, byte[]> extractZip(MultipartFile zipFile) {
        Map<String, byte[]> fileMap = new HashMap<>();
        if (zipFile == null || zipFile.isEmpty()) return fileMap;

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) > 0) baos.write(buffer, 0, len);
                fileMap.put(entry.getName(), baos.toByteArray());
            }
        } catch (IOException e) {
            throw new RuntimeException("ZIP 해제 실패", e);
        }
        return fileMap;
    }

    public ProductResponseDto publishCustomizedDesign(Long customId, Long userId) {
        CustomizedImage custom = customizedImageRepository.findById(customId)
                .orElseThrow(() -> new IllegalArgumentException("디자인을 찾을 수 없습니다."));

        if (!Objects.equals(custom.getUserId(), userId)) {
            throw new SecurityException("본인의 디자인만 가시할 수 있습니다.");
        }

        Product product = Product.builder()
                .name(custom.getTitle() != null ? custom.getTitle() : "사용자 디자인")
                .price(1000)
                .stock(1)
                .category("유저디자인")
                .description("사용자 커스터링 디자인")
                .sellerId(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ProductImage image = ProductImage.builder()
                .images(custom.getCustomizedImageUrl())
                .product(product)
                .build();

        product.setProductImages(List.of(image));
        productRepository.save(product);

        return ProductResponseDto.from(product);
    }

    public List<ProductResponseDto> getAllProductsWithLikes() {
        try {
            List<Product> products = productRepository.findAll();
            return products.stream()
                    .map(p -> {
                        System.out.println("✔️ Product: " + p.getName() + ", ID: " + p.getId());
                        return ProductResponseDto.from(p, likeRepository.countByProduct(p));
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace(); // 꼭!
            throw e;
        }
    }
    
}
