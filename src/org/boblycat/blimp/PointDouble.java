package org.boblycat.blimp;

public class PointDouble {
	public double x;
	public double y;

	public PointDouble() {
	}
	
	public PointDouble(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public String toCommaString() {
		return Double.toString(x) + ',' + Double.toString(y);
	}

	public static PointDouble valueOfCommaString(String input)
		throws NumberFormatException
	{
		int comma = input.indexOf(',');
		if (comma < 0)
			throw new NumberFormatException("Missing comma in point: " + input);
		double x = Double.valueOf(input.substring(0, comma));
		double y = Double.valueOf(input.substring(comma+1, input.length()));
		return new PointDouble(x, y);
	}
	
	/*
	public void setX(double x) { this.x = x; }
	public void setY(double y) { this.y = y; }
	public double getX() { return x; }
	public double getY() { return y; }
	*/
}
