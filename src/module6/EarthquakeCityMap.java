package module6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import module5.CommonMarker;
import module5.CityMarker;
import module5.EarthquakeMarker;
import module5.OceanQuakeMarker;
import module5.LandQuakeMarker;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Rudolf Pham
 * Date: December 16, 2015
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	

	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(1920, 960, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 300, 50, 1600, 900, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
		//earthquakesURL = "test1.atom";
		//earthquakesURL = "test2.atom";
		
		// Uncomment this line to take the quiz
		earthquakesURL = "quiz2.atom";
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    // could be used for debugging
	    printQuakes();
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	    // Call sortAndPrint
	    sortAndPrint(20);
	    
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	// This method does the following:
	// 1) Creates a new array from the list of earthquake markers
	// 2) Sorts the array of earthquake markers in reverse order of magnitude
	// 3) Prints out the top numToPrint earthquakes
	// Note: If numToPrint > size(quakeMarkers), then the program should stop and not crash
	private void sortAndPrint(int numToPrint) {
		
		Object[] quakes = quakeMarkers.toArray();
		
		for (int i = 0; i < quakes.length; i++) {
			String quake = ((EarthquakeMarker) quakes[i]).getTitle();
			quakes[i] = quake;
		}
		
		Arrays.sort(quakes, Collections.reverseOrder());
		
		for (int i = 0; i < numToPrint; i++) {
			System.out.println(quakes[i]);
		}
		
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
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
		//loop();
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
				lastSelected = (CommonMarker) marker;	// Set lastSelected as first marker found
				marker.setSelected(true);
				return;
			}
		}
	}
	
	/** The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes 
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked()
	{
		if (lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
			
		} else if (lastClicked == null) {
			
			selectCityMarkerIfClicked();
			
			if (lastClicked == null) {
				selectQuakeMarkerIfClicked();
			}
		}
	}
	
	// Helper method that will check if a city marker was clicked on
	private void selectCityMarkerIfClicked() {
		
		if (lastClicked != null) return;
		
		// Find which marker was clicked and set it to lastClicked. 
		// Then check if all other cities are hidden. If not, hide them.
		// Afterwards, check which earthquake markers are within range.
		// If not in range, hide them.
		for (Marker marker : cityMarkers) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker) marker;	// Found the lastClicked marker
				
				// Hide the other cities
				for (Marker m : cityMarkers) {
					if (marker != m) {
						m.setHidden(true);
					}
				}
				
				// Hide the earthquakes not within threat range
				for (Marker earthquake : quakeMarkers) {
					EarthquakeMarker quake = (EarthquakeMarker) earthquake;
					if (marker.getDistanceTo(earthquake.getLocation()) > quake.threatCircle()) {
						quake.setHidden(true);
					}
				}
				return;
			}
		}
	}
	
	// Helper method that will check if an earthquake was clicked on
	private void selectQuakeMarkerIfClicked() {
		
		if (lastClicked != null) return;
		
		// Find which quake marker was clicked and set to lastClicked
		// Then, hide all other earthquake markers.
		// Check which cities are within range.
		// If not in range, hide them.
		for (Marker quakeMarker : quakeMarkers) {
			if (!quakeMarker.isHidden() && quakeMarker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker) quakeMarker;
				
				// Hide all other earthquake markers
				for (Marker earthquake : quakeMarkers) {
					if (earthquake != quakeMarker) {
						earthquake.setHidden(true);
					}
				}
				
				// Hide cities not within threat range
				for (Marker city : cityMarkers) {
					EarthquakeMarker quake = (EarthquakeMarker) quakeMarker;
					if (quakeMarker.getDistanceTo(city.getLocation()) > quake.threatCircle()) {
						city.setHidden(true);
					}
				}
				return;
			}
		}
	}
	
	// loop over and unhide all markers
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
	
	// helper method to draw key in GUI
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		rect(50, 50, 280, 400);
		textSize(20);
		
		// Fill legend with text
		fill(0);
		text("Earthquake Key", 110, 100);
		text("City Marker", 160, 140);
		text("Land Quake", 160, 180);
		text("Ocean Quake", 160, 220);
		
		// City Marker
		fill(255, 0, 0);
		triangle(115, 140, 95, 140, 105, 125);
		
		// Land Quake
		fill(255, 255, 255);
		ellipse(105, 175, 20, 20);
		
		// Ocean Quake
		rect(95, 205, 20, 20);
		
		fill(0, 0, 0);
		text("Size ~ Magnitude", 110, 260);
		
		text("Shallow", 160, 300);
		text("Intermediate", 160, 340);
		text("Deep", 160, 380);
		text("Past Day", 160, 420);
		
		fill(255, 255, 0);
		ellipse(105, 290, 20, 20);
		fill(0, 0, 255);
		ellipse(105, 330, 20, 20);
		fill(255, 0, 0);
		ellipse(105, 370, 20, 20);
		
		fill(255);
		ellipse(105, 410, 20, 20);
		line(95, 420, 115, 400);
		line(95, 400, 115, 420);
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {
		
		// IMPLEMENT THIS: loop over all countries to check if location is in any of them
		// If it is, add 1 to the entry in countryQuakes corresponding to this country.
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		
		// not inside any country
		return false;
	}
	
	// prints countries with number of earthquakes
	private void printQuakes() {
		
		int totalWaterQuakes = quakeMarkers.size();
		
		for (Marker country : countryMarkers) {
			
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			
			for (Marker marker : quakeMarkers) {
				
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("\nOCEAN QUAKES: " + totalWaterQuakes);
	}
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake feature if 
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

}
