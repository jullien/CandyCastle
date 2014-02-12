package candy.castle;

import java.util.List;

import com.google.android.maps.GeoPoint;

public class ConvexHull{
	
	public ConvexHull() {
		super();
	}
	
	public List<GeoPoint> calculateHull(List<GeoPoint> drawPoints) {
		// Get the point with the lowest latitude, if two points had the same latitude choose who have the lowest longitude between this two points
		drawPoints = lowPoint(drawPoints); 
		
		// After choose the initial point with the function above
		// Sort the other points according to the polar angle in relation to the initial point (counterclockwise)
		drawPoints = polarAngle(drawPoints);

		return drawPoints;
	}
	
	private List<GeoPoint> lowPoint (List<GeoPoint> drawPoints) {
		int pos = 0;
		GeoPoint temp = drawPoints.get(0);
		
		for (int i=1;i<drawPoints.size();i++) {
			if (pointsG(temp,drawPoints.get(i))) {
			   temp = drawPoints.get(i);
			   pos = i;
			}
		}
		
		if (pos != 0) {
		   drawPoints.set(pos, drawPoints.get(0));
		   drawPoints.set(0, temp);
		}
		
		return drawPoints;
	}
	
	private List<GeoPoint> polarAngle (List<GeoPoint> drawPoints) {
		GeoPoint center = drawPoints.get(0);
		
		for (int i=1;i<drawPoints.size()-1;i++) {
			GeoPoint temp = drawPoints.get(i);
			int pos = i;
			
			for (int j=i+1;j<drawPoints.size();j++) {
				double angle1 = Math.atan2(temp.getLongitudeE6()-center.getLongitudeE6(), temp.getLatitudeE6()-center.getLatitudeE6());
		        double angle2 = Math.atan2(drawPoints.get(j).getLongitudeE6()-center.getLongitudeE6(), drawPoints.get(j).getLatitudeE6()-center.getLatitudeE6());
		        
		        if (
		        	(angle1==angle2 && 
		        	 (
		        	  (angle1==0 && temp.getLatitudeE6()>drawPoints.get(j).getLatitudeE6() && temp.getLongitudeE6()==drawPoints.get(j).getLongitudeE6()) ||
				      (angle1==(Math.PI/2) && temp.getLongitudeE6()<drawPoints.get(j).getLongitudeE6() && temp.getLatitudeE6()==drawPoints.get(j).getLatitudeE6()) ||
				      (angle1>0 && angle1<(Math.PI/2) && temp.getLatitudeE6()>drawPoints.get(j).getLatitudeE6()) ||
				      (angle1<0 && temp.getLatitudeE6()>drawPoints.get(j).getLatitudeE6())
				     )
				    ) 
				    || (angle1>angle2)
				    //|| (angle1<0 && angle2<0 && temp.getLatitudeE6()>drawPoints.get(j).getLatitudeE6())	    	
				   ) {
		           temp = drawPoints.get(j);
				   pos = j;
		        }
			}
			
			if (pos != i) {
			   drawPoints.set(pos, drawPoints.get(i));
			   drawPoints.set(i, temp);
			}
		}
		
		return drawPoints;
	}
	
	private Boolean pointsG (GeoPoint a, GeoPoint b) {
		return (a.getLatitudeE6()>b.getLatitudeE6() || (a.getLatitudeE6()==b.getLatitudeE6() && a.getLongitudeE6()>b.getLongitudeE6()));
	}
}
