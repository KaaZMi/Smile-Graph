package model;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Observable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Model extends Observable {

	private LinkedHashMap<String, List<String>> edges = new LinkedHashMap<String, List<String>>();
	private List<ScenarioEvent> events = new ArrayList<ScenarioEvent>();
	private int cursor = 0; // current position of the scenario

	private boolean graphLoaded = false;
	private boolean scenarioLoaded = false;
	private int nbAgents = 0;
	private static int parsing_index = 0; // index to browse the content of a message

	private List<Example> examples = new ArrayList<Example>(); // it will help us to generate colors for each example
	private HashMap<String,String> tags_colors = new HashMap<String,String>();

	public boolean openXML(String path) {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			// create a parser and a document
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document= builder.parse(new File(path));

			// recovery of the root element
			final Element racine = document.getDocumentElement();

			// recovery of the number of agents and edges
			final NodeList racineNoeuds = racine.getChildNodes();
			final int nbRacineNoeuds = racineNoeuds.getLength();

			for (int n = 0; n<nbRacineNoeuds; n++) {
				if(racineNoeuds.item(n).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
					if (racineNoeuds.item(n).getNodeName().equals("NbAgent")) {
						final String nb_agents = ((Element) racineNoeuds.item(n)).getAttribute("n");
						setNbAgents(Integer.parseInt(nb_agents));
					}
					else {
						final Element arc = (Element) racineNoeuds.item(n);

						// Edges' ID with dash between nodes' names to differentiate them, for 
						// example the edge 11-2 and the edge 1-12 with no dashes would be 112 for both.
						String i = "Ag" + arc.getAttribute("i");
						String j = "Ag" + arc.getAttribute("j");
						String id = "";

						// IMPORTANT : naming convention of ID : AB-BA with A < B
						if (Integer.parseInt(i.substring(i.length()-1)) < Integer.parseInt(j.substring(j.length()-1))) {
							id = i+"-"+j+"-"+j+"-"+i;
						}
						else {
							id = j+"-"+i+"-"+i+"-"+j;
						}

						ArrayList<String> nodes = new ArrayList<String>();
						nodes.add(i);
						nodes.add(j);
						edges.put(id, nodes);

						// display of the edge's ID
						System.out.println("\n************ARC***********");
						System.out.println(id);
					}
				}				
			}
		}
		catch (final ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (final SAXException e) {
			e.printStackTrace();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}

		setGraphLoaded(true);
		setChanged();
		notifyObservers();
		return true;
	}

	public boolean openLOG(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = null;

			int loop = 0; // DEV

			String css_class = null;
			boolean etudier_cette_ligne = false;
			Hypothesis previous_hypothesis = new Hypothesis(new ArrayList<Prototype>(),false);
			int hypothesis_id = 0;

			// tant qu'une ligne non vide est lisible
			while (((line = br.readLine()) != null) && !("".equals(line))) {
				// toute ligne sans parenthèse finale n'est pas à traiter
				// DEV : on ne traite pas les lignes System
				if ( line.substring(line.length()-1, line.length()).equals(")") ) {
					// suppression de la parenthèse finale inutile
					line = line.substring(0, line.length()-1);

					// séparation de la ligne en parties : agents / type ( / contenu )
					String[] parts = line.split(":");

					// TODO traiter les différents type de messages (couleur à donner, ...)
					etudier_cette_ligne = true;
					css_class = "";
					if (parts[1].contains("Nouveaux Exemples")) {
						css_class = "new_examples";
					}
					else if (parts[1].contains("Hypothese a tester")) {
						css_class = "hypothesis_test";
					}
					else if (parts[1].contains("Hypothese SMA-consistante")) {
						css_class = "hypothesis_const";
					}
					else if (parts[1].contains("Contre Exemples")) {
						css_class = "counter_examples";
					}
					else if (parts[1].contains("Hypothese confirmee")) {
						css_class = "hypothesis_accept";
					}
					else {
						// aucun des cas précédents n'est vérifié donc la ligne courante ne nous intéressent pas
						etudier_cette_ligne = false;
					}

					if (etudier_cette_ligne) {
						// separate source and destination
						String[] agents = parts[0].trim().split("->");

						/*
						 * Create a ScenarioEvent object to handle the content of the messages.
						 */
						ScenarioEvent scenario_event = new ScenarioEvent();
						scenario_event.setCSSClass(css_class);
						scenario_event.setSource(agents[0].trim());
						scenario_event.setDestination(agents[1].trim());
						scenario_event.setType(parts[1].trim());
						// if the event has a content
						if (parts.length > 2) {
							ArrayList<Object> content = buildList(parts[2].trim());

							/*
							 * The structure is different depending on the type of the message.
							 * Therefore we treat differently hypotheses and examples.
							 */
							if (parts[1].contains("Hypothese")) {
								Hypothesis hypothesis = parseHypothesis(content);

								/*
								 * Compare the prototypes of this event to the prototypes of the previous event.
								 */
								if (!hypothesis.compareTo(previous_hypothesis)) {
									hypothesis_id++;
									previous_hypothesis = hypothesis;
								}

								hypothesis.setId(hypothesis_id);
								scenario_event.setHypothesis(hypothesis);

								parsing_index = 0;
							}
							else if (parts[1].contains("Exemples")) {
								Example example = parseExample(content);
								scenario_event.setExample(example);

								examples.add(example);

								parsing_index = 0;
							}

						}

						this.events.add(scenario_event); // add the current event to the map
					}

					// DEV : juste pour pas lire tout le fichier
					//					loop++;
					//					if (loop>225){
					//						break;
					//					}
				}
			}
			
			this.tags_colors = generateUniqueColors(this.examples);
			br.close();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}

		setScenarioLoaded(true);
		setChanged();
		notifyObservers();
		return true;
	}

	/**
	 * Transform a string which contains nested array to a real array
	 * @param str
	 * @return array
	 */
	public ArrayList<Object> buildList(String str) {
		ArrayList<Object> list = new ArrayList<>(); // the list can contain Strings and sub-lists
		String stack = "";

		while (parsing_index < str.length()) {
			char c = str.charAt(parsing_index++);

			if (c == '[') {
				if (!stack.trim().equals("") && !stack.trim().equals(",")) {
					list.add(stack.trim()); // add the current stack to the current list
					stack = "";
				}
				list.add(buildList(str)); // new sublist
			}
			else if (c == ']') {
				if (!stack.trim().equals("") && !stack.trim().equals(",")) {
					list.add(stack.trim()); // add the current stack to the current list
					stack = "";
				}
				break; // end of the current list
			}
			else {
				stack += c;
			}
		}

		return list;
	}

	@SuppressWarnings("unchecked")
	public Hypothesis parseHypothesis(ArrayList<Object> content) {
		ArrayList<Object> level_1 = (ArrayList<Object>) content.get(0);
		ArrayList<Object> level_prototype = (ArrayList<Object>) level_1.get(1); // .get(0) is the class

		ArrayList<Prototype> prototypes = new ArrayList<Prototype>();
		for (int i = 0 ; i < level_prototype.size() ; i+=3) {
			ArrayList<String> level_atoms = (ArrayList<String>) level_prototype.get(i);
			List<String> atoms = new ArrayList<String>(Arrays.asList(level_atoms.get(0).split(", ")));
			prototypes.add(new Prototype(atoms));
		}

		return new Hypothesis(prototypes, false);
	}

	@SuppressWarnings("unchecked")
	public Example parseExample(ArrayList<Object> content) {
		ArrayList<Object> level_1 = (ArrayList<Object>) content.get(0);

		ArrayList<String> level_atoms = (ArrayList<String>) level_1.get(1);
		List<String> atoms = new ArrayList<String>(Arrays.asList(level_atoms.get(0).split(", ")));
		ArrayList<String> level_tags = (ArrayList<String>) level_1.get(3);
		List<String> tags = new ArrayList<String>(Arrays.asList(level_tags.get(0).split(", ")));

		return new Example(atoms,tags);
	}

	/**
	 * Compute a list of unique colors in order to assign each one to an example's tag.
	 * Only the hue varies in order to have the most different possible colors.
	 * Weak point : chance can make two consecutive examples having a very similar color.
	 * @param examples
	 * @return 
	 */
	public HashMap<String, String> generateUniqueColors(List<Example> examples) {
		List<Color> colors = new ArrayList<Color>();
		HashMap<String,String> tags_colors = new HashMap<String,String>();

		// fill an array of colors
		for (int i=0 ; i<examples.size() ; i++) {
			colors.add(Color.getHSBColor((float) i / examples.size(), 1, 1));
		}

		Collections.shuffle(colors); // colors are mixed randomly

		// transform each color in a readable format by GraphStream and assign it to a tag
		for (int i=0 ; i<examples.size() ; i++) {
			String rgb = "rgb(" + colors.get(i).getRed() + "," + colors.get(i).getGreen() + "," + colors.get(i).getBlue() + ")";
			tags_colors.put(examples.get(i).getTags().toString(),rgb);
		}
		
		return tags_colors;
	}

	public LinkedHashMap<String, List<String>> getEdges() {
		return edges;
	}

	public List<ScenarioEvent> getEvents() {
		return events;
	}

	public int getCursor() {
		return cursor;
	}

	public void setCursor(int cursor) {
		this.cursor = cursor;
	}

	public boolean isGraphLoaded() {
		return graphLoaded;
	}

	public void setGraphLoaded(boolean graphLoaded) {
		this.graphLoaded = graphLoaded;
	}

	public boolean isScenarioLoaded() {
		return scenarioLoaded;
	}

	public void setScenarioLoaded(boolean scenarioLoaded) {
		this.scenarioLoaded = scenarioLoaded;
	}

	public int getNbAgents() {
		return nbAgents;
	}

	public void setNbAgents(int nbAgents) {
		this.nbAgents = nbAgents;
	}

	public List<Example> getExamples() {
		return examples;
	}

	public void setExamples(List<Example> examples) {
		this.examples = examples;
	}

	public HashMap<String, String> getTags_colors() {
		return tags_colors;
	}

	public void setTags_colors(HashMap<String, String> tags_colors) {
		this.tags_colors = tags_colors;
	}

}
