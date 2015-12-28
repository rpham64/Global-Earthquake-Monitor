package module6;

import java.util.List;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PConstants;
import processing.core.PGraphics;

/** 
 * A class to represent AirportMarkers on a world map.
 *   
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMarker extends SimplePointMarker {
	public static List<SimpleLinesMarker> routes;
	
	// Records whether this marker has been clicked (most recently)
	protected boolean clicked = false;
	
	public AirportMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
	
	}
	
	// Getter method for clicked field
	public boolean getClicked() {
		return clicked;
	}
	
	// Setter method for clicked field
	public void setClicked(boolean state) {
		clicked = state;
	}
		
	public void draw(PGraphics pg, float x, float y) {

		if (!hidden) {
			drawMarker(pg, x, y);
			if (selected) {
				showTitle(pg, x, y);
			}
		}
	}
	
	public void drawMarker(PGraphics pg, float x, float y) {
		pg.fill(192, 192, 192);
		pg.ellipse(x, y, 5, 5);
		
		
	}

	public void showTitle(PGraphics pg, float x, float y) {
		 // show rectangle with title
		
		pg.pushStyle();	// Saves current style settings 
		
		String name = "Name: " + getName();
		String location = "Location: " + getCity() + ", " + getCountry();
		String code = "Code: " + getCode();
		String altitude = "Altitude: " + getAltitude();
		
		
		// Create rectangle display
		pg.rectMode(PConstants.CORNER);
		pg.stroke(110);	// Sets color used to draw lines and borders around shapes
		pg.fill(255, 255, 255);
		pg.rect(x, y+15, pg.textWidth(location) + 6, 55, 5);
		
		// Add title
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.fill(0);
		pg.text(name, x + 3, y + 18);
		pg.text(location, x + 3, y + 28);
		pg.text(code, x + 3, y + 42);
		pg.text(altitude, x + 3, y + 52);
		
		pg.popStyle();	// Removes current style settings and restores the previous one
		// show routes
		
		
	}
	
	public String getCountry() {
		return (String) getProperty("country");
	}
	
	public float getAltitude() {
		return Float.parseFloat(getProperty("altitude").toString());
	}
	
	public String getCode() {
		return (String) getProperty("code");
	}
	
	public String getCity() {
		return (String) getProperty("city");
	}
	
	public String getName() {
		return (String) getProperty("name");
	}
	
}
