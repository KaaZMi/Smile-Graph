package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkDGS;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
import org.graphstream.ui.graphicGraph.stylesheet.Values;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteFactory;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.*;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import controler.Controler;
import model.*;

@SuppressWarnings("serial")
public class Window extends JFrame implements Observer, ViewerListener {
	private JMenuBar menu;
	private JMenu file;
	private JMenuItem openXML;
	private JMenuItem openLOG;
	private JMenuItem openDGS;
	private JMenuItem save;
	private JMenuItem exit;
	private JMenu tools;
	private JMenuItem advance_to;
	private JMenuItem node_details;
	private JMenu analytical_view;
	private JMenuItem default_view;
	private JMenuItem int_ext;
	private JMenuItem one_against_all;
	private JMenu options;
	private JCheckBoxMenuItem autolayout;
	private JMenu speed_menu;
	private JMenuItem speed_slow;
	private JMenuItem speed_normal;
	private JMenuItem speed_fast;
	private JMenuItem speed_extrafast;
	private JMenu help;
	private JMenuItem about;
	private JPanel container;
	private JPanel side_panel;
	private JFileChooser fileChooser ;
	private JLabel display = new JLabel();

	private JButton prevButton;
	private JButton play_pauseButton;
	private JButton nextButton;
	private JButton stopButton;

	private Controler controler;
	private Model model;

	private Graph graph;
	private Viewer viewer;
	private ViewPanel view_panel;
	private ViewerPipe fromViewer;
	private Thread scenario_execution;
	private SpriteManager sman;

