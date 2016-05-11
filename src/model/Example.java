package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents both an example or counter-example
 */
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

	/**
	 * Compare this example to another according to their tags
	 * @param tags
	 * @return true if they're equal, otherwise false if they're not equal
	 */
	public boolean compareTo(List<String> tags) {	
		List<String> tags1 = new ArrayList<String>(this.getTags());
		List<String> tags2 = new ArrayList<String>(tags);

		if(tags1 != null && tags2 != null && (tags1.size() == tags2.size())) {
			tags1.removeAll(tags2);
			if(tags1.isEmpty()) {
				return true;
			}
		}

		return false;
	}

}
