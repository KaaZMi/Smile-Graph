package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Observable;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Model extends Observable {

	private LinkedHashMap<String, ArrayList<String>> edges = new LinkedHashMap<String, ArrayList<String>>();
	private ArrayList<ScenarioEvent> events = new ArrayList<ScenarioEvent>();
	private LinkedHashMap<Integer, Formula> unique_hyp = new LinkedHashMap<Integer, Formula>();
	private int cursor = 0; // current position of the scenario
	
	private boolean graphLoaded = false;
	private boolean scenarioLoaded = false;
	private int nbAgents = 0;
	private static int str_index = 0; // index to browse the content of a message

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
						String i = arc.getAttribute("i");
						String j = arc.getAttribute("j");
						String id = "";
						
						// IMPORTANT : naming convention of ID : AB-BA with A < B
						if (Integer.parseInt(i) < Integer.parseInt(j)) {
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
						System.out.println("\n*************ARC************");
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
			ArrayList<Formula> previous_formulas = new ArrayList<Formula>();
			int group = 0;

			// tant qu'une ligne non vide est lisible
			while (((line = br.readLine()) != null) && !("".equals(line))) {
				// toute ligne sans parenthèse finale n'est pas à traiter
				// DEV : on ne traite pas les lignes System
				if ( line.substring(line.length()-1, line.length()).equals(")") && !line.contains("System") ) {
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
						
						ScenarioEvent scenario_event = new ScenarioEvent();
						scenario_event.setCSSClass(css_class);
						scenario_event.setSource(agents[0].trim().replaceAll("[^\\d.]", ""));
						scenario_event.setDestination(agents[1].trim().replaceAll("[^\\d.]", ""));
						scenario_event.setType(parts[1].trim());
						// if the event has a content
						if (parts.length > 2) {
							ArrayList<Object> content = buildList(parts[2].trim());
							ArrayList<Formula> formulas = parseContent(content);
							/*
							 * We wish to number each hypothesis, so we are fulfilling 
							 * a unique values map.
							 */
							/*if (parts[1].contains("Hypothese a tester") || parts[1].contains("Hypothese confirmee") || parts[1].contains("Hypothese SMA-consistante")) {
								int num = 0;
								for (Formula f : formulas) {
									boolean formula_already_viewed = false;
									for (Entry<Integer, Formula> entry : unique_hyp.entrySet()) {
								        if (entry.getValue().compareTo(f)) {
								        	formula_already_viewed = true;
								        	f.setNum(num); // same number as the hypothesis found
								        }
								        num++;
								    }
									
									if (!formula_already_viewed) {
										num = unique_hyp.size();
										unique_hyp.put(num, f);
										f.setNum(num); // new number
								    }
								}
							}*/
							
							scenario_event.setFormulas(formulas);
							
							// compare the formulas of this event to the formulas of the previous event
							if (!compareFormulas(formulas, previous_formulas)) {
								group++;
								previous_formulas = formulas;
							}
							
							scenario_event.setGroup(group);
							
							str_index = 0;
						}

						this.events.add(scenario_event); // add the current event to the map
					}

					// DEV : juste pour pas lire tout le fichier
					loop++;
					if (loop>225){
						break;
					}
				}
			}
			
			// DEV : vérifier la numérotation
//			for (ScenarioEvent event : events) {
//				System.out.println(event.toString());
//		    }

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
	 * Transform a string which contains nested array to a JAVA array
	 * @param str
	 * @return array
	 */
	public ArrayList<Object> buildList(String str) {
		ArrayList<Object> list = new ArrayList<>(); // the list can contain Strings and sub-lists
		String stack = "";

		while (str_index < str.length()) {
			char c = str.charAt(str_index++);
			
			if (c == '[') {
				if (!stack.trim().equals("") && !stack.trim().equals(",")) {
					// add the current stack to the current list
					list.add(stack.trim());
					stack = "";
				}
				list.add(buildList(str)); // new sublist
			}
			else if (c == ']') {
				if (!stack.trim().equals("") && !stack.trim().equals(",")) {
					// add the current stack to the current list
					list.add(stack.trim());
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
	public ArrayList<Formula> parseContent(ArrayList<Object> content) {
		Object level_1 = (ArrayList<Object>) content.get(0);
		Object level_2 = ((ArrayList<Object>) level_1).get(1);
		ArrayList<Formula> formulas = new ArrayList<Formula>();
		
		for (int i = 0 ; i<((ArrayList<Object>) level_2).size() ; i+=3) {
			Object formula = ((ArrayList<Object>) level_2).get(i);
			formulas.add(new Formula(formula));
		}
		
		return formulas;
	}
	
	public boolean compareFormulas(ArrayList<Formula> formulas1, ArrayList<Formula> formulas2) {
		int nb_equals = 0;
		for (Formula f1 : formulas1) {
			for (Formula f2 : formulas2) {
				if (f1.compareTo(f2)) {
					nb_equals++;
				}	
			}	
		}
		
		if (nb_equals*2 == formulas1.size()+formulas2.size()) {
			return true;
		}
		
		return false;
	}
	
	public LinkedHashMap<String, ArrayList<String>> getEdges() {
		return edges;
	}
	
	public ArrayList<ScenarioEvent> getEvents() {
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

}
