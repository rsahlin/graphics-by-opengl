package com.nucleus.ui;

/**
 * Toggle button, can have 1 or more selections. Only one selection can be active at a time.
 *
 */
public interface Toggle extends Element {

    /**
     * A new item is selected, all other items shall be de-selected
     *
     */
    public interface ToggleListener {
        public void onStateChanged(int selected);
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
