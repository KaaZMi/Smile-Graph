package model;

public class Formula {
	private String content;
	private boolean accepted;
	private boolean consistant;
	
	public Formula(Object formula) {
		setContent(formula.toString());
		setAccepted(false);
		setConsistant(false);
	}

	public Formula(Object formula, boolean accepted) {
		setContent(formula.toString());
		setAccepted(accepted);
	}
	
	public Formula(Object formula, boolean accepted, boolean consistent) {
		setContent(formula.toString());
		setAccepted(accepted);
		setConsistant(consistent);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	
	public boolean isConsistant() {
		return consistant;
	}

	public void setConsistant(boolean consistant) {
		this.consistant = consistant;
	}
	
	public boolean compareTo(Formula formula) {
		return this.content.equals(formula.getContent());
	}

	@Override
	public String toString() {
		return "Formula [content=" + content + ", accepted=" + accepted + ", consistant=" + consistant + "]";
	}

}
