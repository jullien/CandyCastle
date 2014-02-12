package candy.castle;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class PolygonOverlay extends Overlay {
	
	private List<GeoPoint> drawPoints;
	private Paint paint;
	
	
	public PolygonOverlay() { 
		super();
	}

	public void setData(List<GeoPoint> drawPoints, Paint paint) {
		this.drawPoints = drawPoints;
		this.paint = paint;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (!shadow) {
		   Path path = new Path();
		   
	       ConvexHull hull = new ConvexHull();
	        
	       drawPoints = hull.calculateHull(drawPoints);
	       
	       for (int i = 0; i < drawPoints.size(); i++) {
	    	   GeoPoint gp = drawPoints.get(i);
	    	   Point point = new Point();
	    	   Projection projection = mapView.getProjection();
	    	   projection.toPixels(gp, point);
	    	   
	    	   if (i==0) { path.moveTo(point.x, point.y); }
	    	   else { path.lineTo(point.x, point.y); }
	       }
	       
	       // close polygon
	       GeoPoint gp = drawPoints.get(0);
    	   Point point = new Point();
    	   Projection projection = mapView.getProjection();
    	   projection.toPixels(gp, point);
	       path.lineTo(point.x, point.y);
	       path.setLastPoint(point.x, point.y);

	       canvas.drawPath(path, paint);
		}
		
		super.draw(canvas, mapView, shadow);
	}
}