package hello.batch.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.springframework.orm.jpa.JpaVendorAdapter;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages="hello.batch.repository", entityManagerFactoryRef="factory")
public class JpaConfig {
	@Bean
	public LocalContainerEntityManagerFactoryBean factory(JpaVendorAdapter jpaAdapter, DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setPackagesToScan("hello.module.repository");
		factory.setPersistenceUnitName("crystal_batch");
		factory.setJpaVendorAdapter(jpaAdapter);
		factory.setDataSource(dataSource);
		
		return factory;
	}
}
