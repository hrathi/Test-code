package myapps;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MapsArguments;
import net.rim.blackberry.api.maps.MapView;
import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.lbs.maps.*;
import net.rim.device.api.lbs.maps.model.*;
import net.rim.device.api.lbs.maps.ui.*;
import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.NumericChoiceField;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.ui.extension.container.ZoomScreen;
import net.rim.device.api.util.Arrays;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.*;
import com.drew.metadata.wrapper.*;

import javax.microedition.location.Coordinates;
import net.rim.device.api.lbs.MapField;

//public class MapMyPics extends MainScreen {
//
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		MapMyPics theApp = new MapMyPics();
//		theApp.enterEventDispatcher();
//	}
//
//	public MapMyPics() {
//		pushScreen(new MapScreen());
//	}
//}

//class MapScreen extends FullScreen {
//	public MapScreen() {
//		super(FullScreen.DEFAULT_CLOSE | FullScreen.DEFAULT_MENU);
//		RichMapField map = MapFactory.getInstance().generateRichMapField();
//		MapAction action = map.getAction();

public class MapMyPics extends UiApplication {
	
	/*
	static String _kmlDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
					 "<kml xmlns=\"http://www.opengis.net/kml/2.2\">" + 
					 	"<Folder>" + 
					 		"<name>Pictures</name>" + 
					 		"<visibility>0</visibility>"+ 
					 		"<open>1</open>" + 
					 		"<GroundOverlay>" + 
					 			"<name>Glowing</name>" +
					 			"<visibility>1</visibility>" +
					 			"<description>Hot Spot</description>" +
					 			//"<Icon>" +
					 			//	"<href>file:///SDCard/BlackBerry/camera/1.png</href>" +
					 			//	"<viewBoundScale>0.75</viewBoundScale>" +
					 			//	"</Icon>" +
					 			"<LatLonBox>" +
					 				"<north>42.474677</north>" +
					 				"<south>42.474677</south>" +
					 				"<east>-71.061459</east>" +
					 				"<west>-71.061459</west>" +
					 			"</LatLonBox>" +
					 		"</GroundOverlay>" +
						"</Folder>" +
					"</kml>";
	*/
	
	public static void main(String[] args) 
	{
		if (args != null && args.length > 0)
        {
            if (args[0].equals("startup"))
            {
                // Register an ApplicationMenuItem on device startup.
                ApplicationMenuItemRepository amir = ApplicationMenuItemRepository.getInstance();
                ApplicationDescriptor ad_startup = ApplicationDescriptor.currentApplicationDescriptor();
                ApplicationDescriptor ad_gui = new ApplicationDescriptor(ad_startup , "gui", new String[]{"gui"});
                amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_FILE_EXPLORER_BROWSE , new MapsMenuItem() , ad_gui);                
            }
            else if (args[0].equals("gui"))
            {               
                // App was invoked by our ApplicationMenuItem. Call default
                // constructor for GUI version of the application.
                MapMyPics app = new MapMyPics();
                
                // Make the currently running thread the application's event
                // dispatch thread and begin processing events.
                app.enterEventDispatcher();                
            }
        }
	}
	
	private static class MapsMenuItem extends ApplicationMenuItem
	{
		// Constructor
        private MapsMenuItem()
        {
            // Create a new ApplicationMenuItem instance with relative menu 
            // position of 20. Lower numbers correspond to higher placement 
            // in the menu.
            super(0);
        }
        
        /**
         * Returns the name to display in a menu.
         * @return The name to display.
         */
        public String toString()
        {
            return "View by Map";
        }        
        
        /**         
         * Views the map in a MapMenuItemScreen.
         * @see ApplicationMenuItem#run(Object)
         */
        public Object run(Object context)
        {
        	//MapsArguments ma = new MapsArguments(MapsArguments.ARG_KML, getKMLDocument());
    		//Invoke.invokeApplication(Invoke.APP_TYPE_MAPS, ma);
            //  Get the UiApplication instance and display the GUI screen.
            UiApplication app = UiApplication.getUiApplication();
            app.pushScreen( new PicturesMapScreen() );  
            app.requestForeground();
    		
    		return null;
        }	
	}
}


