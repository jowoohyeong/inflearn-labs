package study.data_jpa

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class DataJpaApplication {

	static void main(String[] args) {
		SpringApplication.run(DataJpaApplication, args)
	}

	@Bean
	public AuditorAware<String> auditorAware() {
		return () -> Optional.of(UUID.randomUUID().toString());
	}
}
