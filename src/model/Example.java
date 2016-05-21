package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents both an example or counter-example
 */
public class Example extends Prototype implements Serializable {
	private static final long serialVersionUID = -4553311091709140833L;
	private List<String> tags;
	private int id;

	public Example(int id, List<String> atoms, List<String> tags) {
		super(atoms);
		this.id = id;
		this.tags = tags;
	}

	public Example(Example another) {
		super(another.getAtoms());
		this.id = another.getId();
		this.tags = another.getTags();
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Example [id=" + id + ", atoms=" + super.getAtoms() + ", tags=" + tags + "]";
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
