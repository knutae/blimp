package org.boblycat.blimp.gui.swt;

import org.boblycat.blimp.Debug;
import org.boblycat.blimp.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;

/**
 * A slider component for numerical values which is a composite of a caption
 * label, a edit field and a visual slider (scale).
 * 
 * @author Knut Arild Erstad
 */
public class ValueSlider extends Composite {
    Label captionLabel;
    Text valueEdit;
    Scale scale;
    int minimum;
    int maximum;
    int selection;
    int digits;
    double digitsDivisor;
    boolean flipDirection;

    public ValueSlider(Composite parent, int style) {
        this(parent, style, null, -100, 100, 0);
    }

    public ValueSlider(Composite parent, int style, String caption,
            int minValue, int maxValue, int numDigits) {
        super(parent, style);
        this.minimum = minValue;
        this.maximum = maxValue;
        this.selection = 0;
        this.digits = 0;
        this.digitsDivisor = 1.0;
        setDigits(numDigits);

        captionLabel = new Label(this, SWT.NONE);
        if (caption != null)
            setCaption(caption);
        valueEdit = new Text(this, SWT.BORDER);
        valueEdit.addListener(SWT.Verify, new Listener() {
            public void handleEvent(Event e) {
                // strip illegal characters from e
                StringBuffer buf = new StringBuffer(e.text.length());
                for (char c : e.text.toCharArray()) {
                    if (isLegalValueChar(c))
                        buf.append(c);
                }
                e.text = buf.toString();
            }
        });
        Listener applyTextValueListener = new Listener() {
            public void handleEvent(Event e) {
                try {
                    int value = Util.valueOfFixPointDecimal(
                            valueEdit.getText(), digits);
                    setSelectionNoUpdate(value, true);
                }
                catch (NumberFormatException ex) {
                    // value will be reverted in updateSelection()
                }
                updateSelection();
            }
        };
        valueEdit.addListener(SWT.DefaultSelection, applyTextValueListener);

        scale = new Scale(this, SWT.NONE);
        scale.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                int newSelection;
                if (flipDirection)
                    newSelection = maximum - scale.getSelection();
                else
                    newSelection = minimum + scale.getSelection();
                setSelection(newSelection, true);
            }
        });

        updateGuiValues();

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        setLayout(gridLayout);

        captionLabel.setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, false));
        GridData valueData = new GridData(SWT.FILL, SWT.FILL, false, false);
        valueData.widthHint = 30;
        valueEdit.setLayoutData(valueData);
        GridData scaleData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        scaleData.widthHint = 150;
        scale.setLayoutData(scaleData);
        setAutoPageIncrement();
    }

    boolean isLegalValueChar(char c) {
        if (Character.isDigit(c))
            return true;
        if (minimum < 0 && c == '-')
            return true;
        if (digits > 0 && c == '.')
            return true;
        return false;
    }

    void updateRange() {
        int range = maximum - minimum;
        scale.setMinimum(0);
        scale.setMaximum(range);
    }

    void updateSelection() {
        int scaleSelection;
        if (flipDirection)
            scaleSelection = maximum - selection;
        else
            scaleSelection = selection - minimum;
        scale.setSelection(scaleSelection);
        String strValue = Util.fixPointDecimalToString(selection, digits);
        valueEdit.setText(strValue);
    }

    void updateGuiValues() {
        updateRange();
        updateSelection();
    }

    public void setCaption(String caption) {
        captionLabel.setText(caption);
    }

    public void setMinimum(int minimum) {
        this.minimum = minimum;
    }

    public int getMinimum() {
        return minimum;
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }

    public int getMaximum() {
        return maximum;
    }
    
    public void updateMinMax(int minimum, int maximum) {
        if (minimum > maximum)
            throw new IllegalArgumentException("minimum > maximum");
        setMinimum(minimum);
        setMaximum(maximum);
        updateRange();
        setAutoPageIncrement();
    }

    void setSelectionNoUpdate(int selection, boolean sendSelectionEvent) {
        Debug.print(this, "setSelection: " + selection);
        if (selection < minimum)
            selection = minimum;
        else if (selection > maximum)
            selection = maximum;
        if (selection == this.selection)
            return;
        this.selection = selection;
        if (sendSelectionEvent) {
            Event e = new Event();
            e.type = SWT.Selection;
            notifyListeners(SWT.Selection, e);
        }
    }

    public void setSelection(int selection, boolean sendSelectionEvent) {
        setSelectionNoUpdate(selection, sendSelectionEvent);
        updateSelection();
    }

    public void setSelection(int selection) {
        setSelection(selection, false);
    }

    public int getSelection() {
        return selection;
    }

    public void setDigits(int digits) {
        if (digits < 0)
            return;
        this.digits = digits;
        digitsDivisor = 1.0;
        for (int i=0; i<digits; i++)
            digitsDivisor *= 10.0;
    }
    
    public double getSelectionAsDouble() {
        return getSelection() / digitsDivisor;
    }
    
    public void setSelectionAsDouble(double newValue) {
        setSelection((int) (newValue * digitsDivisor));
    }
    
    private void setAutoPageIncrement() {
        int range = maximum - minimum;
        int incr = 1;
        while (incr * 20 < range)
            incr *= 10;
        scale.setPageIncrement(incr);
    }

    public int getDigits() {
        return digits;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        captionLabel.setEnabled(enabled);
        valueEdit.setEnabled(enabled);
        scale.setEnabled(enabled);
    }

    public void setFlipDirection(boolean flipDirection) {
        this.flipDirection = flipDirection;
    }

    public boolean getFlipDirection() {
        return flipDirection;
    }
}
