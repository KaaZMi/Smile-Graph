package model;

import java.io.Serializable;
import java.util.ArrayList;

public class Hypothesis implements Serializable {
	private static final long serialVersionUID = -1460481349508886149L;
	private ArrayList<Prototype> prototypes;
	private boolean consistent;
	private int id;

	public Hypothesis(ArrayList<Prototype> prototypes, boolean consistent) {
		this.id = -1;
		this.prototypes = prototypes;
		this.consistent = consistent;
	}

	public ArrayList<Prototype> getPrototypes() {
		return prototypes;
	}

	public void setPrototypes(ArrayList<Prototype> prototypes) {
		this.prototypes = prototypes;
	}

	public boolean isConsistent() {
		return consistent;
	}

	public void setConsistent(boolean consistent) {
		this.consistent = consistent;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Hypothesis [id=" + id + ", prototypes=" + prototypes + ", consistent=" + consistent + "]";
	}

	public boolean compareTo(Hypothesis h) {
		int nb_equals = 0;
		for (Prototype p1 : h.getPrototypes())
			for (Prototype p2 : this.getPrototypes())
				if (p1.compareTo(p2))
					nb_equals++;

		if (nb_equals*2 == h.getPrototypes().size()+this.getPrototypes().size())
			return true;

		return false;
	}
}
