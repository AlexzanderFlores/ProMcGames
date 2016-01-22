package promcgames.server.util;

import org.bukkit.Location;

public class MathUtil {
	public static int getLowInt(int a, int b) {
		return a < b ? a : b;
	}
	
	public static int getMaxInt(int a, int b) {
		return a > b ? a : b;
	}
	
	public static double getLowDouble(double a, double b) {
		return a < b ? a : b;
	}
	
	public static double getMaxDouble(double a, double b) {
		return a > b ? a : b;
	}
	
	/*public static double getMaxNumber(double... numbers) {
		double max = -999999999D;
		for(double i : numbers) {
			if(i >= max) {
				max = i;
			}
		}
		return max;
	}
	
	public static double getMinNumber(double... numbers) {
		double min = 999999999D;
		for(double i : numbers) {
			if(i >= min) {
				min = i;
			}
		}
		return min;
	}*/
	
	public static double getMaxNumber(double [] numbers) {
		double max = -999999999D;
		for(double i : numbers) {
			if(i >= max) {
				max = i;
			}
		}
		return max;
	}
	
	public static double getMinNumber(double [] numbers) {
		double min = 999999999D;
		for(double i : numbers) {
			if(i >= min) {
				min = i;
			}
		}
		return min;
	}
	
	public static double round(double value, int places) {
	    if(places < 0) {
	    	throw new IllegalArgumentException();
	    }
	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	public static double getDistance(Location locationOne, Location locationTwo) {
		double x1 = locationOne.getX();
		double z1 = locationOne.getZ();
		double x2 = locationTwo.getX();
		double z2 = locationTwo.getZ();
		return Math.sqrt((x1 - x2) * (x1 - x2) + (z1 - z2) * (z1 - z2));
	}
}
