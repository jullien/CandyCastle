package candy.castle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class CircleOverlay extends Overlay {
	
	private Location origin;
	private double radius;
	private Paint paint;
	private GeoPoint center;
	
	public CircleOverlay() {
		super();
	}
	
	public void setData(Location origin, double radius, Paint paint) {
		this.origin = origin;
		this.radius = radius;
		this.paint = paint;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (!shadow) {
		   int lat = (int) (origin.getLatitude() * 1E6);
		   int lng = (int) (origin.getLongitude() * 1E6);
		   center = new GeoPoint(lat,lng);

		   // Transform geo-position to Point on canvas
		   Projection projection = mapView.getProjection();
		   
		   // Radius in pixels
		   float projectedRadius = projection.metersToEquatorPixels((float) radius);
		   
		   Point point = new Point();
		   
		   // Store the transformed GeoPoint into a point with pixel values
		   projection.toPixels(center, point);
	         
		   canvas.drawCircle(point.x, point.y, projectedRadius+(projectedRadius/4), paint);
		   //canvas.drawCircle(point.x, point.y, projectedRadius+(projectedRadius/4)+1, paint);
		   //canvas.drawCircle(point.x, point.y, projectedRadius+(projectedRadius/4)-2, paint);
		   //testar outros valores para acertar melhor a posição do circulo em relação as torres
		}
		
		super.draw(canvas, mapView, shadow);
	}
}