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
public class ViewController implements EventHandler<Node> {

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

    @Override
    public void registerEventHandler(String key) {
        EventManager.getInstance().register(key, this);
    }

    @Override
    public void handleEvent(Node object, String category, String value) {
        LayerNode target = object.getViewParent();
        if (target != null) {
            try {
                Actions action = Actions.valueOf(category);
                handleAction(action, value, target);
            } catch (IllegalArgumentException e) {
                System.out.println("Could not parse category: " + category);
            }
        } else {
            SimpleLogger.d(getClass(), "No ViewNode parent in node " + object);
        }
    }

    private void handleAction(Actions action, String data, LayerNode target) {
        float[] values = StringUtils.getFloatArray(data);
        switch (action) {
        case MOVE:
            target.getTransform().addTranslation(values);
            break;
        case MOVETO:
            target.getTransform().setTranslate(values);
            break;
        default:
            throw new IllegalArgumentException("Not implemented");
        }
    }

    @Override
    public String getHandlerCategory() {
        return HANDLER_KEY;
    }

}
