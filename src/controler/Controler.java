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
		if (view != null) {
			if (model.getCursor() > model.getEvents().size()) {
				view.enableWarning("Nombre d'événements dépassé.");
			}
			else if (model.getCursor() < 0) {
				view.enableWarning("Retour impossible.");
			}
			else {
				view.disableWarning();
			}
		}
	}

}
