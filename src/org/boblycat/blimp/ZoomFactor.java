package org.boblycat.blimp;

/**
 * A helper class for scaling using a fractional number and zooming in and out
 * in predefined steps.
 * 
 * @author Knut Arild Erstad
 */
public class ZoomFactor {
    int multiplier;
    int divisor;

    public ZoomFactor() {
        multiplier = 1;
        divisor = 1;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public int getDivisor() {
        return divisor;
    }

    public void zoomIn() {
        assert (multiplier >= 1);
        assert (divisor >= 1);
        if (divisor == 1)
            multiplier++;
        else if (multiplier == 2) {
            // 2/3 (the only case where neither multiplier nor divisor is 1)
            assert (divisor == 3);
            multiplier = 1;
            divisor = 1;
        }
        else {
            assert (multiplier == 1);
            switch (divisor) {
            case 2:
                // zoom in to 2/3
                multiplier = 2;
                divisor = 3;
                break;
            case 3:
            case 4:
                divisor--;
                break;
            case 6:
            case 8:
                divisor -= 2;
                break;
            default:
                assert (divisor >= 12);
                assert (divisor % 4 == 0);
                divisor -= 4;
            }
        }
    }

    public void zoomOut() {
        assert (multiplier >= 1);
        assert (divisor >= 1);
        if (divisor == 1) {
            if (multiplier >= 2)
                multiplier--;
            else {
                multiplier = 2;
                divisor = 3;
            }
        }
        else if (multiplier == 2) {
            assert (divisor == 3);
            multiplier = 1;
            divisor = 2;
        }
        else {
            assert (multiplier == 1);
            switch (divisor) {
            case 2:
            case 3:
                divisor++;
                break;
            case 4:
            case 6:
                divisor += 2;
                break;
            default:
                assert (divisor >= 8);
                assert (divisor % 4 == 0);
                divisor += 4;
            }
        }
    }

    public int scale(int value) {
        return value * multiplier / divisor;
    }

    public double toDouble() {
        return (double) multiplier / (double) divisor;
    }
}
