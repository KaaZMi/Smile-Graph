package model;

import java.util.ArrayList;

public class Hypothesis {
	private ArrayList<Prototype> prototypes;
	private boolean consistent;
	private int id;

	public Hypothesis(ArrayList<Prototype> prototypes, boolean consistent) {
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
}
