package com.example.imagetest;

public class Point2D {
	public double x;
	public double y;
	public Point2D() {
		this(0,0);
	}
	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "[x=" + x + ", y=" + y + "]";
	}
	
}
