package model;

public class ScenarioEvent {
	private String css_class;
	private String source;
	private String destination;
	private String type;
	private Example example;
	private Hypothesis hypothesis;

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
	
	public Example getExample() {
		return example;
	}

	public void setExample(Example example) {
		this.example = example;
	}
	
	public Hypothesis getHypothesis() {
		return hypothesis;
	}

	public void setHypothesis(Hypothesis hypothesis) {
		this.hypothesis = hypothesis;
	}

	@Override
	public String toString() {
		return "ScenarioEvent [css_class=" + css_class + ", source=" + source + ", destination=" + destination
				+ ", type=" + type + ", example=" + example + ", hypothesis=" + hypothesis + "]";
	}
	

}
