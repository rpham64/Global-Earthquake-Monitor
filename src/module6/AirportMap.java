package module6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;


/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMap extends PApplet {
	
	private static final long serialVersionUID = 1L;
	
	UnfoldingMap map;
	private List<Marker> airportList;
	List<Marker> routeList;
	
	private SimplePointMarker lastSelected;
	private SimplePointMarker lastClicked;
	
	public void setup() {
		// setting up PAppler
		size(1920, 960, OPENGL);
		
		// setting up map and default events
		map = new UnfoldingMap(this, 300, 50, 1600, 900);
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		
		// list for markers, hashmap for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		
		// create markers from features
		for(PointFeature feature : features) {
			AirportMarker m = new AirportMarker(feature);
	
			m.setRadius(5);
			airportList.add(m);
			
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
		
		}
		
		
		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		
		for(ShapeFeature route : routes) {
			
			// get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// get locations for airports on route
			if(airports.containsKey(source) && airports.containsKey(dest)) {
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
			}
			
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
			
			//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
			routeList.add(sl);
		}
		
		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		map.addMarkers(routeList);
		
		// Hide all route markers
		for (Marker marker : routeList) {
			marker.setHidden(true);
		}
		
		map.addMarkers(airportList);
		
	}
	
	public void draw() {
		background(0);
		map.draw();
		
	}
	
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(airportList);
		selectMarkerIfHover(routeList);
		
	}
	
	// If there is a marker selected 
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}
		
		for (Marker marker : markers) {
			if (marker.isInside(map, mouseX, mouseY)) {
				lastSelected = (SimplePointMarker) marker;	// Set lastSelected as first marker found
				marker.setSelected(true);
				return;
			}
		}
	}
	
	/*The event handler for mouse clicks.
	This method will select an airport on click
	and show the routes coming from that airport.*/
	
	@Override
	public void mouseClicked()
	{
		if (lastClicked != null) {
			hideMarkers();
			lastClicked = null;
			
		} else if (lastClicked == null) {
			
			selectAirportMarkerIfClicked();
			
		}
	}
	
	private void selectAirportMarkerIfClicked() {
		
		if (lastClicked != null) return;
		
		// When clicked, show all airport routes from this airport
		// and the airports connected to this airport
		
		for (Marker marker : airportList) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (SimplePointMarker) marker;	// Set lastClicked to found marker
				break;
			}
		}
				
		// Check which routes are coming from lastClicked
		for (Marker route : routeList) {
			
			SimpleLinesMarker route_start = (SimpleLinesMarker) route;
			if ((route_start.getLocation(0)).equals(lastClicked.getLocation())) {
				route.setHidden(false);
			}
			
		}

	}

	// loop over and unhide all route markers
	private void hideMarkers() {
		
		for(Marker marker : routeList) {
			marker.setHidden(true);
		}
		
	}

}
