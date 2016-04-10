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

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.graphicGraph.stylesheet.Values;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteFactory;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.*;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import controler.Controler;
import model.Model;

@SuppressWarnings("serial")
public class Window extends JFrame implements Observer, ViewerListener {
	private boolean loop = true;
	private ViewerPipe fromViewer = null;

	private JMenuBar menu = null;
	private JMenu file = null;
	private JMenuItem openXML = null;
	private JMenuItem openLOG = null;
	private JMenuItem save = null;
	private JMenuItem exit = null;
	private JMenu options = null;
	private JCheckBoxMenuItem autolayout = null;
	private JMenu help = null;
	private JMenuItem about = null;
	private JPanel container = null;
	private JPanel side_panel = null;
	private JFileChooser fileChooser = null;
	private JLabel display = new JLabel();

	private Controler controler;
	private Model model;

	private Graph graph = null;
	private Viewer viewer = null;
	private LinkedHashMap<Integer, ArrayList<String>> events = new LinkedHashMap<Integer, ArrayList<String>>();
	private Thread scenario_execution = null;
	private SpriteManager sman = null;
	private boolean scenario_reprise = false;

	public Window(Controler controler, Model model){
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
		container = new JPanel();
		container.setBackground(Color.red); // DEV
		setContentPane(this.container); // warn our JFrame that the JPanel will constitute its content pane
		getContentPane().setLayout(new BorderLayout()); // choosing the layout manager
		
		side_panel = new JPanel();
		side_panel.setPreferredSize(new Dimension(300, container.getHeight()));
		String html = "<html>\n"
                  + "<body>\n"
                  + "<h1>Details</h1>\n"
                  + "<h2>Graph ?</h2>\n"
                  + "<p>" + model.isGraphLoaded() + "</p>\n"
                  + "<h2>Scenario ?</h2>\n"
                  + "<p>" + model.isScenarioLoaded() + "</p>\n"
                  + "<h2>Number of agents</h2>\n"
                  + "<p>" + model.getNbAgents() + "</p>\n"
                  + "</body>\n"
                  + "</html>";
		setDisplay(html);
		side_panel.add(display);
		
		container.add(side_panel, BorderLayout.EAST);
	}

