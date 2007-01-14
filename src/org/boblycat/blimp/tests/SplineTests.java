package org.boblycat.blimp.tests;

import org.boblycat.blimp.NaturalCubicSpline;
import org.junit.*;
import static org.junit.Assert.*;

public class SplineTests {
	@Test
	public void testZeroPoints() {
		NaturalCubicSpline spline = new NaturalCubicSpline();
		assertEquals(0.0, spline.getSplineValue(-0.5));
		assertEquals(0.0, spline.getSplineValue(0.0));
		assertEquals(0.0, spline.getSplineValue(0.5));
		assertEquals(0.0, spline.getSplineValue(1.0));
		assertEquals(0.0, spline.getSplineValue(1.5));
	}
	
	@Test
	public void testOnePoint() {
		NaturalCubicSpline spline = new NaturalCubicSpline();
		spline.addPoint(0.1, 0.2);
		assertEquals(0.2, spline.getSplineValue(-0.5));
		assertEquals(0.2, spline.getSplineValue(0.0));
		assertEquals(0.2, spline.getSplineValue(0.5));
		assertEquals(0.2, spline.getSplineValue(1.0));
		assertEquals(0.2, spline.getSplineValue(1.5));
	}

	@Test
	public void testTwoPoints() {
		NaturalCubicSpline spline = new NaturalCubicSpline();
		spline.addPoint(0.0, 0.0);
		spline.addPoint(1.0, 1.0);
		assertEquals(0.0, spline.getSplineValue(-0.5));
		assertEquals(0.0, spline.getSplineValue(0.0));
		assertEquals(0.1, spline.getSplineValue(0.1));
		assertEquals(0.5, spline.getSplineValue(0.5));
		assertEquals(1.0, spline.getSplineValue(1.0));
		assertEquals(1.0, spline.getSplineValue(1.5));
	}
	
	@Test
	public void testThreePoints() {
		NaturalCubicSpline spline = new NaturalCubicSpline();
		spline.addPoint(0.0, 0.0);
		spline.addPoint(0.5, 0.2);
		spline.addPoint(1.0, 1.0);
		
		assertEquals(0.0, spline.getSplineValue(-0.5));
		assertEquals(0.0, spline.getSplineValue(0.0));
		assertEquals(0.2, spline.getSplineValue(0.5));
		assertEquals(1.0, spline.getSplineValue(1.0));
		assertEquals(1.0, spline.getSplineValue(1.5));
		
		double y;
		y = spline.getSplineValue(0.25);
		assertTrue(0 < y);
		assertTrue(y < 0.2);
		y = spline.getSplineValue(0.75);
		assertTrue(0.2 < y);
		assertTrue(y < 1.0);
	}
}
