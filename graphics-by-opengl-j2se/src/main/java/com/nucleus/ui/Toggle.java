package com.nucleus.ui;

/**
 * Toggle button, can toggle between selected and not selected
 *
 */
public interface Toggle extends Element {

    public final static String SELECTED = "selected";
    public final static String SELECTED_FRAMES = "selectedFrames";

    /**
     * Called when state of a toggle has changed.
     * 
     * @param toggle
     */
    public interface ToggleListener {
        public boolean onStateChanged(Toggle toggle);
    }

    /**
     * Sets the selected state
     * 
     * @param selected True to select, false to unselect
     */
    public void setSelected(boolean selected);

    /**
     * Returns the selected state
     * 
     * @return True if the state is selected, false otherwise
     */
    public boolean isSelected();

}
