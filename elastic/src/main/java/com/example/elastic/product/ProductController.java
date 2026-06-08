package com.example.elastic.product;

import com.example.elastic.product.domain.Product;
import com.example.elastic.product.domain.ProductDocument;
import com.example.elastic.product.dto.CreateProductRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping()
    public ResponseEntity<List<Product>> getProducts(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        List<Product> products = productService.getProducts(page, size);
        return ResponseEntity.ok(products);
    }


    @PostMapping()
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequestDto createProductRequestDto) {
        Product product = productService.createProduct(createProductRequestDto);
        return ResponseEntity.ok(product);
    }
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String query) {
        List<String> suggestions = productService.getSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDocument>> searchProducts(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0")  double minPrice,
            @RequestParam(defaultValue = "100000000")  double maxPrice,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "5")  int size,
            @RequestParam(required = false) String sortBy
    ) {
        List<ProductDocument> products = productService.searchProducts(query, category, minPrice, maxPrice, page, size, sortBy);
        return ResponseEntity.ok(products);
    }


    @PostMapping("/reindex")
    public ResponseEntity<String> reindexProducts(
            @RequestParam(defaultValue = "false") boolean reset,
            @RequestParam(defaultValue = "1000") int chunkSize
    ) {
        int count = productService.reindexProducts(reset, chunkSize);
        return ResponseEntity.ok(count + " 개의 상품이 성공적으로 재색인되었습니다.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
