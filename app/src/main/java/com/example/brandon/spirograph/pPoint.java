package com.example.brandon.spirograph;

/**
 * Created by Brandon on 12/6/2014.
 */

public class pPoint
{
    public pPoint()
    {
        setX(0);
        setY(0);
    }

	public pPoint(double x, double y)
	{
		setX(x);
		setY(y);
	}

    public pPoint(pPoint source)
    {
        setX(source.getX());
        setY(source.getY());
    }
	
	//cartesian: horizontal coordinate
	private double _x;
	public double getX() { return _x; }
	public void setX(double value) { _x = value; }
	
	//cartesian: vertical coordinate
	private double _y;
	public double getY() { return _y; }
	public void setY(double value) { _y = value; }
	
	//polar: magnitude
	public double getR()
	{
		return Math.sqrt(getX()*getX() + getY()*getY());
	}
	
	//polar: angle from +x in radians
	public double getTheta()
	{
        double result;

        if(getX() < 0)
            result = -(Math.PI) + Math.atan(getY()/getX());
        else {
            result = Math.atan(getY() / getX());
        }

		return result;
	}
    public void setTheta(double theta)
    {
        double radius = getR();
        setX(radius*Math.cos(theta));
        setY(radius*Math.sin(theta));
    }

	public static double getDistance(pPoint A, pPoint B)
	{
        double dx = A.getX() - B.getX();
        double dy = A.getY() - B.getY();

		return Math.sqrt(dx*dx + dy*dy);
	}
}