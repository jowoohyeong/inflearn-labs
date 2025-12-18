package com.example.elastic.product.domain;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.*;

import static org.springframework.data.elasticsearch.annotations.FieldType.*;

@Document(indexName = "products")
@Setting(settingPath = "/elasticsearch/product-settings.json")
@Getter @Setter
public class ProductDocument {
    @Id
    private String id;

    @MultiField(mainField = @Field(type = Text, analyzer = "products_name_analyzer"),
        otherFields = {
            @InnerField(suffix = "auto_complete", type = Search_As_You_Type, analyzer = "nori")
        }
    )
    private String name;

    @Field(type = Text, analyzer = "products_description_analyzer")
    private String description;
    @Field(type = Integer)
    private int price;
    @Field(type = Double)
    private double rating;
    @MultiField(mainField = @Field(type = Text, analyzer = "products_category_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = Keyword)
        }
    )
    private String category;

    public ProductDocument(String id, String name, String description, int price, double rating, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.category = category;
    }
}
