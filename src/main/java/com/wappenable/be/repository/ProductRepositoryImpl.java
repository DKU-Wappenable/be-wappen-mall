package com.wappenable.be.repository;

import com.wappenable.be.domain.Product;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom{

        private final EntityManager em;

        @Override 
        public Page<Product> searchByConditions(String keyword, String sortBy, String direction, Pageable pageable) {
                 // 검색 조건: 이름 또는 sellerId에 keyword가 포함되는 경우
            String baseQuery = "SELECT p FROM Product p WHERE LOWER(p.name) LIKE :kw OR STR(p.sellerId) LIKE :kw";
            String countQuery = "SELECT COUNT(p) FROM Product p WHERE LOWER(p.name) LIKE :kw OR STR(p.sellerId) LIKE :kw";
                // 정렬 가능한 필드만 허용 (허용되지 않으면 createdAt 기본값 사용)
            List<String> allowedSortFields = List.of("createdAt", "price", "name");
            if (!allowedSortFields.contains(sortBy)) {
                sortBy = "createdAt";
            }
            // 정렬 방향 설정 (asc / desc)
            boolean ascending = direction.equalsIgnoreCase("asc");
            String orderClause = " ORDER BY p." + sortBy + (ascending ? " ASC" : " DESC");
            //  실제 데이터 조회
            List<Product> content = em.createQuery(baseQuery + orderClause, Product.class)
                    .setParameter("kw", "%" + keyword + "%")
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
            // 전체 개수 조회 
            Long count = em.createQuery(countQuery, Long.class)
                    .setParameter("kw", "%" + keyword + "%")
                    .getSingleResult();

            return new PageImpl<>(content, pageable, count);
                }
}
