import controler.*;
import model.*;
import view.*;

public class Main{
	public static void main(String[] args) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		Model model = new Model();
		Controler controler = new Controler(model);
		Window view = new Window(controler, model);
		controler.addView(view);
	}
}
