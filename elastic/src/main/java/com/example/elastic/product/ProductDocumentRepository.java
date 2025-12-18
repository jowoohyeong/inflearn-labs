package com.example.elastic.product;

import com.example.elastic.product.domain.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {
}
