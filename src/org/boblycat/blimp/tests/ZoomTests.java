package org.boblycat.blimp.tests;

import org.boblycat.blimp.ZoomFactor;
import org.junit.*;
import static org.junit.Assert.*;

public class ZoomTests {
	@Test
	public void testDefaultValue() {
		ZoomFactor zoom = new ZoomFactor();
		assertEquals(1, zoom.getMultiplier());
		assertEquals(1, zoom.getDivisor());
	}
	
	@Test
	public void testZoomIn() {
		ZoomFactor zoom = new ZoomFactor();
		for (int i=2; i<=10; i++) {
			zoom.zoomIn();
			assertEquals(i, zoom.getMultiplier());
			assertEquals(1, zoom.getDivisor());
		}
	}
	
	@Test
	public void testZoomInThenOut() {
		ZoomFactor zoom = new ZoomFactor();
		for (int i=2; i<=10; i++) {
			zoom.zoomIn();
		}
		for (int i=9; i>=1; i--) {
			zoom.zoomOut();
			assertEquals(i, zoom.getMultiplier());
			assertEquals(1, zoom.getDivisor());
		}
	}
	
	// 1/d divisors after zooming further out than 2/3
	static final int[] divisors = { 2, 3, 4, 6, 8, 12, 16, 20, 24 };

	@Test
	public void testZoomOut() {
		ZoomFactor zoom = new ZoomFactor();
		zoom.zoomOut();
		assertEquals(2, zoom.getMultiplier());
		assertEquals(3, zoom.getDivisor());
		for (int div: divisors) {
			zoom.zoomOut();
			assertEquals(1, zoom.getMultiplier());
			assertEquals(div, zoom.getDivisor());
		}
	}
	
	@Test
	public void testZoomOutThenIn() {
		ZoomFactor zoom = new ZoomFactor();
		for (int i=0; i<divisors.length+2; i++) {
			zoom.zoomOut();
		}
		for (int i=divisors.length-1; i>=0; i--) {
			zoom.zoomIn();
			assertEquals(1, zoom.getMultiplier());
			assertEquals(divisors[i], zoom.getDivisor());
		}
		zoom.zoomIn();
		assertEquals(2, zoom.getMultiplier());
		assertEquals(3, zoom.getDivisor());
		zoom.zoomIn();
		assertEquals(1, zoom.getMultiplier());
		assertEquals(1, zoom.getDivisor());
	}
}
