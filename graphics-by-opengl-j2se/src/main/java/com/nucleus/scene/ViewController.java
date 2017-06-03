package com.nucleus.scene;

import com.nucleus.SimpleLogger;
import com.nucleus.common.StringUtils;
import com.nucleus.event.EventManager;
import com.nucleus.event.EventManager.EventHandler;
import com.nucleus.vecmath.Transform;

/**
 * Handles the update of the scene View
 * This class can be registered with {@linkplain EventHandler} to listen for view events.
 * 
 * @author Richard Sahlin
 *
 */
public class ViewController implements EventHandler {

    /**
     * The key to register in the property handler for this class
     */
    private final static String HANDLER_KEY = "view";

    public enum Actions {
        MOVE(),
        SCALE(),
        ROTATE(),
        MOVETO();
    }

    private Transform view;

    /**
     * Default constructor, use this to resolve "view" property key.
     * TODO Should {@link #handleEvent(String, String)} and {@link #handleObjectEvent(Object, String, String)}
     * be split into 2 separate interfaces?
     */
    public ViewController() {
    }

    /**
     * Creates a new viewcontroller with the specified view transform.
     * 
     * @param view The transform that is controlled by this view.
     * @throws IllegalArgumentException If view is null
     */
    public ViewController(Transform view) {
        if (view == null) {
            throw new IllegalArgumentException("View transform is null");
        }
        this.view = view;
    }

    /**
     * Registers this class as a eventhandler for the key, if key is null the {@link #HANDLER_KEY} is used.
     * NOTE! Only register ONE view controller, this shall be called with the
     * {@link #handleObjectEvent(Object, String, String)} method which will resolve the needed target view transform.
     * TODO How to make sure only one instance of this class is registered?
     * TODO Perhaps split viewcontroller into 2 parts, one that handles the "view" property and does not need a target
     * reference
     * 
     * @param key The key to register this controller for, or null to use default.
     */
    public void registerEventHandler(String key) {
        EventManager.getInstance().register(key, this);
    }

    @Override
    public void handleEvent(String category, String value) {
        try {
            Actions action = Actions.valueOf(category);
            handleAction(action, value);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not parse category: " + category);
        }
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
    public void handleObjectEvent(Object obj, String category, String value) {
        ViewNode view = ((Node) obj).getViewParent();
        if (view != null) {
            view.getViewController().handleEvent(category, value);
        } else {
            SimpleLogger.d(getClass(), "No ViewNode parent in node " + obj);
        }
    }

    @Override
    public String getHandlerCategory() {
        return HANDLER_KEY;
    }

}
