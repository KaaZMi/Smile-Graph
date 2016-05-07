package model;

import java.util.ArrayList;

public class ScenarioEvent {
	private String css_class;
	private String source;
	private String destination;
	private String type;
	private int group;
	private ArrayList<Formula> formulas = new ArrayList<Formula>();
	private Example example;

	public String getCSSClass() {
		return css_class;
	}
	
	public void setCSSClass(String css_class) {
		this.css_class = css_class;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getDestination() {
		return destination;
	}
	
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}
	
	public ArrayList<Formula> getFormulas() {
		return formulas;
	}
	
	public void setFormulas(ArrayList<Formula> formulas) {
		this.formulas = formulas;
	}
	
	public Example getExample() {
		return example;
	}

	public void setExample(Example example) {
		this.example = example;
	}
	

	@Override
	public String toString() {
		return "ScenarioEvent [css_class=" + css_class + ", source=" + source + ", destination="
				+ destination + ", type=" + type + ", group=" + group + ", formulas=" + formulas + "]";
	}

}
