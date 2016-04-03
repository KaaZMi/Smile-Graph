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
	private LinkedHashMap<Integer, ArrayList<String>> events = new LinkedHashMap<Integer, ArrayList<String>>();
	private int cursor = 0; // current position of the scenario
	
	private boolean graphLoaded = false;
	private boolean scenarioLoaded = false;
	private int nbAgents = 0;

	public boolean openXML(String path) {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			// création d'un parseur et d'un document
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document= builder.parse(new File(path));

			// récupération de l'element racine
			final Element racine = document.getDocumentElement();

			// récupération du nombre d'agents et des arcs
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

						// ID de l'arc avec tiret entre les sommets pour différencer par exemple
						// l'arc 11 à 2 et l'arc 1 à 12 (sans tiret, l'ID serait 112 pour les deux).
						String i = arc.getAttribute("i");
						String j = arc.getAttribute("j");
						String id = "";
						
						// convention de notation de l'ID : AB-BA avec A < B
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
						
						// affichage de l'id de l'arc
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
			int i = 0;
			String color = null;
			boolean etudier_cette_ligne = false;

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
						// aucun des cas précédents n'est vérifié donc la ligne courante ne nous intéressent pas
						etudier_cette_ligne = false;
					}

					if (etudier_cette_ligne) {
						// séparation des agents
						String[] agents = parts[0].trim().split("->");

						// définition et remplissage des valeurs de l'événement courant
						ArrayList<String> values = new ArrayList<String>();
						
						// couleur que prendra l'arc à son "exécution"
						values.add(color);
						
						// agents en retirant "Ag"
						values.add(agents[0].trim().replaceAll("[^\\d.]", "")); 
						values.add(agents[1].trim().replaceAll("[^\\d.]", ""));
						
						values.add(parts[1].trim()); // type du message
						if (parts.length > 2) {
							values.add(parts[2].trim()); // contenu du message
						}

						// ajout de l'évenement courant à la map
						this.events.put(i, values);

						// DEV : vérification du stockage de l'événement
						for (String value: this.events.get(i)) {
						    System.out.print(value);
						}
						System.out.println("");
						
						i++;
					}

					// DEV : juste pour pas lire tout le fichier
					loop++;
					if (loop>225){
						break;
					}
				}
			}

			// DEV : vérification du filtrage effectué
			//System.out.println(events.size());

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
	
	public LinkedHashMap<Integer, ArrayList<String>> getEvents() {
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
