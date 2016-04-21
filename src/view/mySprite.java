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
	 * Move the sprite in the appropriate direction.
	 */
	public boolean move() {
		double p = getX();
		
		if(direction)
			p += step;
		else
			p -= step;
		
		
		if(p<0 || p>1) {
			return false;
		}
		else {
			setPosition(p);
			return true;
		}
	}
}