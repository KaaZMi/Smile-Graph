import controler.*;
import model.*;
import view.*;

public class Main{
	public static void main(String[] args) {
		Model model = new Model(); // Instanciation de notre modèle
		Controler controler = new Controler(model);// Création du contrôleur
		Fenetre fen = new Fenetre(controler, model); // Création de notre fenêtre avec le contrôleur en paramètre
		controler.addView(fen);
	}
	
	//THIS IS ONLY A TEST!
	// TEST ********
}
