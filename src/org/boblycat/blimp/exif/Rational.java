package org.boblycat.blimp.exif;

/**
 * A rational type used by the ExifField class.
 * 
 * @author Knut Arild Erstad
 */
public class Rational {
    int numerator;
    int denominator;
    
    public Rational(int numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }
    
    public int getNumerator() {
        return numerator;
    }
    
    public int getDenominator() {
        return denominator;
    }

    public String toString() {
        return Integer.toString(numerator) + '/' + Integer.toString(denominator);
    }
}
