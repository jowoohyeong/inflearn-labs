package com.example.elastic.product;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.example.elastic.product.domain.Product;
import com.example.elastic.product.domain.ProductDocument;
import com.example.elastic.product.dto.CreateProductRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;

import com.example.elastic.common.utils.HangulKeyConverter;
import com.example.elastic.common.utils.JasoDecomposer;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductService(ProductRepository productRepository, ProductDocumentRepository productDocumentRepository, ElasticsearchOperations elasticsearchOperations) {
        this.productRepository = productRepository;
        this.productDocumentRepository = productDocumentRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public List<Product> getProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return productRepository.findAll(pageable).getContent();
    }

    public Product createProduct(CreateProductRequestDto createProductRequestDto) {
        Product product = new Product(
                createProductRequestDto.getName(),
                createProductRequestDto.getDescription(),
                createProductRequestDto.getPrice(),
                createProductRequestDto.getRating(),
                createProductRequestDto.getCategory()
        );
        Product saveProduct = productRepository.save(product);
        ProductDocument productDocument = new ProductDocument(
                saveProduct.getId().toString(),
                saveProduct.getName(),
                saveProduct.getDescription(),
                saveProduct.getPrice(),
                saveProduct.getRating(),
                saveProduct.getCategory(),
                JasoDecomposer.decompose(saveProduct.getName())
        );
        productDocumentRepository.save(productDocument);

        return saveProduct;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
        productDocumentRepository.deleteById(id.toString());
    }

    public List<String> getSuggestions(String query) {
        String convertedQuery = HangulKeyConverter.convert(query);
        String jasoQuery = JasoDecomposer.decompose(query);
        String convertedJasoQuery = JasoDecomposer.decompose(convertedQuery);

        List<Query> matchQueries = new ArrayList<>();

        // 1. 원본 검색어 매칭
        matchQueries.add(MultiMatchQuery.of(m -> m.query(query)
                .type(TextQueryType.BoolPrefix)
                .fields("name.auto_complete", "name.auto_complete._2gram", "name.auto_complete._3gram")
        )._toQuery());

        // 2. 한영 오타 변환된 검색어 매칭
        if (!convertedQuery.equals(query)) {
            matchQueries.add(MultiMatchQuery.of(m -> m.query(convertedQuery)
                    .type(TextQueryType.BoolPrefix)
                    .fields("name.auto_complete", "name.auto_complete._2gram", "name.auto_complete._3gram")
            )._toQuery());
        }

        // 3. 자소 분해 매칭 (nameJaso 필드)
        matchQueries.add(MatchBoolPrefixQuery.of(m -> m
                .field("nameJaso")
                .query(jasoQuery)
        )._toQuery());

        // 4. 한영 변환 후 자소 분해 매칭 (nameJaso 필드)
        if (!convertedJasoQuery.equals(jasoQuery)) {
            matchQueries.add(MatchBoolPrefixQuery.of(m -> m
                    .field("nameJaso")
                    .query(convertedJasoQuery)
            )._toQuery());
        }

        Query mainMatchQuery = BoolQuery.of(b -> b
                .should(matchQueries)
                .minimumShouldMatch("1")
        )._toQuery();

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(mainMatchQuery)
                .withPageable(PageRequest.of(0, 5))
                .build();

        SearchHits<ProductDocument> searchHits = this.elasticsearchOperations.search(nativeQuery, ProductDocument.class);

        return searchHits.getSearchHits().stream()
                .map(hit -> {
                    ProductDocument content = hit.getContent();
                    return content.getName();
                })
                .toList();

    }

    public List<ProductDocument> searchProducts(String query, String category, double minPrice, double maxPrice, int page, int size, String sortBy) {
        String convertedQuery = HangulKeyConverter.convert(query);
        String jasoQuery = JasoDecomposer.decompose(query);
        String convertedJasoQuery = JasoDecomposer.decompose(convertedQuery);

        List<Query> matchQueries = new ArrayList<>();

        // 1. 원본 검색어 매칭 (일반 텍스트 필드)
        matchQueries.add(MultiMatchQuery.of(m -> m
                .query(query)
                .fields("name^3", "description^1", "category^2")
                .fuzziness("AUTO")
        )._toQuery());

        // 2. 한영 오타 변환된 검색어 매칭 (일반 텍스트 필드)
        if (!convertedQuery.equals(query)) {
            matchQueries.add(MultiMatchQuery.of(m -> m
                    .query(convertedQuery)
                    .fields("name^3", "description^1", "category^2")
                    .fuzziness("AUTO")
            )._toQuery());
        }

        // 3. 자소 분해 매칭 (nameJaso 필드)
        matchQueries.add(MatchQuery.of(m -> m
                .field("nameJaso")
                .query(jasoQuery)
                .fuzziness("AUTO")
        )._toQuery());

        // 4. 한영 변환 후 자소 분해 매칭 (nameJaso 필드)
        if (!convertedJasoQuery.equals(jasoQuery)) {
            matchQueries.add(MatchQuery.of(m -> m
                    .field("nameJaso")
                    .query(convertedJasoQuery)
                    .fuzziness("AUTO")
            )._toQuery());
        }

        Query mainMatchQuery = BoolQuery.of(b -> b
                .should(matchQueries)
                .minimumShouldMatch("1")
        )._toQuery();

        List<Query> filters = new ArrayList<>();
        if(category !=null && !category.isEmpty()) {
            Query categoryFilter = TermQuery.of(t -> t
                    .field("category.raw")
                    .value(category)
            )._toQuery();
            filters.add(categoryFilter);
        }

        Query priceRangeFilter = NumberRangeQuery.of(r -> r
                .field("price")
                .gte(minPrice)
                .lt(maxPrice)
        )._toRangeQuery()._toQuery();
        filters.add(priceRangeFilter);

        Query ratingShould = NumberRangeQuery.of(r -> r
                .field("rating")
                .gt(4.0)
        )._toRangeQuery()._toQuery();

        Query boolQuery = BoolQuery.of(b -> b
                .must(mainMatchQuery)
                .filter(filters)
                .should(ratingShould)
        )._toQuery();

        HighlightParameters highlightParameters = HighlightParameters.builder()
                .withPreTags("<b>")
                .withPostTags("</b>")
                .build();

        Highlight highlight = new Highlight(highlightParameters, List.of(new HighlightField("name")));
        HighlightQuery highlightQuery = new HighlightQuery(highlight, ProductDocument.class);

        Sort sort = Sort.unsorted();
        if ("price_asc".equals(sortBy)) {
            sort = Sort.by(Sort.Order.asc("price"), Sort.Order.desc("_score"));
        } else if ("price_desc".equals(sortBy)) {
            sort = Sort.by(Sort.Order.desc("price"), Sort.Order.desc("_score"));
        } else if ("rating_desc".equals(sortBy)) {
            sort = Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("_score"));
        }

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withHighlightQuery(highlightQuery)
                .withPageable(PageRequest.of(page - 1, size, sort))
                .build();
        SearchHits<ProductDocument> searchHits = this.elasticsearchOperations.search(nativeQuery, ProductDocument.class);

        return searchHits.getSearchHits().stream()
                .map(hit-> {
                    ProductDocument content = hit.getContent();
                    List<String> highlightFields = hit.getHighlightField("name");
                    if (highlightFields != null && !highlightFields.isEmpty()) {
                        content.setName(highlightFields.get(0));
                    }
                    return content;
                    }
                ).toList();
    }

    public int reindexProducts(boolean reset, int chunkSize) {
        if (reset) {
            IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
            if (indexOps.exists()) {
                indexOps.delete();
            }
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping());
        }

        int totalCount = 0;
        int page = 0;
        boolean hasNext = true;

        while (hasNext) {
            Pageable pageable = PageRequest.of(page, chunkSize);
            Page<Product> productPage = productRepository.findAll(pageable);
            List<Product> products = productPage.getContent();

            if (products.isEmpty()) {
                break;
            }

            List<ProductDocument> documents = products.stream()
                    .map(p -> new ProductDocument(
                            p.getId().toString(),
                            p.getName(),
                            p.getDescription(),
                            p.getPrice(),
                            p.getRating(),
                            p.getCategory(),
                            JasoDecomposer.decompose(p.getName())
                    ))
                    .toList();

            productDocumentRepository.saveAll(documents);
            totalCount += documents.size();

            hasNext = productPage.hasNext();
            page++;
        }

        return totalCount;
    }
}
