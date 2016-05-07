package model;

import java.util.ArrayList;

public class Example extends Prototype {
	private ArrayList<String> tags;

	public Example(ArrayList<String> atoms, ArrayList<String> tags) {
		super(atoms);
		this.tags = tags;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "Example [tags=" + tags + ", atoms=" + super.getAtoms() + "]";
	}

}
