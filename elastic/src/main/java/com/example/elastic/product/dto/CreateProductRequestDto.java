package com.example.elastic.product.dto;

import jakarta.persistence.Column;

public class CreateProductRequestDto {
    private String name;
    private String description; // 에디터가 HTML로 상품 설명을 저장
    private int price;
    private double rating;
    private String category;

    public CreateProductRequestDto(String name, String description, int price, double rating, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }

    public String getCategory() {
        return category;
    }
}
