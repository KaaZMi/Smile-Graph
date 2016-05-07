package view;

import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

public class mySprite extends Sprite {
	private double step = 0.01;
	private boolean direction = true;
	
	public mySprite(String identifier, SpriteManager manager) {
		super(identifier, manager);
	}
	
	public void setDirection(boolean direction) {
		this.direction = direction;
	}
	
	/**
	 * Defines the position and direction of the sprite depending on the source and destination.
	 * @param i Source
	 * @param j Destination
	 */
	public void initEtat(String i, String j) {
		if ( i.contains("System") || j.contains("System") ) {
			if ( i.contains("System") ) {
				this.setDirection(true);
				this.setPosition(0);
			}
			else if ( j.contains("System") ) {
				this.setDirection(false);
				this.setPosition(1);
			}
		}
		
		else {
			i = i.substring(i.length()-1);
			j = j.substring(j.length()-1);
			if (Integer.parseInt(i) < Integer.parseInt(j)) {
				this.setDirection(true);
				this.setPosition(0);
			}
			else {
				this.setDirection(false);
				this.setPosition(1);
			}
		}
	}

	/**
	 * Move the sprite in the appropriate direction.
	 */
	public boolean move() {
		double p = getX();
		
		if(direction)
			p += step;
		else
			p -= step;
		
		// the sprite came to the end of the edge
		if(p<0 || p>1) {
			return false;
		}
		else {
			setPosition(p);
			return true;
		}
	}
}