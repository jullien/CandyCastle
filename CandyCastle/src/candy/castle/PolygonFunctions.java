package candy.castle;

import java.util.List;

public class PolygonFunctions {
	
	List<Position> values;
	
	public PolygonFunctions (List<Position> values) {
		this.values = values;
		values = lowPoint(values);
		values = polarAngle(values);
	}
	
	public double polygonArea (List<Position> values) {
    	double area = 0;
    	
    	for (int i=1;i<values.size()-1;i++) {
    		area += (values.get(i).getLat()*values.get(i+1).getLng())-(values.get(i+1).getLat()*values.get(i).getLng());
    	}
    	
    	area += (values.get(values.size()-1).getLat()*values.get(1).getLng())-(values.get(1).getLat()*values.get(values.size()-1).getLng());
    	
    	return area/2;
    }
    
	public double certerGravityX (List<Position> values, double area) {
    	double x = 0;
    	
    	for (int i=1;i<values.size()-1;i++) {
    		x += (values.get(i).getLat()+values.get(i+1).getLat())*((values.get(i).getLat()*values.get(i+1).getLng())-(values.get(i+1).getLat()*values.get(i).getLng()));
    	}
    	
    	x += (values.get(values.size()-1).getLat()+values.get(1).getLat())*((values.get(values.size()-1).getLat()*values.get(1).getLng())-(values.get(1).getLat()*values.get(values.size()-1).getLng()));
    	
    	return x/(6*area);
    }
    
	public double certerGravityY (List<Position> values, double area) {
    	double y = 0;
    	
    	for (int i=1;i<values.size()-1;i++) {
    		y += (values.get(i).getLng()+values.get(i+1).getLng())*((values.get(i).getLat()*values.get(i+1).getLng())-(values.get(i+1).getLat()*values.get(i).getLng()));
    	}
    	
    	y += (values.get(values.size()-1).getLng()+values.get(1).getLng())*((values.get(values.size()-1).getLat()*values.get(1).getLng())-(values.get(1).getLat()*values.get(values.size()-1).getLng()));
    	
    	return y/(6*area);
    }
	
	private List<Position> lowPoint (List<Position> values) {
		int pos = 1;
		Position temp = values.get(1);
		
		for (int i=2;i<values.size();i++) {
			if (pointsG(temp,values.get(i))) {
			   temp = values.get(i);
			   pos = i;
			}
		}
		
		if (pos != 1) {
			values.set(pos, values.get(1));
			values.set(1, temp);
		}
		
		return values;
	}
	
	private List<Position> polarAngle (List<Position> values) {
		Position center = values.get(1);
		
		for (int i=2;i<values.size()-1;i++) {
			Position temp = values.get(i);
			int pos = i;
			
			for (int j=i+1;j<values.size();j++) {
				double angle1 = Math.atan2(temp.getLng()-center.getLng(), temp.getLat()-center.getLat());
		        double angle2 = Math.atan2(values.get(j).getLng()-center.getLng(), values.get(j).getLat()-center.getLat());
		        
		        if (
		        	(angle1==angle2 && 
		        	 (
		        	  (angle1==0 && temp.getLat()>values.get(j).getLat() && temp.getLng()==values.get(j).getLng()) ||
		        	  (angle1==1.5707963267948966 && temp.getLng()<values.get(j).getLng() && temp.getLat()==values.get(j).getLat()) ||
		        	  (angle1>0 && angle1<1.5707963267948966 && temp.getLat()>values.get(j).getLat()) ||
		        	  (angle1<0 && temp.getLat()>values.get(j).getLat())
		        	 )
		        	)
		        	|| (angle1>angle2)
		           ) {
		           temp = values.get(j);
				   pos = j;
		        }
			}
			
			if (pos != i) {
				values.set(pos, values.get(i));
				values.set(i, temp);
			}
		}
		
		return values;
	}
	
	private Boolean pointsG (Position a, Position b) {
		return (a.getLat()>b.getLat() || (a.getLat()==b.getLat() && a.getLng()>b.getLng()));
	}
}
