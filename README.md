# Projet Logiciel DAC

## Data Flow Visualization for a Multi-Agent Collaborative Learning System
<b><i>(Français: Visualisation de la Circulation des Données dans un Apprentissage Collaboratif Multiagent)</i></b>


### Description

#### English
The purpose of this project is to setup a tool that visulazes the propagation of examples in a multi-agent system collaboratively learning a concept.  Collaborative learning is based on a critical learning protocol, where an agent modifying its assumption submits it to its neighbors who can then validate or send an counter-example, which will result in reviewing the assumption and storing it in memory. We are interested in the flow of counter-examples, in particular when the learning data is not evenly distributed among the agents. As a result, the main task is to graphically visualize the changes in the distribution of examples in such a scenario.

#### Français
Il s'agit de mettre en place un outil de visualisation de la propagation des exemples dans un système multi-agent apprenant collaborativement un concept. L'apprentissage collaboratif est basé sur un protocole apprenant critique, où un agent modifiant son hypothèse soumet celle-ci à ses voisins qui peuvent alors la valider ou envoyer un contre-exemple, qui donnera lieu à une révision de l'hypothèse et sera mémorisé. On s'intéresse ici à la circulation de ces contre-exemples, en particulier lorsque les données d'apprentissage ne sont pas distribuées de façon homogène entre les agents.
La tâche principale est alors de pouvoir visualiser graphiquement (a priori en affichant le graphe des agents, en coloriant les nœuds agents en fonction de leur mémoire d'exemples) l'évolution de la répartition des exemples dans un tel scénario. 


### How to Import the Project

1. The project needs to be imported as an Eclipse Project into the Eclipse IDE
2. The graphStream library files need to be downloaded and added to the project <b>and</b> to the build path. These 3 files are: <b>gs-core.jar</b> , <b>gs-algo.jar</b> and <b>gs-ui.jar</b>.
 
The library files can be downloaded from the following link: http://graphstream-project.org/download/

<u><b>IMPORTANT:</b></u> It is highly recommended that you use the latest version of the graphStream library (by the time of this writing it is release 1.3).

### References

*Gauvain Bourgne, Amal El Fallah-Seghrouchni, Henry Soldano. Learning in a Fixed or Evolving Network of Agents. IAT 2009: 549-556.

*Gauvain Bourgne, Dominique Bouthinon, Amal El Fallah-Seghrouchni, Henry Soldano. Collaborative Concept Learning: Non Individualistic vs Individualistic Agents. ICTAI 2009: 653-65


