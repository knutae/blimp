package org.boblycat.blimp.gui.swt;

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

    public ValueSlider(Composite parent, int style) {
        this(parent, style, null, -100, 100, 0);
    }

    public ValueSlider(Composite parent, int style, String caption,
            int minValue, int maxValue, int numDigits) {
        super(parent, style);
        this.minimum = minValue;
        this.maximum = maxValue;
        this.selection = 0;
        this.digits = numDigits;

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
        // valueEdit.addListener(SWT.FocusOut, applyTextValueListener); //
        // unreliable

        scale = new Scale(this, SWT.NONE);
        scale.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                setSelection(scale.getSelection() + minimum, true);
            }
        });

        updateGuiValues();

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        setLayout(gridLayout);

        captionLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridData valueData = new GridData(SWT.FILL, SWT.FILL, false, false);
        valueData.widthHint = 30;
        valueEdit.setLayoutData(valueData);
        scale
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2,
                        1));
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
        scale.setSelection(selection - minimum);
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

    public void setMinimum(int minumum) {
        this.minimum = minumum;
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

    void setSelectionNoUpdate(int selection, boolean sendSelectionEvent) {
        // System.out.println("setSelection: " + selection);
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
        /*
         * int mult = 1; for (int i=0; i<digits; i++) mult *= 10;
         * scale.setIncrement(mult); scale.setPageIncrement(mult);
         */
    }

    public void setPageIncrement(int incr) {
        scale.setPageIncrement(incr);
    }

    public int getDigits() {
        return digits;
    }
}
