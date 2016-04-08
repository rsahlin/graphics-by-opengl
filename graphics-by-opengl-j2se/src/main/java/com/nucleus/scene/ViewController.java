package com.nucleus.scene;

import com.nucleus.common.StringUtils;
import com.nucleus.properties.Property;
import com.nucleus.properties.PropertyManager;
import com.nucleus.properties.PropertyManager.PropertyHandler;
import com.nucleus.vecmath.Transform;

/**
 * This handles the update of the scene View.
 * To register this class to handle property key, call the
 * This class will be registered as handling view properties.
 * 
 * @author Richard Sahlin
 *
 */
public class ViewController implements PropertyHandler {

    /**
     * The key to register in the property handler for this class
     * TODO Do not store as magic string, find some other way.
     */
    public final static String HANDLER_KEY = "view";

    public enum Actions {
        MOVE(),
        SCALE(),
        ROTATE(),
        MOVETO();
    }

    private Transform view;

    /**
     * Creates a new viewcontroller with the specified view.
     * 
     * @param view
     * @throws IllegalArgumentException If view is null
     */
    public ViewController(Transform view) {
        if (view == null) {
            throw new IllegalArgumentException("View transform is null");
        }
        this.view = view;
    }

    /**
     * Registers this class as a propertyhandler for the key, if key is null the {@link #HANDLER_KEY} is used.
     * 
     * @param key The key to register this controller for, or null to use default.
     */
    public void registerPropertyHandler(String key) {
        if (key == null) {
            key = HANDLER_KEY;
        }
        PropertyManager.getInstance().registerKey(key, this);
    }

    @Override
    public boolean handleProperty(String key, String value) {
        String[] parts = Property.split(value);
        try {
            Actions action = Actions.valueOf(parts[0]);
            handleAction(action, parts[1]);
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println("Could not parse action: " + parts[0]);
        }
        return true;
    }

    private void handleAction(Actions action, String data) {
        float[] values = StringUtils.getFloatArray(data);
        switch (action) {
        case MOVE:
            view.addTranslation(values);
            break;
        case MOVETO:
            view.setTranslate(values);
            break;
        default:
            throw new IllegalArgumentException("Not implemented");
        }
    }

    @Override
    public boolean handleObjectProperty(Object obj, String key, String value) {
        // TODO Auto-generated method stub
        return false;
    }

}