/**
 * A MainScreen class for our UiApplication.
 */            
final class PicturesMapScreen extends MainScreen implements FieldChangeListener, FocusChangeListener
{    
    PicturesMapScreen()
    {
    	FileConnection fc = null;
    	Enumeration rootEnum = null;
    	String root = null;
    	Vector jpegFileNames = new Vector();
    	RichMapField map = MapFactory.getInstance().generateRichMapField();
        add(map);

    	Enumeration e = FileSystemRegistry.listRoots();
    	while (e.hasMoreElements()) {
    		root = (String) e.nextElement();
    		String prefixPath = null;
    		if( root.equalsIgnoreCase("sdcard/") ) 
    		{
    			try 
    			{
    				prefixPath = "file:///SDCard/BlackBerry/camera/";
    				fc = (FileConnection)Connector.open(prefixPath, Connector.READ);
    				addFileNames(jpegFileNames, prefixPath, fc.list());
    				
    				prefixPath = "file:///SDCard/BlackBerry/pictures/";
    				fc = (FileConnection)Connector.open(prefixPath, Connector.READ);
    				addFileNames(jpegFileNames, prefixPath, fc.list());
    				
    			} 
    			catch (Exception ioex) 
    			{
    				Dialog.alert(ioex.toString());
    			} 
    		} 

    		else if( root.equalsIgnoreCase("store/") ) 
    		{
    			
          	 try 
               { 
          		 	prefixPath = "file:///store/home/user/camera/";
          		 	fc = (FileConnection)Connector.open("file:///store/home/user/camera/", Connector.READ);
          		 	addFileNames(jpegFileNames, prefixPath, fc.list());

          		 	prefixPath = "file:///store/home/user/pictures/";
          		 	fc = (FileConnection)Connector.open("file:///store/home/user/camera/", Connector.READ);
          		 	addFileNames(jpegFileNames, prefixPath, fc.list());
               } 
               catch (final Exception ioex) 
               {

              	 UiApplication.getUiApplication().invokeLater( new Runnable() {
              		 public void run() {
                           Dialog.alert(ioex.toString());                			 
              		 }
              	 });

              	 //Dialog.alert(ioex.toString());
               } 
    			 
    		}

    	}


    	for(int i = 0; i < jpegFileNames.size(); i++ ) 
    	{
    		String fileName = (String)jpegFileNames.elementAt(i);
    		File jpegFile = new File(fileName);
    		Metadata metaData = new Metadata();
    		try {
    			metaData = JpegMetadataReader.readMetadata(jpegFile);
    		}catch (JpegProcessingException je) {
    			System.err.println("error 1a");
    			//close();
    		}

    		String strLat = metaData.getLatitude();
    		String strLon = metaData.getLongitude();

    		if( strLat != null && strLon != null ) {
    			double lat = Double.parseDouble(strLat);// * 100000;
    			double lon = Double.parseDouble(strLon);// * 100000;
    			jpegFile.setLat(lat);
    			jpegFile.setLon(lon);
    			//myMapField.addImage(jpegFile);
    			MapDataModel data = map.getModel();

    	        final MyBitmap bitmap = new MyBitmap(fileName, 3, 4);
    	        //bitmap.setChangeListener(this);
    	        //bitmap.setFocusListener(this);
    	        
    			
    			String name = fileName.substring(fileName.lastIndexOf('/')+1);
    	        MapLocation imageLoc = new MapLocation( lat, lon, name, "test" ) {
    	        	protected Bitmap getImage(String name) {
    	        		return bitmap.getImage().getBitmap();
    	        	}
    	        };
    	        
    	        imageLoc.addData(fileName, bitmap);
    	        
    	        Object x = imageLoc.getData(fileName);
    	        if( x instanceof BitmapField ) {
    	        	int a = 0;
    	        }
    	        data.add( (Mappable) imageLoc );
    			//myMapField.addCoordinates(new Coordinates(lat, lon, 0));
    			//action.setCentreAndZoom(new MapPoint(lat, lon), 2);
    		}
    	}
    	
    	map.getMapField().update(true);

    	
    }
    
