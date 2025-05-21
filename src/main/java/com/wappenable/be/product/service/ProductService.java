package com.wappenable.be.product.service;

import com.wappenable.be.product.domain.Product;
import com.wappenable.be.product.repository.ProductRepository;
import com.wappenable.be.product.infrastructure.LocalFileUploader;
import com.wappenable.be.product.infrastructure.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.nio.charset.StandardCharsets;
import com.wappenable.be.product.domain.ProductImage;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final LocalFileUploader fileUploader; // 개발용
    private final S3Uploader s3Uploader; // 배포용 (현재 미사용)

    // ============================== 개발용 ==============================

    // 상품 등록
    public Product createProduct(String name, int price, int stock, MultipartFile[] images, Long sellerId) {
        List<ProductImage> productImageEntities = new ArrayList<>();

        Product product = Product.builder()
        .name(name)
        .price(price)
        .stock(stock)
        .sellerId(sellerId)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

        for (MultipartFile file : images) {
            if (file != null && !file.isEmpty()) {
                String imageUrl = fileUploader.upload(file); 

                // 커스터마이징용 이미지도 따로 저장
                ProductImage pi =  ProductImage.builder()
                    .images(imageUrl)
                    .product(product)
                    .build();
                    productImageEntities.add(pi);
            }
        }

       product.setProductImages(productImageEntities); // 새 방식

        return productRepository.save(product);
    }

    // 상품 수정
    public Product updateProduct(Long id, String name, int price, int stock, MultipartFile[] images) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setUpdatedAt(LocalDateTime.now());
        List<ProductImage> productImageEntities = new ArrayList<>();

        if (images != null) {
           
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
            product.getProductImages().clear(); // 기존 이미지 참조 유지
            product.getProductImages().addAll(productImageEntities); // 새 이미지 추가
        }

        return productRepository.save(product);
    }

    // 상품 삭제
    public void deleteProduct(Long id, Long sellerId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!product.getSellerId().equals(sellerId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        productRepository.delete(product);
    }

    // 상품 조회
    public Page<Product> getProduct(String keyword, String sortBy, String direction, Pageable pageable) {
        if (keyword == null) keyword = "";
        return productRepository.searchByConditions(keyword.toLowerCase(),sortBy, direction, pageable);
    }

    public Product getProductDetail(Long id) {
        return productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));
    }

    // 상품 대량 업로드
    public List<Map<String, Object>> bulkUpload(MultipartFile csvFile, MultipartFile zipFile, Long sellerId) {
        List<Map<String, Object>> logs = new ArrayList<>(); // 행별 처리 로그 저장
        Map<String, byte[]> imageMap = extractZip(zipFile); // zip 파일을 map으로 변환
        // csv 파일을 UTF-8로 저장해야 데이터베이스에서 한글을 읽음
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                row++;
                if (row ==1 && line.contains("name,")) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) {
                    logs.add(Map.of("row",row,"success", false, "message", "열 개수 부족"));
                    continue;
                }
                
                String name = parts[0].trim();
                int price, stock;
                String imageName = parts[3].trim();

                try{
                    price = Integer.parseInt(parts[1].trim());
                    stock = Integer.parseInt(parts[2].trim());
                } catch(Exception e) {
                    logs.add(Map.of("row",row ,"success", false , "message", "숫자 변환 오류"));
                    continue;
                }

                try{
                    List<ProductImage> productImageEntities = new ArrayList<>();
                    
                    Product product = Product.builder()
                            .name(name)
                            .price(price)
                            .stock(stock)
                            .sellerId(sellerId)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    if(imageMap.containsKey(imageName)){ // ZIP에 해당 이미지가 있으면 업로드 후 URL 저장
                        String imageUrl = fileUploader.upload(imageName, imageMap.get(imageName));
                        productImageEntities.add(ProductImage.builder()
                                .images(imageUrl)
                                .product(product)
                                .build());
                    }
                    product.setProductImages(productImageEntities);
                    productRepository.save(product);
                    logs.add(Map.of("row", row, "success", true,"productId", product.getId(), "message", "등록 성공"));

                } catch (Exception e){
                    // 상품 등록 중 예외 발생 시
                    logs.add(Map.of("row", row, "success", false, "message", e.getMessage()));
                }
            }
        } catch(Exception e){
            throw new RuntimeException("CSV 읽기 오류", e);
        }
        return logs;
    }

    // 파일 압축 해제 메소드
    private Map<String, byte[]> extractZip(MultipartFile zipFile) {
        Map<String, byte[]> fileMap = new HashMap<>();
        if (zipFile == null || zipFile.isEmpty()) return fileMap; // 비어있으면 빈 맵 반환

        try(ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while((len = zis.read(buffer)) > 0) baos.write(buffer, 0, len);
                fileMap.put(entry.getName(), baos.toByteArray()); // 파일명 -> 내용 저장
            }
        } catch(IOException e){
             throw new RuntimeException("ZIP 해제 실패", e);
        }
        return fileMap;
    }
}
    // ============================== 배포용 (현재 주석 처리 상태) ==============================

    /*
    // 상품 등록
    public Product createProduct(String name, int price, int stock, MultipartFile[] images, Long sellerId) {
        List<String> imageUrls = s3Uploader.uploadFiles(images, "products"); 
        Product product = Product.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .images(imageUrls)
                .sellerId(sellerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return productRepository.save(product);
    }

    // 상품 수정
    public Product updateProduct(Long id, String name, int price, int stock, MultipartFile[] images, Long sellerId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!product.getSellerId().equals(sellerId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setUpdatedAt(LocalDateTime.now());

        if (images != null) {
            List<String> imageUrls = s3Uploader.uploadFiles(images, "products");
            product.setImages(imageUrls);
        }

        return productRepository.save(product);
    }

    // 상품 삭제
    public void deleteProduct(Long id, Long sellerId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!product.getSellerId().equals(sellerId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        productRepository.delete(product);
    }

    
    // 상품 조회
    public Page<Product> getProduct(String keyword, String sortBy, String direction, Pageable pageable) {
        if (keyword == null) keyword = "";
        return productRepository.searchByConditions(keyword.toLowerCase(),sortBy, direction, pageable);
    }

    public Product getProductDetail(Long id) {
        return productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));
    }

    
    public List<Map<String, Object>> bulkUpload(MultipartFile csvFile, MultipartFile zipFile, Long sellerId) {
        List<Map<String, Object>> logs = new ArrayList<>(); // 행별 처리 로그 저장
        Map<String, byte[]> imageMap = extractZip(zipFile); // zip 파일을 map으로 변환
        // csv 파일을 UTF-8로 저장해야 데이터베이스에서 한글을 읽음
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                row++;
                if (row ==1 && line.contains("name,")) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) {
                    logs.add(Map.of("row",row,"success", false, "message", "열 개수 부족"));
                    continue;
                }
                
                String name = parts[0].trim();
                int price, stock;
                String imageName = parts[3].trim();

                try{
                    price = Integer.parseInt(parts[1].trim());
                    stock = Integer.parseInt(parts[2].trim());
                } catch(Exception e) {
                    logs.add(Map.of("row",row ,"success", false , "message", "숫자 변환 오류"));
                    continue;
                }

                try{
                    List<String> imageUrls = new ArrayList<>();
                    if(imageMap.containsKey(imageName)){ // ZIP에 해당 이미지가 있으면 업로드 후 URL 저장
                        imageUrls.add(fileUploader.upload(imageName, imageMap.get(imageName)));
                    }

                    // 상품 객체 생성

                Product product = Product.builder()
                        .name(name)
                        .price(price)
                        .stock(stock)
                        .images(imageUrls)
                        .sellerId(sellerId)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                    
                    productRepository.save(product); // DB저장
                    logs.add(Map.of("row", row, "success", true,"productId", product.getId(), "message", "등록 성공"));

                } catch (Exception e){
                    // 상품 등록 중 예외 발생 시
                    logs.add(Map.of("row", row, "success", false, "message", e.getMessage()));
                }
            }
        } catch(Exception e){
            throw new RuntimeException("CSV 읽기 오류", e);
        }
        return logs;
    }

    // 파일 압축 해제 메소드
    private Map<String, byte[]> extractZip(MultipartFile zipFile) {
        Map<String, byte[]> fileMap = new HashMap<>();
        if (zipFile == null || zipFile.isEmpty()) return fileMap; // 비어있으면 빈 맵 반환

        try(ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while((len = zis.read(buffer)) > 0) baos.write(buffer, 0, len);
                fileMap.put(entry.getName(), baos.toByteArray()); // 파일명 -> 내용 저장
            }
        } catch(IOException e){
             throw new RuntimeException("ZIP 해제 실패", e);
        }
        return fileMap;
    }
    */


