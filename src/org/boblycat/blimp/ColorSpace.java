package org.boblycat.blimp;

public enum ColorSpace {
	// These following color spaces are supported by dcraw 
	Uncalibrated, // raw
	sRGB, // standard for web
	Adobe, // Adobe RGB (good for printing)
	AdobeWide, // Adobe Wide Gamut RGB
	ProPhoto, // ?
	XYZ, // CIE 1931 XYZ ?
}
