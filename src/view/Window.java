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
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.spriteManager.Sprite;
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
	protected ViewerPipe fromViewer = null;

	private JMenuBar menu = null;
	private JMenu file = null;
	private JMenuItem openXML = null;
	private JMenuItem openLOG = null;
	private JMenuItem save = null;
	private JMenuItem exit = null;
	private JMenu help = null;
	private JMenuItem about = null;
	private JPanel container = null;
	private JSlider slider = null;
	private JPanel side_panel = null;
	private JFileChooser fileChooser = null;
	private JLabel display = new JLabel();

	private Controler controler;
	private Model model;

	private Graph graph = null;
	private LinkedHashMap<Integer, ArrayList<String>> events = new LinkedHashMap<Integer, ArrayList<String>>();
	private Thread scenario_execution = null;
	private SpriteManager sman = null;

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
	
						LinkedHashMap<String, ArrayList<String>> edges = model.getEdges();
						for (Entry<String, ArrayList<String>> entry : edges.entrySet()) {
							graph.addEdge(entry.getKey(), entry.getValue().get(0), entry.getValue().get(1), false); // false = not oriented
						}
						for (Node node : graph) {
							node.addAttribute("ui.label", node.getId());
						}
	
						Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
						viewer.enableAutoLayout();
						ViewPanel view_panel = viewer.addDefaultView(false); // false indicates "no JFrame"
	
						/* IMPORTANT !
						 * We connect back the viewer to the graph,
						 * the graph becomes a sink for the viewer.
						 * We also install us as a viewer listener 
						 * to intercept the graphic events.
						 */
						fromViewer = viewer.newViewerPipe();
						fromViewer.addViewerListener(Window.this);
						fromViewer.addSink(graph);
	
						// TODO : MouseWheelListener pour le zoom au lieu de JSlider (priorité minimal)
						slider = new JSlider();
						slider.addChangeListener(e -> view_panel.getCamera().setViewPercent(slider.getValue() / 10.0));
						slider.setBackground(Color.white);
						
						container.add((Component) view_panel, BorderLayout.CENTER);
						container.add(slider, BorderLayout.SOUTH);
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
						//for (Entry<Integer, ArrayList<String>> entry : events.entrySet()) {
						for (int cursor = model.getCursor() ; cursor < model.getEvents().keySet().size() ; cursor++) {
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
							
							Sprite s = null;
							if(sman.getSprite("s_"+id) != null) {
								s = sman.getSprite("s_"+id);
							}
							else {
								s = sman.addSprite("s_"+id);
								s.attachToEdge(id);
							}
							
							if(s.hasAttribute("ui.hide")) {
								s.removeAttribute("ui.hide");
							}
							
							double start;
							double end;
							double increment;
							if(i.equals(graph.getEdge(id).getNode0().getAttribute("ui.label"))) {
								start = 0.0;
								end = 1.0;
								increment = 0.01;
								while(start < end) {
									s.setPosition(start);
									start += increment;
									try {
										Thread.sleep(10);
									} catch (InterruptedException e1) {
										Thread.currentThread().interrupt();
										break;
									}
								}
							}
							else {
								start = 1.0;
								end = 0.0;
								increment = -0.01;
								while(start > end) {
									s.setPosition(start);
									start += increment;
									try {
										Thread.sleep(10);
									} catch (InterruptedException e1) {
										Thread.currentThread().interrupt();
										break;
									}
								}
							}
							
							s.addAttribute("ui.hide");
							
							controler.incrementCursor();
							try {
								Thread.sleep(500); // speed of execution
							} catch (InterruptedException e1) {
								/* Even if the thread is not waiting on the blocking code at the time of the 
								 * interruption, it will continue to reach this blocking code. And when it 
								 * arrives to this blocking code, then it will throw an exception.
								 */
								Thread.currentThread().interrupt();
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
				scenario_execution.interrupt(); // it interrupts the waiting thread and throws the exception InterruptedException
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

	public void viewClosed(String id) {
		loop = false;
	}

	public void buttonPushed(String id) {
		System.out.println("Noeud " + id + " attrapé");
	}

	public void buttonReleased(String id) {
		System.out.println("Noeud " + id + " relâché");
	}
	
	public void setDisplay(String s) {
		display.setText(s);
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
			"}"+
			"node {"+
			"	text-alignment: under;"+
			"	text-color: black;"+
			"}"
	;

}
