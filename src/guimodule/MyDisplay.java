package guimodule;

import processing.core.PApplet;

public class MyDisplay extends PApplet{

	// setup() creates our canvas and executes once
	// Runs at beginning of GUI
	public void setup() {
		
		size(600, 600);
		background(255, 255, 0);
	}
	
	// draw() loops every time our canvas refreshes/gets redrawn
	// Used to display images on canvas
	public void draw() {
		
		fill(51, 153, 255);
		ellipse(width/2, height/2, width, height);
		fill(0, 0, 0);
		ellipse(width/4, height/4, 30, 30);
		ellipse(width*3/4, height/4, 30, 30);
		arc(width/2, height*3/4, 200, 200, 0, PI);
	}
}
