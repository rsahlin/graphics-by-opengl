package com.nucleus.scene;

import java.util.ArrayList;

import com.nucleus.SimpleLogger;
import com.nucleus.common.Constants;
import com.nucleus.event.EventManager;
import com.nucleus.event.EventManager.EventHandler;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.mmi.MMIPointerEvent.Action;
import com.nucleus.mmi.NodeInputListener;
import com.nucleus.mmi.core.InputProcessor;
import com.nucleus.properties.Property;
import com.nucleus.scene.Node.State;

/**
 * Handles pointer input checking on nodes
 * Takes {@link MMIEventListener} events and checks the registered node tree for pointer hits.
 * This class must be registred to {@link InputProcessor} for it to get mmi event callbacks.
 */
public class J2SENodeInputListener implements MMIEventListener {

    /**
     * Set this property to true for nodes that shall check pointer input
     */
    public static final String ONCLICK = "onclick";

    private final RootNode root;
    private final ArrayList<Node> visibleNodes = new ArrayList<>();
    private int nodeId = Constants.NO_VALUE;

    private float[] down = new float[2];

    public J2SENodeInputListener(RootNode root) {
        this.root = root;
    }

    /**
     * Recursively check nodes for the input event, when a node consumes the event true will be returned.
     * 
     * @param nodes List of nodes to check - this must be in draw order, ie first drawn node will be first.
     * Iterate through this from end to beginning
     * @param event
     * @return True if a node has consumed the input event event
     */
    protected boolean onInputEvent(ArrayList<Node> nodes, MMIPointerEvent event) {
        int count = nodes.size() - 1;
        Node node = null;
        for (int i = count; i >= 0; i--) {
            node = nodes.get(i);
            switch (node.getState()) {
                case ON:
                case ACTOR:
                    if (checkChildren(node, event)) {
                        return true;
                    }
                    break;
                default:
                    SimpleLogger.d(getClass(), "Not handling input, node in state: " + node.getState());
                    // Do nothing
            }
        }
        return false;
    }

    /**
     * Checks this node for pointer event.
     * This default implementation will check bounds and check {@link #ONCLICK} property and send to
     * {@link EventManager#post(Node, String, String)} if defined.
     * TODO Instead of transforming the bounds the inverse matrix should be used.
     * Will stop when a node is in state {@link State#OFF} or {@link State#RENDER}
     * 
     * @param event
     * @return True if the input event was consumed, false otherwise.
     */
    protected boolean onPointerEvent(Node node, MMIPointerEvent event) {
        float[] position = event.getPointerData().getCurrentPosition();
        if (position == null && event.getAction() != Action.ZOOM) {
            return false;
        }
        if (node.getProperty(EventHandler.EventType.POINTERINPUT.name(), Constants.FALSE)
                .equals(Constants.FALSE)) {
            return false;
        }
        NodeInputListener listener = root.getObjectInputListener();
        // Zoom is a special case that does not have position
        if (event.getAction() == Action.ZOOM) {
            if (listener != null) {
                return listener.onInputEvent(node, event.getPointerData().getCurrent());
            }
        } else {
            if (node.isInside(position)) {
                if (node instanceof MMIEventListener) {
                    ((MMIEventListener) node).onInputEvent(event);
                }
                switch (event.getAction()) {
                    case ACTIVE:
                        down[0] = position[0];
                        down[1] = position[1];
                        SimpleLogger.d(getClass(), "HIT: " + node);
                        String onclick = node.getProperty(ONCLICK, null);
                        if (onclick != null) {
                            Property p = Property.create(onclick);
                            if (p != null) {
                                EventManager.getInstance().post(node, p.getKey(), p.getValue());
                            } else {
                                SimpleLogger.d(getClass(),
                                        "Invalid property for node " + node.getId() + " : " + onclick);
                            }
                        }
                        if (listener != null) {
                            listener.onInputEvent(node, event.getPointerData().getCurrent());
                        }
                        return true;
                    case INACTIVE:
                        if (listener != null) {
                            listener.onInputEvent(node, event.getPointerData().getCurrent());
                        }
                        return true;
                    case MOVE:
                        if (listener != null) {
                            listener.onDrag(node, event.getPointerData());
                        }
                        return true;
                    // Zoom should not be handle inside since it is an action that does not have any real position
                    default:
                        break;
                }
            }

        }

        return false;
    }

    /**
     * Checks children for pointer event, calling {@link #onPointerEvent(MMIPointerEvent)} recursively and stopping when
     * a child returns true.
     * 
     * @param event
     * @return true if one of the children has a match for the pointer event, false otherwise
     */
    protected boolean checkChildren(Node node, MMIPointerEvent event) {
        State state = node.getState();
        if (state == State.ON || state == State.ACTOR) {
            if (onPointerEvent(node, event)) {
                return true;
            }
            for (Node n : node.getChildren()) {
                if (onPointerEvent(n, event)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onInputEvent(MMIPointerEvent event) {
        nodeId = root.getVisibleNodeList(visibleNodes, nodeId);
        onInputEvent(visibleNodes, event);
    }

}