    public void fieldChanged(Field arg0, int arg1) {
    	if( arg0 instanceof MyBitmap ) {
    		MyBitmap a = (MyBitmap)arg0;
    		UiApplication.getUiApplication().pushScreen(new ZoomScreen(a.getImage()));
    	}
                                    
	}
	
    
	public void addFileNames(Vector names, String prefixPath, Enumeration e) 
	{
		while( e.hasMoreElements() ) 
		{
			String s = (String)e.nextElement();
			
			if( s.toLowerCase().endsWith("jpg") ) {
				names.addElement(prefixPath + s);
			}
		}
	}

	public void focusChanged(Field field, int eventType) {
    	if( field instanceof MyBitmap ) {
    		MyBitmap a = (MyBitmap)field;
    		UiApplication.getUiApplication().pushScreen(new ZoomScreen(a.getImage()));
    	}
		
	}

	
}


class MyBitmap extends BitmapField {
	EncodedImage _img;
	
	public MyBitmap(String imagename, int ratioX, int ratioY) {
		super( null, Field.FOCUSABLE);
		_img = getScaledBitmapImage(imagename, ratioX, ratioY);
		setImage(_img);
	}
	
	public EncodedImage getImage() {
		return _img;
	}
	
	/**
     * @see Screen#navigationClick(int, int)
     */
     protected boolean navigationClick(int status, int time)
     {
         // Push a new ZoomScreen if trackpad or screen is clicked
         UiApplication.getUiApplication().pushScreen(new ZoomScreen(_img));                            
         return true;
     }
     
     
     /**
     * @see Screen#touchEvent(TouchEvent)
     */
     protected boolean touchEvent(TouchEvent message)
     {     
         if(message.getEvent() == TouchEvent.CLICK)
         {
             UiApplication.getUiApplication().pushScreen(new ZoomScreen(_img));
             return true;                            
         }
         return super.touchEvent(message);          
     }
     
     protected void paint(Graphics g) {
    	 super.paint(g);
    	 g.drawBitmap(this.getContentTop(), this.getContentTop(), 50, 50, _img.getBitmap(), this.getContentTop(), this.getContentTop());
    	 
     }
	
	private static EncodedImage getScaledBitmapImage(String imagename, int ratioX, int ratioY){
		
		FileConnection connection=null;
        byte[] byteArray=null;
        EncodedImage image = null;
        Bitmap bitmap=null;
        try
        {
            connection=(FileConnection)Connector.open(imagename);
            if(connection.exists())
            {
                byteArray=new byte[(int)connection.fileSize()];
                InputStream inputStream=connection.openInputStream();
                inputStream.read(byteArray);
                inputStream.close();
                image=EncodedImage.createEncodedImage(byteArray, 0, byteArray.length);
                image= image.scaleImage32(Fixed32.toFP(8), Fixed32.toFP(8));
                
            }
            connection.close();
        }
        catch(Exception e)
        {
            System.out.println("Exception "+e.toString());
        }      

        /*
        if( image != null ) {

			int currentWidthFixed32 = Fixed32.toFP(image.getWidth());
			int currentHeightFixed32 = Fixed32.toFP(image.getHeight());
	
			double ratio = (double)ratioX / (double) ratioY;
			double w = (double) image.getWidth() * ratio; 
			double h = (double)image.getHeight() * ratio;
			int width = (int) w;
			int height = (int) h;
	
			int requiredWidthFixed32 = Fixed32.toFP(width);
			int requiredHeightFixed32 = Fixed32.toFP(height);
	
			int scaleXFixed32 = Fixed32.div(currentWidthFixed32, requiredWidthFixed32);
			int scaleYFixed32 = Fixed32.div(currentHeightFixed32, requiredHeightFixed32);
			image = image.scaleImage32(scaleXFixed32, scaleYFixed32);
			return image.getBitmap();
        }
        */

        return image;
	}

	
}

