package model;

public class ScenarioEvent {
	private String color;
	private String source;
	private String destination;
	private String type;
	private String content;

	public String getColor() {
		return color;
	}
	
	public void setColor(String color) {
		this.color = color;
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
	
	@Override
	public String toString() {
		return "ScenarioEvent [color=" + color + ", source=" + source + ", destination=" + destination + ", type="
				+ type + ", content=" + content + "]";
	}

}
