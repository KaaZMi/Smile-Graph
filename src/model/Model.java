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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private HashMap<Integer,String> examples_colors = new HashMap<Integer,String>();

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
			boolean process_line = false;

			// as a non-blank line is readable
			while (((line = br.readLine()) != null)) {
				// any line without final parenthesis is not to treat
				if (!("".equals(line))) {
					if (line.contains("->") && !line.contains("] -> [")) {
						// removing unnecessary final parenthesis
						line = line.substring(0, line.length()-1);
	
						// split the line in parts : agent / type / content
						String[] parts = line.split(":");
						
						process_line = true;
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
							// none of the above facts is checked therefore the current line doesn't interest us
							process_line = false;
						}
	
						if (process_line) {
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
								/*
								 * The structure is different depending on the type of the message.
								 * Therefore we treat differently hypotheses and examples.
								 */
								if (parts[1].contains("Hypothese")) {
									ArrayList<Object> content = buildList(parts[3].trim());
									Hypothesis hypothesis = parseHypothesis(content);
									String str = parts[2].trim();
									hypothesis.setId(str.substring(str.indexOf("(")+1,str.indexOf(")")));
									//System.out.println(hypothesis);
									scenario_event.setHypothesis(hypothesis);
	
									parsing_index = 0;
								}
								else if (parts[1].contains("Exemples")) {
									ArrayList<Object> content = buildList(parts[2].trim());
									Example example = parseExample(content);
									scenario_event.setExample(example);
									//System.out.println(example);
	
									examples.add(example);
	
									parsing_index = 0;
								}
	
							}
	
							this.events.add(scenario_event); // add the current event to the map
						}
					}
					
					else if (line.contains("((Ag")) {
						// split the line in parts : agent / type / (id /) content
						String[] parts = line.split(":");
						
						/*
						 * Create a ScenarioEvent object to handle the content of the messages.
						 */
						ScenarioEvent scenario_event = new ScenarioEvent();
						scenario_event.setSource(parts[0].trim().replace("(", "").replace(")", ""));
						scenario_event.setType(parts[1].trim());
						
						if (parts[1].contains("revision protocol")) {
							ArrayList<Object> content = buildList(parts[2].trim());
							parsing_index = 0;
							Hypothesis hypothesis = parseHypothesis(content);
							String str = parts[1].trim();
							hypothesis.setId(str.substring(str.indexOf("(")+1,str.indexOf(")")));
							System.out.println("PROTOCOL : " + hypothesis);
							scenario_event.setHypothesis(hypothesis);
						}
						else if (parts[1].contains("adopts")) {
							ArrayList<Object> content = buildList(parts[3].trim());
							parsing_index = 0;
							Hypothesis hypothesis = parseHypothesis(content);
							String str = parts[2].trim();
							hypothesis.setId(str.substring(str.indexOf("(")+1,str.indexOf(")")));
							System.out.println("ADOPTS : " + hypothesis);
							scenario_event.setHypothesis(hypothesis);
						}
						else if (parts[1].contains("tags ex")) {
							ArrayList<Object> content = (ArrayList<Object>) buildList(parts[2].trim()).get(0);
							parsing_index = 0;
							String level_tags = (String) content.get(0);
							List<String> tags = new ArrayList<String>(Arrays.asList(level_tags.split(", ")));
							Example example = new Example(Integer.parseInt(parts[1].replaceAll("\\D+","")), null, tags);
							System.out.println("TAGGING : " + example);
							scenario_event.setExample(example);
						}
						else if (parts[1].contains("remove from ex")) {
							String[] content = parts[2].split("->");
							String level_tag_to_remove = content[0].trim();
							String level_tag_to_add = content[1].trim();
							
							List<String> tags_to_replace = new ArrayList<String>();
							
							ArrayList<Object> tag_to_remove = (ArrayList<Object>) buildList(level_tag_to_remove).get(0);
							parsing_index = 0;
							tags_to_replace.add((String) tag_to_remove.get(0));
							
							ArrayList<Object> tag_to_add = (ArrayList<Object>) buildList(level_tag_to_add).get(0);
							parsing_index = 0;
							tags_to_replace.add((String) tag_to_add.get(0));
							
							Example example = new Example(Integer.parseInt(parts[1].replaceAll("\\D+","")), null, tags_to_replace);
							System.out.println("REPLACE TAG : " + example);
							scenario_event.setExample(example);
						}

						this.events.add(scenario_event); // add the current event to the map
					}
					
					// DEV : juste pour pas lire tout le fichier
//					loop++;
//					if (loop>500){
//						break;
//					}
				}
			}
			
			this.examples_colors = generateUniqueColors(this.examples);
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
		for (int i = 1 ; i < level_prototype.size() ; i+=4) {
			ArrayList<String> level_atoms = (ArrayList<String>) level_prototype.get(i);
			List<String> atoms = new ArrayList<String>(Arrays.asList(level_atoms.get(0).split(", ")));
			prototypes.add(new Prototype(atoms));
		}

		return new Hypothesis(prototypes, false);
	}

	@SuppressWarnings("unchecked")
	public Example parseExample(ArrayList<Object> content) {
		ArrayList<Object> level_1 = (ArrayList<Object>) content.get(0);
		
		String level_id = (String) level_1.get(0);
		int id = Integer.parseInt(level_id.substring(0, level_id.length()-2));
		
		ArrayList<String> level_atoms = (ArrayList<String>) level_1.get(1);
		List<String> atoms = new ArrayList<String>(Arrays.asList(level_atoms.get(0).split(", ")));
		
		ArrayList<String> level_tags = (ArrayList<String>) level_1.get(3);
		List<String> tags = new ArrayList<String>();
		if (level_tags.size() != 0) {
			tags = new ArrayList<String>(Arrays.asList(level_tags.get(0).split(", ")));
		}

		return new Example(id,atoms,tags);
	}

	/**
	 * Compute a list of unique colors in order to assign each one to an example's tag.
	 * Only the hue varies in order to have the most different possible colors.
	 * Weak point : chance can make two consecutive examples having a very similar color.
	 * @param examples
	 * @return 
	 */
	public HashMap<Integer, String> generateUniqueColors(List<Example> examples) {
		List<Color> colors = new ArrayList<Color>();
		HashMap<Integer,String> examples_colors = new HashMap<Integer,String>();

		// fill an array of colors
		for (int i=0 ; i<examples.size() ; i++) {
			colors.add(Color.getHSBColor((float) i / examples.size(), 1, 1));
		}

		Collections.shuffle(colors); // colors are mixed randomly

		// transform each color in a readable format by GraphStream and assign it to an example's id
		for (int i=0 ; i<examples.size() ; i++) {
			String rgb = "rgb(" + colors.get(i).getRed() + "," + colors.get(i).getGreen() + "," + colors.get(i).getBlue() + ")";
			examples_colors.put(examples.get(i).getId(),rgb);
		}
		
		return examples_colors;
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

	public HashMap<Integer, String> getExamples_colors() {
		return examples_colors;
	}

	public void setExamples_colors(HashMap<Integer, String> examples_colors) {
		this.examples_colors = examples_colors;
	}

}
