package com.nucleus.ui;

/**
 * Toggle button, can have 1 or more selections. Only one selection can be active at a time.
 *
 */
public interface Toggle extends Element {

    /**
     * Called when state of a toggle has changed.
     * 
     * @param toggle
     */
    public interface ToggleListener {
        public boolean onStateChanged(Toggle toggle);
    }

    /**
     * Selects the next item
     */
    public void toggle();

    /**
     * Sets the selected item
     * 
     * @param selected
     */
    public void setSelected(int selected);

    /**
     * Returns the item that is selected, 0 and upwards.
     * 
     * @return
     */
    public int getSelected();

}