/*

class MyMapField extends MapField {
	File[] mPoints = new File[0];
	Bitmap mPoint;
    Bitmap mPointsBitmap;
    XYRect mDest;
    XYRect[] mPointDest;
	
	public void addImage(File imageFile) {
    	Arrays.add(mPoints, imageFile);
    	mPoint = getScaledBitmapImage(imageFile.getPath(), 3, 4);
    	zoomToFitPoints();
    	repaintPoints();
    }
	
    protected void zoomToFitPoints() {
    	// zoom to max
    	//setZoom(getMaxZoom());

    	// get pixels of all points
    	int minLeft = getPreferredWidth();
    	int minUp = getPreferredHeight();
    	int maxRight = 0;
    	int maxDown = 0;
    	Coordinates minLeftCoordinates = null;
    	Coordinates minUpCoordinates = null;
    	Coordinates maxRightCoordinates = null;
    	Coordinates maxDownCoordinates = null;
    	for (int i = 0; i < mPoints.length; i++) {
    		XYPoint point = new XYPoint();
    		Coordinates imageLoc = new Coordinates(mPoints[i].getLat(), mPoints[i].getLon(), 0); 
    		convertWorldToField(imageLoc, point);
    		if (point.x <= minLeft) {
    			minLeft = point.x;
    			minLeftCoordinates = imageLoc;
    		}
    		if (point.x >= maxRight) {
    			maxRight = point.x;
    			maxRightCoordinates = imageLoc;
    		}
    		if (point.y <= minUp) {
    			minUp = point.y;
    			minUpCoordinates = imageLoc;
    		}
    		if (point.y >= maxDown) {
    			maxDown = point.y;
    			maxDownCoordinates = imageLoc;
    		}
    	}

    	double moveToLat = maxDownCoordinates.getLatitude() + (minUpCoordinates.getLatitude() - maxDownCoordinates.getLatitude()) / 2;
    	double moveToLong = minLeftCoordinates.getLongitude() + (maxRightCoordinates.getLongitude() - minLeftCoordinates.getLongitude()) / 2;
    	Coordinates moveTo = new Coordinates(moveToLat, moveToLong, 0);
    	moveTo(moveTo);
    	// zoom to min left up, max right down pixels + 1
    	int zoom = getZoom();
    	boolean outOfBounds = false;
    	while (!outOfBounds && zoom > getMinZoom()) {
    		zoom--;
    		setZoom(zoom);
    		XYPoint point = new XYPoint();
    		try {
    			convertWorldToField(minLeftCoordinates, point);
    			if (point.x < 0)
    				outOfBounds = true;
    			convertWorldToField(minUpCoordinates, point);
    			if (point.y < 0)
    				outOfBounds = true;
    			convertWorldToField(maxRightCoordinates, point);
    			if (point.x > getWidth())
    				outOfBounds = true;
    			convertWorldToField(maxDownCoordinates, point);
    			if (point.y > getHeight())
    				outOfBounds = true;
    		} catch (IllegalArgumentException ex) {
    			outOfBounds = true;
    		}
    	}
    	zoom++;
    	setZoom(zoom);
    }

    protected void repaintPoints() {
    	mPointsBitmap = new Bitmap(getWidth(), getHeight());
    	mPointsBitmap.createAlpha(Bitmap.ALPHA_BITDEPTH_8BPP);
    	mDest = new XYRect(0, 0, mPointsBitmap.getWidth(), mPointsBitmap
    			.getHeight());
    	Graphics g = Graphics.create(mPointsBitmap);
    	if (null != mPoint) {
    		mPointDest = new XYRect[mPoints.length];
    		for (int i = 0; i < mPoints.length; i++) {
    			if (null == mPointDest[i]) {
    				XYPoint fieldOut = new XYPoint();
    				convertWorldToField(new Coordinates(mPoints[i].getLat(), mPoints[i].getLon(), 0), fieldOut);
    				int imgW = mPoint.getWidth();
    				int imgH = mPoint.getHeight();
    				mPointDest[i] = new XYRect(fieldOut.x - imgW / 2,
    						fieldOut.y - imgH, imgW, imgH);
    			}
    			g.drawBitmap(mPointDest[i], mPoint, 0, 0);
    		}
    	}
    }

    protected void paint(Graphics graphics) {
    	super.paint(graphics);
    	if (null != mPointsBitmap) {
    		graphics.setGlobalAlpha(100);
    		graphics.drawBitmap(mDest, mPointsBitmap, 0, 0);
    	}
    }
	
	
}
*/