	private void initMenuBar() {
		menu = new JMenuBar();

		file = new JMenu("File");

		openXML = new JMenuItem("Open a graph");
		openXML.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				fileChooser = new JFileChooser();
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String path = fileChooser.getSelectedFile().getAbsolutePath();
					if(controler.openXML(path)) {
						graph = new MultiGraph("embedded");
						graph.setStrict(false);
						graph.setAutoCreate(true); // automatic creation of nodes depending edges
						graph.addAttribute("ui.antialias"); // graphics smoothing
						graph.addAttribute("ui.quality");
						graph.addAttribute("ui.stylesheet", styleSheet); // CSS style of the graph
						sman = new SpriteManager(graph);
						sman.setSpriteFactory(new mySpritesFactory());
	
						LinkedHashMap<String, ArrayList<String>> edges = model.getEdges();
						for (Entry<String, ArrayList<String>> entry : edges.entrySet()) {
							graph.addEdge(entry.getKey(), entry.getValue().get(0), entry.getValue().get(1), false); // false = not oriented
						}
						for (Node node : graph) {
							node.addAttribute("ui.label", node.getId());
						}
	
						viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
						viewer.enableAutoLayout();
						ViewPanel view_panel = viewer.addDefaultView(false); // false indicates "no JFrame"
	
						/* IMPORTANT !
						 * We connect back the viewer to the graph,
						 * the graph becomes a sink for the viewer.
						 * We also install us as a viewer listener 
						 * to intercept the graphic events.
						 */
						fromViewer = viewer.newViewerPipe();
						fromViewer.addSink(graph);
						fromViewer.addViewerListener(Window.this);
						fromViewer.pump();
						
						container.add((Component) view_panel, BorderLayout.CENTER);
					}
				}
				container.revalidate();
			}	    	
		});

		openLOG = new JMenuItem("Open a scenario");
		openLOG.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				fileChooser = new JFileChooser();
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String path = fileChooser.getSelectedFile().getAbsolutePath();
					if(controler.openLOG(path)) {
						events = model.getEvents();
						System.out.println(events.size());
					}
				}
				container.revalidate();
			}	    	
		});

		save = new JMenuItem("Save");
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				container.removeAll();
				System.out.println("Enregistrer");
				container.revalidate();
			}
		});

		exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});

		file.add(openXML);
		file.add(openLOG);
		file.add(save);
		file.addSeparator();
		file.add(exit);
		
		options = new JMenu("Options");

		autolayout = new JCheckBoxMenuItem("Auto-layout");
		autolayout.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (((AbstractButton) e.getSource()).getModel().isSelected())
					viewer.disableAutoLayout();
				else
					viewer.enableAutoLayout();
			}
		});

		options.add(autolayout);

		help = new JMenu("Help");

		about = new JMenuItem("About");
		about.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JOptionPane.showMessageDialog(null,
						"Créateurs : ... & ...\nLicence : Freeware\nCopyright : ...@....com",
						"Informations", JOptionPane.NO_OPTION);
				container.removeAll();
				container.revalidate();
			}
		});

		help.add(about);

		menu.add(file);
		menu.add(options);
		menu.add(help);
	}

	private void initToolBar() {
		JToolBar toolbar = new JToolBar();

		JButton prevButton = new JButton(new ImageIcon("playback_prev_icon&16.png"));
		prevButton.setFocusPainted(false);
		toolbar.add(prevButton);
		prevButton.addActionListener(new ActionListener() {
			// TODO : revenir en arrière est beaucoup plus complexe
			public void actionPerformed(ActionEvent e) {
				if(model.getCursor() > 0) {
					controler.decrementCursor();
					int cursor = model.getCursor();
					/* We need to call the pump() method before each use 
					 * of the graph to copy back events that have already 
					 * occurred in the viewer thread inside our thread.
					 */
					fromViewer.pump();
					String i = model.getEvents().get(cursor).get(1);
					String j = model.getEvents().get(cursor).get(2);
					String id = "";
					if (Integer.parseInt(i) < Integer.parseInt(j)) {
						id = i+"-"+j+"-"+j+"-"+i;
					}
					else {
						id = j+"-"+i+"-"+i+"-"+j;
					}
					System.out.println("----------------");
					System.out.println(cursor + "/" + model.getEvents().get(cursor));
					System.out.println(id);
					System.out.println("----------------");
					graph.getEdge(id).setAttribute("ui.class", model.getEvents().get(cursor).get(0));
				}
			}
		});

		JButton playButton = new JButton(new ImageIcon("playback_play_icon&16.png"));
		toolbar.add(playButton);
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// scenario execution in a separate thread
				scenario_execution = new Thread() {
					public void run() {
						int cursor = 0;
						boolean direction = true;
						double pos = 0;
						String i = null;
						String j = null;
						String edge_id = null;
						mySprite sprite = null;
						
						if (!scenario_reprise) {
							cursor = model.getCursor();
							i = model.getEvents().get(cursor).get(1);
							j = model.getEvents().get(cursor).get(2);
							edge_id = getEdgebyNodes(i,j);
							
							if (Integer.parseInt(i) < Integer.parseInt(j)) {
								direction = true;
								pos = 0;
							}
							else {
								direction = false;
								pos = 1;
							}
							
							System.out.println("----------------");
							System.out.println(cursor + "/" + model.getEvents().get(cursor));
							System.out.println(edge_id);
							System.out.println("----------------");
							
							sprite = (mySprite) sman.addSprite("s_" + edge_id);
							sprite.attachToEdge(edge_id);
							sprite.setPosition(pos);
							sprite.setDirection(direction);
							sprite.addAttribute("ui.class", model.getEvents().get(cursor).get(0));
						}
						else {
							cursor = model.getCursor();
							i = model.getEvents().get(cursor).get(1);
							j = model.getEvents().get(cursor).get(2);
							edge_id = getEdgebyNodes(i,j);
							
							sprite = (mySprite) sman.getSprite("s_" + edge_id);
							scenario_reprise = false;
						}
						
						/*
						 * Each loop processes a movement, not necessarily an event.
						 * Thus we have to detect at each loop if there is an event 
						 * change or if it's just the updating of a sprite.
						 */
						while(loop) {
							
							/* We need to call the pump() method before each use 
							 * of the graph to copy back events that have already 
							 * occurred in the viewer thread inside our thread.
							 */
							fromViewer.pump();
							
							if(!sprite.move()) {
								controler.incrementCursor();
								sman.removeSprite(sprite.getId());
								
								cursor = model.getCursor();
								i = model.getEvents().get(cursor).get(1);
								j = model.getEvents().get(cursor).get(2);
								edge_id = getEdgebyNodes(i,j);
								
								if (Integer.parseInt(i) < Integer.parseInt(j)) {
									direction = true;
									pos = 0;
								}
								else {
									direction = false;
									pos = 1;
								}
								
								System.out.println("----------------");
								System.out.println(cursor + "/" + model.getEvents().get(cursor));
								System.out.println(edge_id);
								System.out.println("----------------");
								
								sprite = (mySprite) sman.addSprite("s_" + edge_id);
								sprite.attachToEdge(edge_id);
								sprite.setPosition(pos);
								sprite.setDirection(direction);
								sprite.addAttribute("ui.class", model.getEvents().get(cursor).get(0));
							}
							
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								/* Even if the thread is not waiting on the blocking code at the time of the 
								 * interruption, it will continue to reach this blocking code. And when it 
								 * arrives to this blocking code, then it will throw an exception.
								 */
								Thread.currentThread().interrupt();
								scenario_reprise = true;
								break;
							}
						}
						Thread.currentThread().interrupt();
					}
				};
				scenario_execution.start();
			}
		});
		
		JButton pauseButton = new JButton(new ImageIcon("playback_pause_icon&16.png"));
		pauseButton.setFocusPainted(false);
		toolbar.add(pauseButton);
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// it interrupts the waiting thread and throws the exception InterruptedException
				scenario_execution.interrupt();
			}
		});

		JButton nextButton = new JButton(new ImageIcon("playback_next_icon&16.png"));
		toolbar.add(nextButton);
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int cursor = model.getCursor();
				if(cursor < model.getEvents().keySet().size()) {
					/* We need to call the pump() method before each use 
					 * of the graph to copy back events that have already 
					 * occurred in the viewer thread inside our thread.
					 */
					fromViewer.pump();
					String i = model.getEvents().get(cursor).get(1);
					String j = model.getEvents().get(cursor).get(2);
					String id = "";
					if (Integer.parseInt(i) < Integer.parseInt(j)) {
						id = i+"-"+j+"-"+j+"-"+i;
					}
					else {
						id = j+"-"+i+"-"+i+"-"+j;
					}
					System.out.println("----------------");
					System.out.println(cursor + "/" + model.getEvents().get(cursor));
					System.out.println(id);
					System.out.println("----------------");
					graph.getEdge(id).setAttribute("ui.class", model.getEvents().get(cursor).get(0));
					controler.incrementCursor();
				}
			}
		});

		JButton stopButton = new JButton(new ImageIcon("playback_stop_icon&16.png"));
		toolbar.add(stopButton);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scenario_execution.interrupt(); // it interrupts the waiting thread and throws the exception InterruptedException
				// TODO : reset du graphe
			}
		});

		toolbar.setFloatable(false);

		this.setJMenuBar(menu); // add the menu to the JFrame
		container.add(toolbar, BorderLayout.NORTH); // add the toolbar to the main panel
	}

	@Override
	public void update(Observable obs, Object obj) {
		String html = "<html>\n"
                + "<body>\n"
                + "<h1>Details</h1>\n"
                + "<h2>Graph ?</h2>\n"
                + "<p>" + model.isGraphLoaded() + "</p>\n"
                + "<h2>Scenario ?</h2>\n"
                + "<p>" + model.isScenarioLoaded() + "</p>\n"
                + "<h2>Number of agents</h2>\n"
                + "<p>" + model.getNbAgents() + "</p>\n"
                + "</body>\n"
                + "</html>";
		setDisplay(html);
	}
	
	private String getEdgebyNodes(String i, String j) {
		if (Integer.parseInt(i) < Integer.parseInt(j))
			return i+"-"+j+"-"+j+"-"+i;
		else
			return j+"-"+i+"-"+i+"-"+j;
	}
	
	public void setDisplay(String s) {
		display.setText(s);
	}
	
	private class mySpritesFactory extends SpriteFactory {
		@Override
		public Sprite newSprite(String id, SpriteManager manager, Values position) {
			return new mySprite(id, manager);
		}
	}
	
	private class mySprite extends Sprite {
		private double step = 0.01;
		private boolean direction = true;
		
		public mySprite(String identifier, SpriteManager manager) {
			super(identifier, manager);
		}
		
		public void setDirection(boolean direction) {
			this.direction = direction;
		}

		/**
		 * Move the sprite in the appropriate direction.
		 */
		public boolean move() {
			double p = getX();
			
			if(direction)
				p += step;
			else
				p -= step;
			
			
			if(p<0 || p>1) {
				return false;
			}
			else {
				setPosition(p);
				return true;
			}
		}
	}
	
	/*
	 *  ViewerListener interface
	 */
	// --------------------------------
	public void viewClosed(String id) {
		loop = false;
	}

	public void buttonPushed(String id) {
		System.out.println("Noeud " + id + " attrapé");
	}

	public void buttonReleased(String id) {
		System.out.println("Noeud " + id + " relâché");
	}
	// --------------------------------
	
	
	/*
	 *  Style
	 */
	// TODO : il faudra mettre ça au propre dans un fichier peut-être
	protected static String styleSheet =
			"sprite.red {"+
			"	fill-color: red;"+
			"}"+
			"sprite.blue {"+
			"	fill-color: blue;"+
			"}"+
			"sprite.purple {"+
			"	fill-color: purple;"+
			"}"+
			"sprite.yellow {"+
			"	fill-color: yellow;"+
			"}"+
			"sprite.green {"+
			"	fill-color: green;"+
			"}"+
			"sprite.orange {"+
			"	fill-color: orange;"+
			"}"+
			"sprite {"+
			"	shape: circle;"+
			"	size: 5px;"+
			"	fill-mode: plain;"+
			"	fill-color: black;"+
			"	stroke-mode: none;"+
			"	z-index: 4;"+
			"}"+
			"graph {"+
			"	fill-mode: plain;"+
			"	fill-color: white, gray;"+
			"	padding: 60px;"+
			"}"+
			"node {"+
			"	text-alignment: under;"+
			"	text-color: black;"+
			"	size-mode: dyn-size;"+
			"	size: 15px;"+
			"	fill-color: black;"+
			"	fill-mode: dyn-plain;"+
			"}"+
			"edge {"+
			"	size: 1px;"+
			"	shape: line;"+
			"	fill-color: grey;"+
			"	fill-mode: plain;"+
			"	stroke-mode: none;"+
			"}"
	;

}
