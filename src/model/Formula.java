package model;

public class Formula {
	private String content;
	private boolean accepted;
	
	public Formula(Object formula) {
		setContent(formula.toString());
		setAccepted(false);
	}

	public Formula(Object formula, boolean accepted) {
		setContent(formula.toString());
		setAccepted(accepted);
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

	@Override
	public String toString() {
		return "Formula [content=" + content + ", accepted=" + accepted + "]";
	}

}
