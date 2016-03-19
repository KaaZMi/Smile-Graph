package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Observable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Model extends Observable {
	
	private Graph graph;
	private ArrayList<String> stockedLines; //contient les lignes du fichier log en mémoire
	
	public Model() {
		this.graph = new MultiGraph("embedded");
		this.graph.setStrict(false);
		this.graph.setAutoCreate(true);
		this.graph.addAttribute("ui.antialias");
	}
	
	public void openXML(String path) {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//final ArrayList<String> arcs = new ArrayList<String>();
		LinkedHashMap<String, ArrayList<String>> edges = new LinkedHashMap<String, ArrayList<String>>();

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
						System.out.println("nb_agents : " + nb_agents);
					}
					else {
						final Element arc = (Element) racineNoeuds.item(n);

						// affichage de i et de j
						System.out.println("\n*************ARC************");
						System.out.println("i : " + arc.getAttribute("i"));
						System.out.println("j : " + arc.getAttribute("j"));
						
						ArrayList<String> values = new ArrayList<String>();
						values.add(arc.getAttribute("i"));
						values.add(arc.getAttribute("j"));
						// ID de l'arc avec tiret entre les sommets pour différencer par exemple
					    // l'arc 11 à 2 et l'arc 1 à 12 (sans tiret, l'ID serait 112 pour les deux).
						edges.put(arc.getAttribute("i")+"-"+arc.getAttribute("j"), values);
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
		
		addEdges(edges);
	}
	
	public void openLOG(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = null;
			stockedLines = new ArrayList<String>();
			
			while ((line = br.readLine()) != null) {  
				stockedLines.add(line);
			}
			
			br.close();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void addEdges(LinkedHashMap<String, ArrayList<String>> edges) {
		Iterator<String> iterator = edges.keySet().iterator();
		
		while (iterator.hasNext()) {
			String id = (String) iterator.next(); // key = id
		    ArrayList<String> values = (ArrayList<String>) edges.get(id); // values = i et j
		    
		    this.graph.addEdge(id, values.get(0), values.get(1)); 
		}
		
		for (Node node : this.graph) {
	        node.addAttribute("ui.label", node.getId());
	    }
		
	    setChanged();
		notifyObservers();
	}
	
	public Graph getGraph() {
		return graph;
	}

}
