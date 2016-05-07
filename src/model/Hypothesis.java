package model;

import java.util.ArrayList;

public class Hypothesis {
	private ArrayList<Prototype> prototypes;

	public Hypothesis(ArrayList<Prototype> prototypes) {
		this.prototypes = prototypes;
	}

	public ArrayList<Prototype> getPrototypes() {
		return prototypes;
	}

	public void setPrototypes(ArrayList<Prototype> prototypes) {
		this.prototypes = prototypes;
	}

	@Override
	public String toString() {
		return "Hypothesis [prototypes=" + prototypes + "]";
	}
}
