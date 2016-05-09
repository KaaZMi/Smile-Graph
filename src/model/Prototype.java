package model;

import java.util.List;

public class Prototype {
	private List<String> atoms;
	
	public Prototype(List<String> atoms) {
		setAtoms(atoms);
	}

	public List<String> getAtoms() {
		return atoms;
	}

	public void setAtoms(List<String> atoms) {
		this.atoms = atoms;
	}
	
	public boolean compareTo(Prototype prototype) {
		return this.atoms.equals(prototype.getAtoms());
	}

	@Override
	public String toString() {
		return "Prototype [atoms=" + atoms + "]";
	}

}
