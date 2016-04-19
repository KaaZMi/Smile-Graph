package controler;

import model.Model;
import view.Window;

public class Controler {
	
	private Window view = null;
	private Model model;
	
	
	public Controler(Model mod){
		this.model = mod;
	}

	public boolean openXML(String path) {
		// ...
		// TODO vérifier validité du chemin (est-ce que c'est bien un fichier xml, ...) 
		// ...
		return model.openXML(path);
	}
	
	public boolean openLOG(String path) {
		return model.openLOG(path);
	}
	
	public void addView(Window view) {
		this.view = view ;
	}

	public void incrementCursor() {
		model.setCursor((model.getCursor()+1));
		control();
	}
	
	public void decrementCursor() {
		model.setCursor((model.getCursor()-1));
		control();
	}
	
	public void resetCursor() {
		model.setCursor(0);
		control();
	}

	public void control() {
		if ( view != null ) {
			if ( model.getCursor() > model.getEvents().size()) {
				// TODO : gérer affichage des erreurs dans la vue
			}
		}
	}

}
