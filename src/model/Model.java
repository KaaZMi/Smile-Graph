package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Observable;

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
	private int cursor = 0; // current position of the scenario
	
	private boolean graphLoaded = false;
	private boolean scenarioLoaded = false;
	private int nbAgents = 0;

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
			String color = null;
			boolean etudier_cette_ligne = false;

			// tant qu'une ligne non vide est lisible
			while (((line = br.readLine()) != null) && !("".equals(line))) {
				// toute ligne sans parenth�se finale n'est pas � traiter
				// DEV : on ne traite pas les lignes System
				if ( line.substring(line.length()-1, line.length()).equals(")") && !line.contains("System") ) {
					// suppression de la parenth�se finale inutile
					line = line.substring(0, line.length()-1);

					// s�paration de la ligne en parties : agents / type ( / contenu )
					String[] parts = line.split(":");

					// TODO traiter les diff�rents type de messages (couleur � donner, ...)
					etudier_cette_ligne = false;
					color = "black";
					if (parts[1].contains("Nouveaux Exemples")) {
						etudier_cette_ligne = true;
						color = "red";
					}
					else if (parts[1].contains("Hypothese a tester")) {
						etudier_cette_ligne = true;
						color = "blue";
					}
					else if (parts[1].contains("Hypothese SMA-consistante")) {
						etudier_cette_ligne = true;
						color = "purple";
					}
					else if (parts[1].contains("Contre Exemples")) {
						etudier_cette_ligne = true;
						color = "yellow";
					}
					else if (parts[1].contains("Nouvelle Hypothese a tester")) {
						etudier_cette_ligne = true;
						color = "green";
					}
					else if (parts[1].contains("Message de")) {
						etudier_cette_ligne = true;
						color = "orange";
					}
					else {
						// aucun des cas pr�c�dents n'est v�rifi� donc la ligne courante ne nous int�ressent pas
						etudier_cette_ligne = false;
					}

					if (etudier_cette_ligne) {
						// separate source and destination
						String[] agents = parts[0].trim().split("->");
						
						ScenarioEvent scenario_event = new ScenarioEvent();
						scenario_event.setColor(color);
						scenario_event.setSource(agents[0].trim().replaceAll("[^\\d.]", ""));
						scenario_event.setDestination(agents[1].trim().replaceAll("[^\\d.]", ""));
						scenario_event.setType(parts[1].trim());
						if (parts.length > 2) {
							scenario_event.setContent(parts[2].trim());
						}

						// add the current event to the map
						this.events.add(scenario_event);
					}

					// DEV : juste pour pas lire tout le fichier
					loop++;
					if (loop>225){
						break;
					}
				}
			}

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
