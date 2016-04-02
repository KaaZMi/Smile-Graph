package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.*;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import controler.Controler;
import model.Model;

@SuppressWarnings("serial")
public class Fenetre extends JFrame implements Observer, ViewerListener {
	protected boolean loop = true;
	protected ViewerPipe fromViewer = null;

	private JMenuBar menu = null;
	private JMenu fichier = null;
	private JMenuItem ouvrirXML = null;
	private JMenuItem ouvrirLOG = null;
	private JMenuItem enregistrer = null;
	private JMenuItem quitter = null;
	private JMenu help = null;
	private JMenuItem apropos = null;
	private JPanel conteneur = new JPanel();
	private JFileChooser fileChooser = null;

	private Controler controler;
	private Model model;

	private Graph graph = null;
	private LinkedHashMap<Integer, ArrayList<String>> events = new LinkedHashMap<Integer, ArrayList<String>>();

	public Fenetre(Controler controler, Model model){
		this.controler = controler;
		this.model = model;
		this.model.addObserver(this); // adding the window as an observer of the model

		initFrame();
		initMenuBar();
		initToolBar();

		setVisible(true);
	}

	private void initFrame() {
		setTitle("Smile Graph");
		setSize(new Dimension(1600,900));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); // window at the center
		setResizable(true);
		conteneur.setBackground(Color.red); // DEV
		setContentPane(this.conteneur); // warn our JFrame that the JPanel will constitute its content pane
		getContentPane().setLayout(new BorderLayout()); // choosing the layout manager
	}

	private void initMenuBar() {
		menu = new JMenuBar();

		fichier = new JMenu("Fichier");
		fichier.setMnemonic('f');

		ouvrirXML = new JMenuItem("Ouvrir XML");
		ouvrirXML.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				fileChooser = new JFileChooser();
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String path = fileChooser.getSelectedFile().getAbsolutePath();
					controler.openXML(path);
				}
				conteneur.revalidate();
			}	    	
		});

		ouvrirLOG = new JMenuItem("Ouvrir scénario");
		ouvrirLOG.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				fileChooser = new JFileChooser();
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String path = fileChooser.getSelectedFile().getAbsolutePath();
					controler.openLOG(path);
				}
				conteneur.revalidate();
			}	    	
		});

		enregistrer = new JMenuItem("Enregistrer");
		enregistrer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				conteneur.removeAll();
				System.out.println("Enregistrer");
				conteneur.revalidate();
			}
		});

		quitter = new JMenuItem("Quitter");
		quitter.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});

		fichier.add(ouvrirXML);
		fichier.add(ouvrirLOG);
		fichier.add(enregistrer);
		fichier.addSeparator();
		fichier.add(quitter);

		help = new JMenu("Aide");
		help.setMnemonic('o');

		apropos = new JMenuItem("A propos");
		apropos.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JOptionPane.showMessageDialog(null,
						"Créateurs : ... & ...\nLicence : Freeware\nCopyright : ...@....com",
						"Informations", JOptionPane.NO_OPTION);
				conteneur.removeAll();
				conteneur.revalidate();
			}
		});

		help.add(apropos);

		menu.add(fichier);
		menu.add(help);
	}

	private void initToolBar() {
		JToolBar toolbar = new JToolBar();

		JButton prevButton = new JButton(new ImageIcon("playback_prev_icon&16.png"));
		prevButton.setFocusPainted(false);
		toolbar.add(prevButton);
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("PREV");
			}
		});

		JButton playButton = new JButton(new ImageIcon("playback_play_icon&16.png"));
		toolbar.add(playButton);
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// scenario execution in a separate thread
				new Thread() {
					public void run() {
						//for (Entry<Integer, ArrayList<String>> entry : events.entrySet()) {
						for (int cursor = model.getCursor() ; cursor < model.getEvents().keySet().size() ; cursor++) {
							// We need to call the pump() method before each use 
							// of the graph to copy back events that have already 
							// occurred in the viewer thread inside our thread.
							fromViewer.pump();
							System.out.println(cursor + "/" + model.getEvents().get(cursor));
							String i = model.getEvents().get(cursor).get(1);
							String j = model.getEvents().get(cursor).get(2);
							String id = "";
							if (Integer.parseInt(i) < Integer.parseInt(j)) {
								id = i+"-"+j+"-"+j+"-"+i;
							}
							else {
								id = j+"-"+i+"-"+i+"-"+j;
							}
							System.out.println(id);
							graph.getEdge(id).setAttribute("ui.class", model.getEvents().get(cursor).get(0));
							model.setCursor(cursor);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
				}.start();
			}
		});

		JButton nextButton = new JButton(new ImageIcon("playback_next_icon&16.png"));
		toolbar.add(nextButton);
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("NEXT");
			}
		});

		JButton stopButton = new JButton(new ImageIcon("playback_stop_icon&16.png"));
		toolbar.add(stopButton);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("STOP");
			}
		});

		toolbar.setFloatable(false);

		this.setJMenuBar(menu); // add the menu to the JFrame
		conteneur.add(toolbar, BorderLayout.NORTH); // add the toolbar to the main panel
	}

	@Override
	public void update(Observable obs, Object obj) {

		// initialization and displaying the original graph in the window
		if (graph == null) {
			graph = new MultiGraph("embedded");
			graph.setStrict(false);
			graph.setAutoCreate(true); // automatic creation of nodes depending edges
			graph.addAttribute("ui.antialias"); // graphics smoothing
			graph.addAttribute("ui.quality");
			graph.addAttribute("ui.stylesheet", styleSheet); // CSS style of the graph

			LinkedHashMap<String, ArrayList<String>> edges = model.getEdges();
			for (Entry<String, ArrayList<String>> entry : edges.entrySet()) {
				graph.addEdge(entry.getKey(), entry.getValue().get(0), entry.getValue().get(1), false); // false = not oriented
			}
			for (Node node : graph) {
				node.addAttribute("ui.label", node.getId());
			}

			Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
			viewer.enableAutoLayout();
			ViewPanel view = viewer.addDefaultView(false); // false indicates "no JFrame"

			// IMPORTANT !
			// We connect back the viewer to the graph,
			// the graph becomes a sink for the viewer.
			// We also install us as a viewer listener 
			// to intercept the graphic events.
			fromViewer = viewer.newViewerPipe();
			fromViewer.addViewerListener(this);
			fromViewer.addSink(graph);

			// TODO : MouseWheelListener pour le zoom au lieu de JSlider (priorité minimal)
			JSlider slider = new JSlider();
			slider.addChangeListener(e -> view.getCamera().setViewPercent(slider.getValue() / 10.0));
			slider.setBackground(Color.white);
			conteneur.add((Component) view, BorderLayout.CENTER);
			conteneur.add(slider, BorderLayout.SOUTH);
		}

		else {
			events = model.getEvents();
			System.out.println(events.size());
		}

	}

	public void viewClosed(String id) {
		loop = false;
	}

	public void buttonPushed(String id) {
		System.out.println("Noeud " + id + " attrapé");
	}

	public void buttonReleased(String id) {
		System.out.println("Noeud " + id + " relâché");
	}
	
	// TODO : mettre au point le style (juste un test pour l'instant)
	// TODO : il faudra mettre ça au propre dans un fichier peut-être
	protected static String styleSheet =
			"edge.red {"+
			"	fill-color: red;"+
			"}"+
			"edge.blue {"+
			"	fill-color: blue;"+
			"}"+
			"edge.purple {"+
			"	fill-color: purple;"+
			"}"+
			"edge.yellow {"+
			"	fill-color: yellow;"+
			"}"+
			"edge.green {"+
			"	fill-color: green;"+
			"}"+
			"edge.orange {"+
			"	fill-color: orange;"+
			"}"
	;

}
