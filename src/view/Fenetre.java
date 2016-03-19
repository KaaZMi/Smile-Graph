package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;

import controler.Controler;
import model.Model;

@SuppressWarnings("serial")
public class Fenetre extends JFrame implements Observer{
	
	//TEST TEST
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

	public Fenetre(Controler controler, Model model){
		this.controler = controler;
		this.model = model;
		
		this.model.addObserver(this); // ajout de la fenêtre comme observer du modèle
		this.graph = new MultiGraph("embedded");
		this.graph.setStrict(false);
		this.graph.setAutoCreate(true);
		this.graph.addAttribute("ui.antialias");
		
		initFrame();
		initMenuBar();
		initToolBar();
		
		this.setVisible(true);
	}

	private void initFrame() {
		this.setTitle("Smile Graph");
		this.setSize(new Dimension(1600,900));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null); // fenêtre centrée
		this.setResizable(true);
		this.conteneur.setBackground(Color.red); // temporaire
		this.setContentPane(this.conteneur); // avertir notre JFrame que ce sera notre JPanel qui constituera son content pane
		this.getContentPane().setLayout(new BorderLayout()); // choix du gestionnaire d'agencement
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
		
		//TODO: voir pourquoi le jfilechooser ne fonctionne pas ici
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
				//conteneur.add(new ScorePanel(size, model.getScores()).getPanel(), BorderLayout.CENTER);
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
				System.out.println("PLAY");
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
		
		this.setJMenuBar(menu); // menu propre au JFrame
		this.conteneur.add(toolbar, BorderLayout.NORTH); // menu propre au content pane
	}

	@Override
	public void update(Observable obs, Object obj) {
		
		// initialisation et affichage du graphe original dans la fenetre
		if (this.graph.getEdgeCount() == 0) {
			Graph g = this.model.getGraph();
			Viewer viewer = new Viewer(g, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		    DefaultView view = (DefaultView) viewer.addDefaultView(false); // false indicates "no JFrame"
		    //view.setPreferredSize(new Dimension(1400, 700));
		    JSlider slider = new JSlider();
		    slider.addChangeListener(e -> view.getCamera().setViewPercent(slider.getValue() / 10.0));
		    slider.setBackground(Color.white);
		    viewer.enableAutoLayout();
		    this.conteneur.add(view, BorderLayout.CENTER);
		    this.conteneur.add(slider, BorderLayout.SOUTH);
		}
		
	}

}
