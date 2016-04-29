package model;

import java.util.ArrayList;

public class ScenarioEvent {
	private String css_class;
	private String source;
	private String destination;
	private String type;
	private String content;
	private ArrayList<Formula> formulas = new ArrayList<Formula>();

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
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	@SuppressWarnings("unchecked")
	public void parseContent(ArrayList<Object> content) {
		Object level_1 = (ArrayList<Object>) content.get(0);
		Object level_2 = ((ArrayList<Object>) level_1).get(1);
		for (int i = 0 ; i<((ArrayList<Object>) level_2).size() ; i+=3) {
			Object formula = ((ArrayList<Object>) level_2).get(i);
			this.formulas.add(new Formula(formula));
		}
	}
	
	public ArrayList<Formula> getFormulas() {
		return formulas;
	}

	@Override
	public String toString() {
		return "ScenarioEvent [css_class=" + css_class + ", source=" + source + ", destination="
				+ destination + ", type=" + type + ", content=" + content + ", formulas=" + formulas + "]";
	}

}
