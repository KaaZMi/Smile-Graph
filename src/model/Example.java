package model;

import java.util.List;

public class Example extends Prototype {
	private List<String> tags;

	public Example(List<String> atoms, List<String> tags) {
		super(atoms);
		this.tags = tags;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "Example [tags=" + tags + ", atoms=" + super.getAtoms() + "]";
	}
	
	public boolean compareTo(List<String> tags) {
		return this.tags.equals(tags);
	}

}
