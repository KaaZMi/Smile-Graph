package controler;

import java.io.IOException;

import model.Model;
import view.Fenetre;

public class Controler {
	
	private Fenetre fen = null;
	private Model model;
	
	
	public Controler(Model mod){
		this.model = mod;
	}
	
	public void openLOG(String path) throws IOException {
		
		//ouvrir le fichier log et retourner la version stock�e en m�moire
		this.model.openLOG(path);
		
		
	}

	public void openXML(String path) {
		// ...
		// TODO v�rifier validit� du chemin (est-ce que c'est bien un fichier xml, ...) 
		// ...
		this.model.openXML(path);
	}
	
	public void addView(Fenetre fen) {
		this.fen = fen ;
	}

}
