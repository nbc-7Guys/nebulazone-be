package nbc.chillguys.nebulazone.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@TestConfiguration
public class TestQuerydslConfig {
	@Bean
	public JPAQueryFactory jpaQueryFactory(EntityManager em) {
		return Mockito.mock(JPAQueryFactory.class);
	}
}
