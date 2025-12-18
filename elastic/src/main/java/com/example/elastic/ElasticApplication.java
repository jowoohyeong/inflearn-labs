package com.example.elastic;

import com.example.elastic.product.ProductDocumentRepository;
import com.example.elastic.user.UserDocumentRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
public class ElasticApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElasticApplication.class, args);
	}

}
