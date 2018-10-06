package hello.batch;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import hello.module.repository.Person;
import hello.module.service.PeopleService;

public class PersonItemWriter implements ItemWriter<Person> {
	private final PeopleService service;
	public PersonItemWriter (PeopleService service) {
		this.service = service;
	}
	
	@Override
	public void write(List<? extends Person> items) throws Exception {
		for (Person person : items) service.insert(person);
	}

}
