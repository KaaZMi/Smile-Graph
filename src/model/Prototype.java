package model;

import java.util.ArrayList;
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
		List<String> atoms1 = new ArrayList<String>(this.getAtoms());
		List<String> atoms2 = new ArrayList<String>(prototype.getAtoms());
		
		if(atoms1 != null && atoms2 != null && (atoms1.size() == atoms2.size())) {
			atoms1.removeAll(atoms2);
            if(atoms1.isEmpty()) {
                return true;
            }
        }
		
		return false;
	}

	@Override
	public String toString() {
		return "Prototype [atoms=" + atoms + "]";
	}

}
