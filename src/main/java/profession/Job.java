package profession;

import java.util.LinkedList;
import java.util.List;

public class Job {

	private String title, salary, location, link, description;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSalary() {
		return salary;
	}

	public void setSalary(String salary) {
		this.salary = salary;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getListOfProps() {
		List<String> listOfProps = new LinkedList<>();
		listOfProps.add(title);
		listOfProps.add(salary);
		listOfProps.add(location);
		listOfProps.add(link);
		listOfProps.add(description);
		return listOfProps;
	}

	@Override
	public String toString() {
		return "Job [title=" + title + ", salary=" + salary + ", location=" + location + ", link=" + link + ", description=" + description
				+ "]";
	}

}
