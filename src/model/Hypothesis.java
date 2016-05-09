package model;

import java.util.ArrayList;

public class Hypothesis {
	private ArrayList<Prototype> prototypes;
	private boolean consistent;

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

	@Override
	public String toString() {
		return "Hypothesis [prototypes=" + prototypes + ", consistent=" + consistent + "]";
	}
}
