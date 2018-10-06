package hello;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import hello.module.repository.Person;
import hello.module.service.PeopleService;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SampleTest {
	@Autowired
	private PeopleService peopleService;
	
	@Test
	public void insertTest() {
		Person person = new Person();
		person.setFirstName("crystal");
		person.setLastName("New");
		peopleService.insert(person);
	}
}