	private boolean play = false;
	private boolean currently_moving = false; // is the scenario running ?
	private int speed = 10;
	private boolean loop = true;
	private String active_view = "default";
	private String inputTag;

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
		setSize(new Dimension(1600,600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); // window at the center
		container = new JPanel();
		setContentPane(this.container); // warn our JFrame that the JPanel will constitute its content pane
		getContentPane().setLayout(new BorderLayout()); // choosing the layout manager

		side_panel = new JPanel();
		side_panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		side_panel.setPreferredSize(new Dimension(300, container.getHeight()));

		String html = "<html>\n"
				+ "<body>\n"
				+ "<p>Graph : " + model.isGraphLoaded() + "</p>\n"
				+ "<p>Scenario : " + model.isScenarioLoaded() + "</p>\n"
				+ "<p>Agents : " + model.getNbAgents() + "</p>\n"
				+ "<p>Event n° : " + model.getCursor() + "</p>\n"
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
				fileChooser.setFileFilter(new FileNameExtensionFilter("XML file (*.xml)", "xml"));
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String path = fileChooser.getSelectedFile().getAbsolutePath();
					if(controler.openXML(path)) {
						graph = new MultiGraph("embedded");
						graph.setStrict(false);
						graph.setAutoCreate(true); // automatic creation of nodes depending edges

						sman = new SpriteManager(graph);
						sman.setSpriteFactory(new CustomSpritesFactory());

						LinkedHashMap<String, List<String>> edges = model.getEdges();
						for (Entry<String, List<String>> entry : edges.entrySet()) {
							// add an undirected edge
							graph.addEdge(entry.getKey(), entry.getValue().get(0), entry.getValue().get(1), false);
						}

						graph.addNode("System");

						for (Node node : graph) {
							if (node.getId().equals("System")) {
								node.setAttribute("ui.label", node.getId());
								node.addAttribute("ui.class", "system");
							}
							else {
								node.setAttribute("ui.label", node.getId());
								// add an edge from System to all nodes
								graph.addEdge("System-"+node.getId()+"-"+node.getId()+"-System", "System", node.getId()).addAttribute("ui.class", "system");
								node.setAttribute("memory", new ArrayList<Example>());
							}
						}

						graph.addAttribute("ui.antialias"); // graphics smoothing
						graph.addAttribute("ui.quality");
						graph.addAttribute("ui.stylesheet", STYLESHEET); // CSS style of the graph

						viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
						viewer.enableAutoLayout();
						view_panel = viewer.addDefaultView(false); // false indicates "no JFrame"

						/* IMPORTANT !
						 * We connect back the viewer to the graph,
						 * the graph becomes a sink for the viewer.
						 * We also install a viewer listener 
						 * to intercept the graphic events.
						 */
						fromViewer = viewer.newViewerPipe();
						fromViewer.addSink(graph);
						fromViewer.addViewerListener(Window.this);
						fromViewer.pump();
						
						for (Component c : container.getComponents()) {
							if (c.getClass().getSimpleName().equals("DefaultView")) {
								container.remove(c);
							}
						}
						
						container.add((Component) view_panel, BorderLayout.CENTER);
						
						autolayout.setEnabled(true);
						node_details.setEnabled(true);
					}
				}
				container.revalidate();
			}	    	
		});

		openLOG = new JMenuItem("Open a scenario");
		openLOG.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("Log file (*.log)", "log"));
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String path = fileChooser.getSelectedFile().getAbsolutePath();
					if(controler.openLOG(path)) {
						play_pauseButton.setEnabled(true);
						nextButton.setEnabled(true);
						speed_menu.setEnabled(true);
					}
				}
				container.revalidate();
			}	    	
		});

		openDGS = new JMenuItem("Open a DGS graph");
		openDGS.addActionListener(new ActionListener(){
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent arg0){
				fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("DGS file (*.dgs)", "dgs"));
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String path = fileChooser.getSelectedFile().getAbsolutePath();
					FileSource fs = null;
					graph = new MultiGraph("embedded");

					try {
						fs = FileSourceFactory.sourceFor(path);
						fs.addSink(graph);
						fs.readAll(path);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						fs.removeSink(graph);
					}

					for (Node node : graph) {
						if (!node.getId().equals("System")) {
							try {
								node.setAttribute("memory", (ArrayList<Example>) fromBase64String(node.getAttribute("memory")));
								node.setAttribute("hypothesis", (Hypothesis) fromBase64String(node.getAttribute("hypothesis")));
							} catch (IOException | ClassNotFoundException e) {
								e.printStackTrace();
							}
						}
					}
					
//					viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
//					viewer.enableAutoLayout();
//					view_panel = viewer.addDefaultView(false); // false indicates "no JFrame"
//					
//					fromViewer = viewer.newViewerPipe();
//					fromViewer.addSink(graph);
//					fromViewer.addViewerListener(Window.this);
//					fromViewer.pump();
//					
//					for (Component c : container.getComponents()) {
//						if (c.getClass().getSimpleName().equals("DefaultView")) {
//							container.remove(c);
//						}
//					}
//					
//					container.add((Component) view_panel, BorderLayout.CENTER);
//					
//					autolayout.setEnabled(true);
//					node_details.setEnabled(true);
				}
				container.revalidate();
			}
		});

		save = new JMenuItem("Save");
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				graph.setAttribute("cursor", model.getCursor());
				for (Node node : graph) {
					if (!node.getId().equals("System")) {
						try {
							node.setAttribute("memory", toBase64String(node.getAttribute("memory")));
							node.setAttribute("hypothesis", toBase64String(node.getAttribute("hypothesis")));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				FileSinkDGS fs = new FileSinkDGS();
				try {
					fs.writeAll(graph, "output.dgs");
				} catch (IOException e) {
					e.printStackTrace();
				}
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
		file.add(openDGS);
		file.add(save);
		file.addSeparator();
		file.add(exit);

		tools = new JMenu("Tool");

		advance_to = new JMenuItem("Advance to...");
		advance_to.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (model.isGraphLoaded() && model.isScenarioLoaded()) {
					String inputValue = JOptionPane.showInputDialog("Please input an example's ID");

					int limit = Integer.parseInt(inputValue);

					controler.resetCursor();
					
					for(Sprite sprite: sman) {
						sman.removeSprite(sprite.getId()); // each sprite is removed
					}

					for (Node node : graph) {
						node.setAttribute("memory", new ArrayList<Example>()); // memory of the nodes is reset
						if (node.hasAttribute("hypothesis")) {
							node.removeAttribute("hypothesis");
						}
						if (!node.getId().equals("System")) {
							node.setAttribute("ui.size", 25); // size is reset
						}
					}

					while (limit > 0) {
						fromViewer.pump();
						
						play_pauseButton.setEnabled(false);
						nextButton.setEnabled(false);

						int cursor = model.getCursor();
						ScenarioEvent se = model.getEvents().get(cursor);

						// System.out.println(cursor + "/" + se);

						updateNode(se);

						if (se.getType().contains("Nouveaux Exemples")) {
							limit--;
						}

						controler.incrementCursor();
					}
					
					currently_moving = false;
					play_pauseButton.setEnabled(true);
					nextButton.setEnabled(true);
					stopButton.setEnabled(true);
				}
				else if (!model.isGraphLoaded()) {
					enableWarning("Graph not loaded.");
				}
				else if (!model.isScenarioLoaded()) {
					enableWarning("Scenario not loaded.");
				}
			}
		});

		node_details = new JMenuItem("Get node's details");
		node_details.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String inputValue = JOptionPane.showInputDialog("Please input an node's ID");
				JTextArea textArea = new JTextArea(6, 25);
				textArea.setText(getNodeInfoByID(inputValue));
				textArea.setEditable(false);
				JScrollPane scrollPane = new JScrollPane(textArea);
				JOptionPane.showMessageDialog(null, scrollPane, "Details", JOptionPane.NO_OPTION);
			}
		});
		node_details.setEnabled(false);

		analytical_view = new JMenu("Analytical view");

		default_view = new JMenuItem("Default");
		default_view.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				active_view = "default";
				for (Node node : graph) {
					if (!node.getId().equals("System")) {
						node.setAttribute("ui.style", "shape: circle; fill-color: #fad15f;");
					}
				}
			}
		});

		int_ext = new JMenuItem("Internal versus External");
		int_ext.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (model.isGraphLoaded()) {
					active_view = "int_ext";
					for (Node node : graph) {
						if (!node.getId().equals("System")) {
							node.setAttribute("ui.style", "shape: pie-chart; fill-color: cyan, orange;");

							double[] pie_values = new double[2];
							pie_values[0] = 0.0;
							pie_values[1] = 0.0;

							ArrayList<Example> memory = node.getAttribute("memory");
							for (Example example : memory) {
								for (String tag : example.getTags()) {
									if (tag.equals("Ext")) {
										pie_values[0] += 1.0 / (double) memory.size();
										break;
									}
									else if (tag.equals("Int")) {
										pie_values[1] += 1.0 / (double) memory.size();
										break;
									}
								}
							}

							node.setAttribute("ui.pie-values", pie_values);
						}
					}
				}
				else {
					enableWarning("Graph not loaded.");
				}
			}
		});

		one_against_all = new JMenuItem("One against all");
		one_against_all.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (model.isGraphLoaded()) {
					/*
					 * It's not mandatory to handle a non-existent tag, other 
					 * tags will be considered as winners.
					 */
					inputTag = JOptionPane.showInputDialog("Please input a tag");
					active_view = "one_against_all";
					for (Node node : graph) {
						if (!node.getId().equals("System")) {
							node.setAttribute("ui.style", "shape: pie-chart; fill-color: cyan, orange;");
	
							double[] pie_values = new double[2];
							pie_values[0] = 0.0;
							pie_values[1] = 0.0;
	
							ArrayList<Example> memory = node.getAttribute("memory");
							for (Example example : memory) {
								boolean tag_found = false;
								for (String tag : example.getTags()) {
									if (tag.equals(inputTag)) {
										pie_values[0] += 1.0 / (double) memory.size();
										tag_found = true;
										break;
									}
								}
								if (!tag_found) {
									pie_values[1] += 1.0 / (double) memory.size();
								}
							}
	
							node.setAttribute("ui.pie-values", pie_values);
						}
					}
				}
				else {
					enableWarning("Graph not loaded.");
				}
			}
		});
		//analytical_view.setEnabled(false);

		analytical_view.add(default_view);
		analytical_view.add(int_ext);
		analytical_view.add(one_against_all);

		tools.add(advance_to);
		tools.add(node_details);
		tools.add(analytical_view);

		options = new JMenu("Options");

		autolayout = new JCheckBoxMenuItem("Auto-layout");
		autolayout.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (((AbstractButton) e.getSource()).getModel().isSelected())
					viewer.enableAutoLayout();
				else
					viewer.disableAutoLayout();
			}
		});
		autolayout.setSelected(true);
		autolayout.setEnabled(false);

		speed_menu = new JMenu("Execution speed");
		ButtonGroup speed_group = new ButtonGroup();
		speed_slow = new JRadioButtonMenuItem("Slow");
		speed_normal = new JRadioButtonMenuItem("Normal");
		speed_fast = new JRadioButtonMenuItem("Fast");
		speed_extrafast = new JRadioButtonMenuItem("Extra fast");
		speed_normal.setSelected(true); // default speed
		speed_group.add(speed_slow);
		speed_group.add(speed_normal);
		speed_group.add(speed_fast);
		speed_group.add(speed_extrafast);

		speed_slow.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setSpeed(30);
			}
		});
		speed_normal.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setSpeed(10);
			}
		});
		speed_fast.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setSpeed(1);
			}
		});
		speed_extrafast.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setSpeed(0);
			}
		});

		speed_menu.add(speed_slow);
		speed_menu.add(speed_normal);
		speed_menu.add(speed_fast);
		speed_menu.add(speed_extrafast);

		speed_menu.setEnabled(false);

		options.add(autolayout);
		options.add(speed_menu);

		help = new JMenu("Help");

		about = new JMenuItem("About");
		about.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JOptionPane.showMessageDialog(null,
						"Créateurs : Le Boulaire Mikaël & El Fakhry Charbel",
						"About", JOptionPane.NO_OPTION);
			}
		});

		help.add(about);

		menu.add(file);
		menu.add(tools);
		menu.add(options);
		menu.add(help);
		this.setJMenuBar(menu); // add the menu to the JFrame
	}

	private void initToolBar() {
		JToolBar toolbar = new JToolBar();

		prevButton = new JButton(new ImageIcon("images/playback_prev_icon&16.png"));
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});

		play_pauseButton = new JButton(new ImageIcon("images/playback_play_icon&16.png"));
		play_pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!stopButton.isEnabled()) {
					stopButton.setEnabled(true);
				}

				if (!play) {
					play = true;
					// switch between play/pause icon
					((AbstractButton) e.getSource()).setIcon(new ImageIcon("images/playback_pause_icon&16.png"));

					// scenario execution in a separate thread
					scenario_execution = new Thread() {
						public void run() {
							play();
						}
					};
					scenario_execution.start();
				}
				else {
					play = false;
					((AbstractButton) e.getSource()).setIcon(new ImageIcon("images/playback_play_icon&16.png"));
					nextButton.setEnabled(true);

					// it interrupts the waiting thread and throws the exception InterruptedException
					scenario_execution.interrupt();
				}
			}
		});

		nextButton = new JButton(new ImageIcon("images/playback_next_icon&16.png"));
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!stopButton.isEnabled()) {
					stopButton.setEnabled(true);
				}

				// scenario execution in a separate thread
				scenario_execution = new Thread() {
					public void run() {
						CustomSprite sprite;
						int cursor = model.getCursor();						
						ScenarioEvent se = model.getEvents().get(cursor);
						String i = se.getSource();
						String j = se.getDestination();

						// System.out.println(cursor + "/" + se);

						/*
						 * Process a movement between two nodes.
						 */
						if (j != null) {
							String edge_id = getEdgebyNodes(i,j);
							if (!currently_moving) {
								sprite = (CustomSprite) sman.addSprite("s_" + edge_id);
								sprite.attachToEdge(edge_id);
								sprite.initState(i, j);
								sprite.setAttribute("ui.class", se.getCSSClass());
								if (se.getType().contains("Hypothese")) {
									sprite.setAttribute("ui.label", se.getHypothesis().getId());
								}
								else if (se.getType().contains("Exemples")) {
									sprite.setAttribute("ui.style", "fill-color: " + model.getExamples_colors().get(se.getExample().getId()) + ";");								}
							}
							else {
								// continue the movement of the sprite.
								sprite = (CustomSprite) sman.getSprite("s_" + edge_id);
							}

							/*
							 * Each loop processes a movement until it reaches the 
							 * destination node.
							 */
							while(loop) {
								nextButton.setEnabled(false);
								play_pauseButton.setEnabled(false);

								/* We need to call the pump() method before each use 
								 * of the graph to copy back events that have already 
								 * occurred in the viewer thread inside our thread.
								 */
								fromViewer.pump();

								/*
								 * We call the function which moves the sprite
								 * until it returns false.
								 */
								if(!sprite.move()) {
									updateNode(model.getEvents().get(cursor));
									controler.incrementCursor();
									if (sman.hasSprite(sprite.getId())) {
										sman.removeSprite(sprite.getId());
									}
									nextButton.setEnabled(true);
									play_pauseButton.setEnabled(true);
									break;
								}

								try {
									Thread.sleep(speed);
								} catch (InterruptedException e2) {
									/* should never happen because 
									 * all the buttons are disabled 
									 * until the end of the action.
									 */
									currently_moving = true;
									Thread.currentThread().interrupt();
									break;
								}
							}

							currently_moving = false;
							Thread.currentThread().interrupt();

						}

						/*
						 * Process a node's task applied to itself.
						 */
						else {
							updateNode(se);
							controler.incrementCursor();
							currently_moving = false;
							Thread.currentThread().interrupt();

						}
					}
				};
				scenario_execution.start();
			}
		});

		stopButton = new JButton(new ImageIcon("images/playback_stop_icon&16.png"));
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prevButton.setEnabled(false);
				stopButton.setEnabled(false);
				nextButton.setEnabled(true);
				play_pauseButton.setIcon(new ImageIcon("images/playback_play_icon&16.png"));
				play_pauseButton.setEnabled(true);
				play = false;

				// it interrupts the waiting thread and throws the exception InterruptedException
				scenario_execution.interrupt();
				try {
					scenario_execution.join(); // wait until the thread finishes
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				};

				for(Sprite sprite: sman) {
					sman.removeSprite(sprite.getId()); // each sprite is removed
				}


				for (Node node : graph) {
					node.setAttribute("memory", new ArrayList<Example>()); // memory of the nodes is reset
					if (node.hasAttribute("hypothesis")) {
						node.removeAttribute("hypothesis");
					}
					if (!node.getId().equals("System")) {
						node.setAttribute("ui.size", 25); // size is reset
					}
				}

				currently_moving = false;
				controler.resetCursor();
			}
		});

		prevButton.setFocusPainted(false);
		play_pauseButton.setFocusPainted(false);
		nextButton.setFocusPainted(false);
		stopButton.setFocusPainted(false);
		toolbar.add(prevButton);
		toolbar.add(play_pauseButton);
		toolbar.add(nextButton);
		toolbar.add(stopButton);
		prevButton.setEnabled(false);
		play_pauseButton.setEnabled(false);
		nextButton.setEnabled(false);
		stopButton.setEnabled(false);
		toolbar.setFloatable(false);
		container.add(toolbar, BorderLayout.NORTH); // add the toolbar to the main panel
	}

	/**
	 * Iterative function that runs the scenario.
	 */
	public void play() {
		CustomSprite sprite;
		int cursor = model.getCursor();
		ScenarioEvent se = model.getEvents().get(cursor);
		String i = se.getSource();
		String j = se.getDestination();

		// System.out.println(cursor + "/" + se);

		/*
		 * Process a movement between two nodes.
		 */
		if (j != null) {
			String edge_id = getEdgebyNodes(i,j);

			if (!currently_moving) {
				sprite = (CustomSprite) sman.addSprite("s_" + edge_id);
				sprite.attachToEdge(edge_id);
				sprite.initState(i, j);
				sprite.setAttribute("ui.class", se.getCSSClass());
				if (se.getType().contains("Hypothese")) {
					sprite.setAttribute("ui.label", se.getHypothesis().getId());
				}
				else if (se.getType().contains("Exemples")) {
					sprite.setAttribute("ui.style", "fill-color: " + model.getExamples_colors().get(se.getExample().getId()) + ";");
				}
			}
			else {
				// continue the movement of the sprite.
				sprite = (CustomSprite) sman.getSprite("s_" + edge_id);
				currently_moving = false;
			}

			/*
			 * Each loop processes a movement, not necessarily an event.
			 * Thus we have to detect at each loop if there is an event 
			 * change or if it's just the updating of a sprite.
			 */
			while(loop) {
				nextButton.setEnabled(false);

				/* We need to call the pump() method before each use 
				 * of the graph to copy back events that have already 
				 * occurred in the viewer thread inside our thread.
				 */
				fromViewer.pump();

				/*
				 * We call the function which moves the sprite
				 * until it returns false.
				 */
				if(!sprite.move()) {
					updateNode(model.getEvents().get(cursor));		
					controler.incrementCursor();

					// check if we can continue the execution
					if (!controler.hasAuthorization()) {
						nextButton.setEnabled(false);
						play_pauseButton.setEnabled(false);
						break;
					}

					if (sman.hasSprite(sprite.getId())) {
						sman.removeSprite(sprite.getId());
					}

					cursor = model.getCursor();
					se = model.getEvents().get(cursor);
					i = se.getSource();
					j = se.getDestination();

					// System.out.println(cursor + "/" + se);

					/*
					 * Process a movement between two nodes.
					 */
					if (j != null) {
						edge_id = getEdgebyNodes(i,j);

						sprite = (CustomSprite) sman.addSprite("s_" + edge_id);
						sprite.attachToEdge(edge_id);
						sprite.initState(i, j);
						sprite.setAttribute("ui.class", se.getCSSClass());
						if (se.getType().contains("Hypothese")) {
							sprite.setAttribute("ui.label", se.getHypothesis().getId());
						}
						else if (se.getType().contains("Exemples")) {
							sprite.setAttribute("ui.style", "fill-color: " + model.getExamples_colors().get(se.getExample().getId()) + ";");
						}
					}
				}

				try {
					Thread.sleep(speed);
				} catch (InterruptedException e) {
					/* Even if the thread is not waiting on the blocking code at the time of the 
					 * interruption, it will continue to reach this blocking code. And when it 
					 * arrives to this blocking code, then it will throw an exception.
					 */
					Thread.currentThread().interrupt();
					currently_moving = true;
					nextButton.setEnabled(true);
					break;
				}
			}
		}

		/*
		 * Process a node's task applied to itself.
		 */
		else {
			updateNode(se);
			controler.incrementCursor();
			play();
		}

		Thread.currentThread().interrupt();
	}

	/**
	 * Retrieve the ID of an edge based on the ID of its nodes.
	 * @param i : ID of source
	 * @param j : ID of destination
	 * @return String : ID of edge
	 */
	private String getEdgebyNodes(String i, String j) {
		String id = "";

		if ( i.contains("System") || j.contains("System") ) {
			if ( i.contains("System") )
				id = "System-"+j+"-"+j+"-System";
			else if ( j.contains("System") )
				id = "System-"+i+"-"+i+"-System";
		}

		else {
			if (Integer.parseInt(i.substring(i.length()-1)) < Integer.parseInt(j.substring(j.length()-1)))
				id = i+"-"+j+"-"+j+"-"+i;
			else
				id = j+"-"+i+"-"+i+"-"+j;
		}

		return id;
	}

	/**
	 * Update of a node according to information that reached it or action it applied to itself.
	 * @param se : event responsible for the update
	 */
	private void updateNode(ScenarioEvent se) {
		/*
		 * If an agent receives new examples, they are added to his memory.
		 */
		if (se.getType().contains("Exemples")) {
			Node node_destination = graph.getNode(se.getDestination());
			ArrayList<Example> memory = node_destination.getAttribute("memory");
			// System.out.println("EXAMPLE TO ADD : " + se.getExample());
			try {
				Example ex = (Example)(ObjectCloner.deepCopy(se.getExample()));
				memory.add(ex);
			} catch (Exception e) {
				e.printStackTrace();
			}
			node_destination.setAttribute("memory", memory);
			node_destination.setAttribute("ui.size", memory.size()+25);
			// System.out.println("MEMORY AFTER NEW EXAMPLE : " + memory);
		}

		else if (se.getType().contains("Hypothese SMA-consistante")) {
			Node node_source = graph.getNode(se.getSource());
			Hypothesis h = se.getHypothesis();
			h.setConsistent(true);
			node_source.setAttribute("hypothesis", h);
			// System.out.println(node_source.getAttribute("hypothesis").toString());
		}
		else if (se.getType().contains("revision protocol")) {
			Node node_source = graph.getNode(se.getSource());
			node_source.setAttribute("hypothesis", se.getHypothesis());
		}
		else if (se.getType().contains("adopts")) {
			Node node_source = graph.getNode(se.getSource());
			node_source.setAttribute("hypothesis", se.getHypothesis());
		}
		else if (se.getType().contains("tags ex")) {
			Node node_source = graph.getNode(se.getSource());
			ArrayList<Example> memory = node_source.getAttribute("memory");
			// System.out.println("MEMORY BEFORE TAGGING : " + memory);
			for (Example e : memory) {
				if (e.getId() == se.getExample().getId()) {
					for (String tag : se.getExample().getTags()) {
						if (!e.getTags().contains(tag)) {
							e.getTags().add(tag);
						}
					}
				}
			}
			node_source.setAttribute("memory", memory);
			if (!active_view.equals("default")) {
				updateAnalyticalView(node_source);
			}
			// System.out.println("MEMORY AFTER TAGGING : " + memory);
		}
		else if (se.getType().contains("remove from ex")) {
			Node node_source = graph.getNode(se.getSource());
			ArrayList<Example> memory = node_source.getAttribute("memory");
			// System.out.println("MEMORY BEFORE REMOVE : " + memory);
			for (Example e : memory) {
				if (e.getId() == se.getExample().getId()) {
					e.setTags(se.getExample().getTags());
				}
			}
			node_source.setAttribute("memory", memory);
			if (!active_view.equals("default")) {
				updateAnalyticalView(node_source);
			}
			// System.out.println("MEMORY AFTER REMOVE : " + memory);
		}

	}

	/**
	 * Updates the display of a node according to the current analytical view selected by the user.
	 * @param node : node whose display must be updated
	 */
	private void updateAnalyticalView(Node node) {
		if (active_view.equals("int_ext")) {
			node.setAttribute("ui.style", "shape: pie-chart; fill-color: cyan, orange;");

			double[] pie_values = new double[2];
			pie_values[0] = 0.0;
			pie_values[1] = 0.0;

			ArrayList<Example> memory = node.getAttribute("memory");
			for (Example example : memory) {
				for (String tag : example.getTags()) {
					if (tag.equals("Ext")) {
						pie_values[0] += 1.0 / (double) memory.size();
						break;
					}
					else if (tag.equals("Int")) {
						pie_values[1] += 1.0 / (double) memory.size();
						break;
					}
				}
			}

			node.setAttribute("ui.pie-values", pie_values);
		}
		else if (active_view.equals("one_against_all")) {
			node.setAttribute("ui.style", "shape: pie-chart; fill-color: cyan, orange;");

			double[] pie_values = new double[2];
			pie_values[0] = 0.0;
			pie_values[1] = 0.0;

			ArrayList<Example> memory = node.getAttribute("memory");
			for (Example example : memory) {
				boolean tag_found = false;
				for (String tag : example.getTags()) {
					if (tag.equals(inputTag)) {
						pie_values[0] += 1.0 / (double) memory.size();
						tag_found = true;
						break;
					}
				}
				if (!tag_found) {
					pie_values[1] += 1.0 / (double) memory.size();
				}
			}

			node.setAttribute("ui.pie-values", pie_values);
		}
	}

	/**
	 * Retrieve information of a node thanks to its ID.
	 * @param node_id : ID of the node
	 * @return String : information inside the node
	 */
	public String getNodeInfoByID(String node_id) {
		Node n = graph.getNode(node_id);
		ArrayList<Example> memory  = n.getAttribute("memory");
		String data = "";
		data += n.getAttribute("hypothesis");
		for (Example e : memory) {
			data += "Example " + e.getId() + " with tags " + e.getTags() + "\n";
		}
		return data;
	}
	
	@Override
	public void update(Observable obs, Object obj) {
		String html = "<html>\n"
				+ "<body>\n"
				+ "<p>Graph : " + model.isGraphLoaded() + "</p>\n"
				+ "<p>Scenario : " + model.isScenarioLoaded() + "</p>\n"
				+ "<p>Agents : " + model.getNbAgents() + "</p>\n"
				+ "<p>Event n° : " + model.getCursor() + "</p>\n"
				+ "</body>\n"
				+ "</html>";
		setDisplay(html);
	}

	public void setDisplay(String s) {
		display.setText(s);
	}

	/**
	 * Display a particular message in a dialog window.
	 * @param warning : message to show
	 */
	public void enableWarning(String warning) {
		JOptionPane.showMessageDialog(null, warning, "Warning", JOptionPane.NO_OPTION);
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	/**
	 * A factory which allows to use custom Sprite.
	 */
	private class CustomSpritesFactory extends SpriteFactory {
		@Override
		public Sprite newSprite(String id, SpriteManager manager, Values position) {
			return new CustomSprite(id, manager);
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
		// System.out.println("Noeud " + id + " attrapé");
	}

	public void buttonReleased(String id) {
		// System.out.println("Noeud " + id + " relâché");
	}
	// --------------------------------

	/** 
	 * Read an object from Base64 string.
	 * @return Object : reconstructed object
	 */
	private static Object fromBase64String(String s) throws IOException, ClassNotFoundException {
		byte [] data = Base64.getDecoder().decode(s);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	/**
	 * Write an object to a Base64 string.
	 * @return String : object deeply converted in Base64
	 */
	private static String toBase64String(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return Base64.getEncoder().encodeToString(baos.toByteArray()); 
	}


	/*
	 *  Style
	 */
	static final String STYLESHEET = 
					"graph {"+
					"	fill-mode: gradient-radial;"+
					"	fill-color: #FFFFFF,#EEEEEE;"+
					"}"+
					"edge {"+
					"	size: 1.5px;"+
					"	shape: line;"+
					"	fill-color: #222222;"+
					"	fill-mode: dyn-plain;"+
					"	size-mode: dyn-size;"+
					"}"+
					"edge.system {"+
					"	size: 0px;"+
					"	shape: line;"+
					"	fill-mode: none;"+
					"	stroke-mode: dashes;"+
					"	stroke-width: 1px;"+
					"	stroke-color: #222222;"+
					"}"+
					"node {"+
					"	size: 25;"+
					"	shape: circle;"+
					"	fill-mode: dyn-plain;"+
					"	fill-color: #fad15f;"+
					"	size-mode: dyn-size;"+
					"	stroke-mode: plain;"+
					"	stroke-width: 2px;"+
					"	stroke-color: #333333;"+
					"}"+
					"node.system {"+
					"	size: 40;"+
					"	shape: rounded-box;"+
					"	fill-mode: dyn-plain;"+
					"	fill-color: #fad15f;"+
					"	size-mode: dyn-size;"+
					"	stroke-mode: plain;"+
					"	stroke-width: 2px;"+
					"	stroke-color: #333333;"+
					"}"+
					"sprite {"+
					"	size: 20px;"+
					"	fill-mode: plain;"+
					"	stroke-mode: plain;"+
					"	stroke-width: 2px;"+
					"	stroke-color: #333333;"+
					"	z-index: 4;"+
					"}"+
					"sprite.hypothesis_test {"+
					"	fill-color: white;"+
					"}"+
					"sprite.hypothesis_accept {"+
					"	fill-color: white;"+
					"	stroke-color: green;"+
					"}"+
					"sprite.hypothesis_const {"+
					"	fill-color: white;"+
					"	stroke-color: purple;"+
					"}"+
					"sprite.new_examples {"+
					"	shape: rounded-box;"+
					"}"+
					"sprite.counter_examples {"+
					"	shape: cross;"+
					"}"
					;

}
