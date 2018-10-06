package hello.batch.config;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.HibernatePagingItemReader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.autoconfigure.batch.JpaBatchConfigurer;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import hello.JobCompletionNotificationListener;
import hello.PersonItemProcessor;
import hello.PersonItemWriter;
import hello.module.repository.Person;
import hello.module.service.PeopleService;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    // tag::readerwriterprocessor[]
    /*@Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
            .name("personItemReader")
            .resource(new ClassPathResource("sample-data.csv"))
            .delimited()
            .names(new String[]{"firstName", "lastName"})
            .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }})
            .build();
    }*/
    
    @Bean
    @StepScope
    public HibernatePagingItemReader<Person> personReader(
    	EntityManagerFactory factory,
    	@Value("#{jobParameters['limit']}") Integer limit) {
    	HibernatePagingItemReader<Person> reader = new HibernatePagingItemReader<>();
    	
    	reader.setQueryString("FROM Person ORDER BY person_id DESC");
    	reader.setFetchSize(10);
    	reader.setPageSize(5);
    	reader.setSessionFactory(factory.unwrap(SessionFactory.class));
    	reader.setSaveState(false);
    	
    	return reader;
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }
    
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importUserJob")
            .incrementer(new RunIdIncrementer())
            .listener(listener)
            .flow(step1)
            .end()
            .build();
    }

    @Bean
    public Step step1(HibernatePagingItemReader<Person> personReader, PeopleService service) {
        return stepBuilderFactory.get("step1")
            .<Person, Person> chunk(10)
            .reader(personReader)
            .processor(processor())
            .writer(new PersonItemWriter(service))
            .build();
    }
    // end::jobstep[]
    @Bean
    public CrystalBatchConfigurer configurer(BatchProperties properties, DataSource dataSource, @Qualifier("factory") LocalContainerEntityManagerFactoryBean factory,
    		ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
    	return new CrystalBatchConfigurer(properties, dataSource, transactionManagerCustomizers.getIfAvailable(), factory.getObject());
    }
    
    public class CrystalBatchConfigurer extends JpaBatchConfigurer {
		protected CrystalBatchConfigurer(BatchProperties properties, DataSource dataSource,
				TransactionManagerCustomizers transactionManagerCustomizers,
				EntityManagerFactory entityManagerFactory) {
			super(properties, dataSource, transactionManagerCustomizers, entityManagerFactory);
		}
    }
    
    @Bean
    public JobExplorer jobExplorer(DataSource dataSource) throws Exception {
    	JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
    	factory.setDataSource(dataSource);
    	factory.afterPropertiesSet();
    	return factory.getObject();
    }
}