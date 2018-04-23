package com.nucleus.common;

import java.util.ArrayList;

/**
 * Wrapper for a list that can be accessed in a thread safe way.
 * Call {@link #updateList(ArrayList)} to clear old contents of this list, add new objects and update the current id.
 * Call {@link #getList(ArrayList, int)} to get the contents of the list, if the specified id matches the current id
 * then nothing is copied.
 * This means that multiple calls to {@link #getList(ArrayList, int)} will not copy values unless
 * {@link #updateList(ArrayList)}
 * is called inbetween.
 * 
 *
 */
public class ManagedList<E> {

    private ArrayList<E> list = new ArrayList<>();
    private int id = 1;

    /**
     * Fetches the list with matching id, keep the returned id as a reference next time this method is called.
     * If {@link #updateList(ArrayList)} has not been called then nothing needs to be done.
     * 
     * @param list
     * @param id
     * @return The id of the copied list, pass this to method next time to avoid copying if values have not changed.
     */
    public int getList(ArrayList<E> list, int id) {
        synchronized (this.list) {
            if (this.id != id) {
                list.clear();
                list.addAll(this.list);
            }
        }
        return this.id;
    }

    public void updateList(ArrayList<E> update) {
        synchronized (list) {
            list.clear();
            list.addAll(update);
            id++;
        }
    }

}
