package model;

import java.util.ArrayList;

public class Prototype {
	private ArrayList<String> atoms;
	
	public Prototype(ArrayList<String> atoms) {
		setAtoms(atoms);
	}

	public ArrayList<String> getAtoms() {
		return atoms;
	}

	public void setAtoms(ArrayList<String> atoms) {
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
