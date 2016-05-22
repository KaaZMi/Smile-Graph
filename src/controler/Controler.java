package controler;

import model.Model;
import view.Window;

public class Controler {

	private Window view = null;
	private Model model;
	
	private boolean authorization = true; // authorization to continue the scenario execution

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
		model.setCursor(model.getCursor()+1);
		control();
	}

	public void decrementCursor() {
		model.setCursor(model.getCursor()-1);
		control();
	}

	public void resetCursor() {
		model.setCursor(0);
		control();
	}

	public void control() {
		if (view != null) {
			if (model.getCursor() >= model.getEvents().size()) {
				view.enableWarning("No more events.");
				setAuthorization(false);
			}
			else if (model.getCursor() < 0) {
				view.enableWarning("Already at the beginning.");
				setAuthorization(false);
			}
			else {
				setAuthorization(true);
			}
		}
	}

	public boolean hasAuthorization() {
		return authorization;
	}

	public void setAuthorization(boolean authorization) {
		this.authorization = authorization;
	}

}
