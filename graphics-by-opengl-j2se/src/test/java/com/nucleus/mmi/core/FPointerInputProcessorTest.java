package com.nucleus.mmi.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nucleus.BaseTestCase;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.mmi.MMIPointerEvent.Action;
import com.nucleus.mmi.PointerData;
import com.nucleus.mmi.PointerData.PointerAction;
import com.nucleus.mmi.PointerData.Type;
import com.nucleus.vecmath.Vec2;

public class FPointerInputProcessorTest extends BaseTestCase implements MMIEventListener {

    protected List<MMIPointerEvent> pointerEvents = new ArrayList<MMIPointerEvent>();
    protected final static float[] FIRST_POS = new float[] { 100f, 200f };
    protected final static float[] SECOND_POS = new float[] { 110f, 220f };

    protected final static float[] FINGER1_FIRST = new float[] { 100, 100 };
    protected final static float[] FINGER1_SECOND = new float[] { 110, 110 };
    protected final static float[] FINGER2_FIRST = new float[] { 200, 200 };
    protected final static float[] FINGER2_SECOND = new float[] { 190, 190 };
    protected final static float[] FINGER2_FIRST_SMALL = new float[] { 200, 200 };
    protected final static float[] FINGER2_SECOND_SMALL = new float[] { 201, 201 };
    protected final static float[] FINGER3_FIRST = new float[] { 300, 300 };
    protected final static float[] FINGER4_FIRST = new float[] { 400, 400 };
    protected final static float[] FINGER5_FIRST = new float[] { 500, 500 };

    protected Random random = new Random();

    /**
     * Used to assert that a pointer movement results in correct mmi action.
     *
     */
    private class AssertMMIAction {
        float[] position;
        Action action;
        int pointer;
        long timestamp;

        public AssertMMIAction(Action action, int pointer, float[] position, long timestamp) {
            this.action = action;
            this.pointer = pointer;
            this.position = position;
            this.timestamp = timestamp;
        }
    }

    @Before
    public void before() {
        pointerEvents.clear();
    }

    @Test
    public void testActionDownUpAll() {

        // Test that action down and up result in correct event.
        InputProcessor processor = InputProcessor.getInstance();

        ArrayList<AssertMMIAction> assertValues = new ArrayList<AssertMMIAction>();
        processor.addMMIListener(this);
        createAndSend(PointerAction.DOWN, processor, processor.getMaxPointers(), assertValues);
        createAndSend(PointerAction.UP, processor, processor.getMaxPointers(), assertValues);

        Assert.assertEquals(processor.getMaxPointers() * 2, pointerEvents.size());
        assertActionPosition(pointerEvents, assertValues, 0, processor.getMaxPointers() * 2, 0);

    }

    private void assertActionPosition(List<MMIPointerEvent> events, ArrayList<AssertMMIAction> assertValues,
            int offset, int count, int assertOffset) {

        /**
         * PointerMotionData starts with ACTIVE and ends with INACTIVE
         */
        for (int i = 0; i < count; i++) {
            MMIPointerEvent event = events.get(i + offset);
            AssertMMIAction assertAction = assertValues.get(i + assertOffset);
            Assert.assertEquals(assertAction.action, event.getAction());
            PointerData pointer = null;
            switch (event.getAction()) {
                case ACTIVE:
                    // First event
                    pointer = event.getPointerData().getFirst();
                    break;
                case INACTIVE:
                    // Last event
                    pointer = event.getPointerData().getCurrent();
                    break;
                case MOVE:
                    // Only works if move action comes after ACTIVE at index 0
                    pointer = event.getPointerData().get(i);
                    break;
                default:
                    throw new IllegalArgumentException("Not implemented");

            }
            Assert.assertEquals(assertAction.timestamp, pointer.timeStamp);
            Assert.assertArrayEquals(assertAction.position, pointer.data, 0);
        }

    }

    /**
     * Internal method to create an event with created position, position is added to ArrayList
     * 
     * @param action
     * @param processor
     * @param count
     * @param positions
     */
    private void createAndSend(PointerAction action, InputProcessor processor, int count,
            ArrayList<AssertMMIAction> positions) {

        for (int i = 0; i < count; i++) {
            float[] position = new float[] { i * 100, i * 100 + 1 };
            AssertMMIAction a = new AssertMMIAction(getFromPointerAction(action), i, position,
                    System.currentTimeMillis());
            positions.add(a);
            processor.pointerEvent(action, Type.FINGER, a.timestamp, i, position, 0);
        }
    }

    private AssertMMIAction sendEvent(PointerAction action, InputProcessor processor, int pointer,
            float[] coordinates, int pos) {

        float[] coord = new float[] { coordinates[pos++], coordinates[pos++] };
        AssertMMIAction check = new AssertMMIAction(getFromPointerAction(action), pointer, coord,
                System.currentTimeMillis());
        processor.pointerEvent(action, Type.FINGER, check.timestamp, pointer, coord, 0);
        return check;
    }

    /**
     * Internal method to convert from pointer action to corresponding MMI action.
     * 
     * @param action
     * @return
     */
    private Action getFromPointerAction(PointerAction action) {
        switch (action) {
            case DOWN:
                return Action.ACTIVE;
            case UP:
                return Action.INACTIVE;
            case MOVE:
                return Action.MOVE;
            default:
                throw new IllegalArgumentException("Not implemented for action " + action);
        }
    }

    /**
     * Creates a number of random positions, 2 values will be created for each (to simulate x and y)
     * 
     * @param count
     * @return Array containing 2 * count random values.
     */
    private float[] createRandomPositions(int count) {
        float[] positions = new float[count * 2];
        int index = 0;
        for (int i = 0; i < count; i++) {
            positions[index++] = random.nextFloat() * 100;
            positions[index++] = random.nextFloat() * 100;
        }
        return positions;
    }

    public void testActionDownMove() {
        // Test that down + one move results in correct action.
        InputProcessor processor = InputProcessor.getInstance();

        processor.addMMIListener(this);
        processor.pointerEvent(PointerAction.DOWN, Type.FINGER, System.currentTimeMillis(),
                PointerData.POINTER_1, FIRST_POS, 0);
        processor.pointerEvent(PointerAction.MOVE, Type.FINGER, System.currentTimeMillis(), PointerData.POINTER_1,
                SECOND_POS, 0);

        Assert.assertEquals(2, pointerEvents.size());
        MMIPointerEvent event = pointerEvents.get(0);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.ACTIVE, event.getAction());
        event = pointerEvents.get(1);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.MOVE, event.getAction());
        for (PointerData pointer : event.getPointerData().getPointers()) {
            Assert.assertEquals(PointerData.POINTER_1, pointer.pointer);
        }
        float[] delta = event.getPointerData().getDelta(2);
        Assert.assertArrayEquals(new float[] { SECOND_POS[0] - FIRST_POS[0], SECOND_POS[1] - FIRST_POS[1] }, delta, 0);
    }

    public void testMultipleDownMove() {
        InputProcessor processor = InputProcessor.getInstance();
        processor.addMMIListener(this);

        ArrayList<AssertMMIAction> checkList = new ArrayList<>();
        float[] positions = createRandomPositions(10);
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_1, positions, 0));
        checkList.add(sendEvent(PointerAction.MOVE, processor, PointerData.POINTER_1, positions, 2));
        checkList.add(sendEvent(PointerAction.MOVE, processor, PointerData.POINTER_1, positions, 4));
        checkList.add(sendEvent(PointerAction.MOVE, processor, PointerData.POINTER_1, positions, 6));
        checkList.add(sendEvent(PointerAction.UP, processor, PointerData.POINTER_1, positions, 8));

        assertActionPosition(pointerEvents, checkList, 0, checkList.size(), 0);
        pointerEvents.clear();
        checkList.clear();

        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_1, positions, 10));
        checkList.add(sendEvent(PointerAction.MOVE, processor, PointerData.POINTER_1, positions, 12));
        checkList.add(sendEvent(PointerAction.MOVE, processor, PointerData.POINTER_1, positions, 14));
        checkList.add(sendEvent(PointerAction.MOVE, processor, PointerData.POINTER_1, positions, 16));
        checkList.add(sendEvent(PointerAction.UP, processor, PointerData.POINTER_1, positions, 18));

        assertActionPosition(pointerEvents, checkList, 0, checkList.size(), 0);

    }

    @Test
    public void testManyActionDownUp() {
        // Test that many action down and up in different order results in correct action
        InputProcessor processor = InputProcessor.getInstance();
        processor.addMMIListener(this);

        ArrayList<AssertMMIAction> checkList = new ArrayList<>();
        float[] positions = createRandomPositions(10);
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_1, positions, 0));
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_2, positions, 2));
        checkList.add(sendEvent(PointerAction.UP, processor, PointerData.POINTER_1, positions, 4));
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_1, positions, 6));
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_3, positions, 8));
        checkList.add(sendEvent(PointerAction.UP, processor, PointerData.POINTER_2, positions, 10));
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_2, positions, 12));
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_4, positions, 14));
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_5, positions, 16));
        checkList.add(sendEvent(PointerAction.UP, processor, PointerData.POINTER_1, positions, 18));

        assertActionPosition(pointerEvents, checkList, 0, checkList.size(), 0);

    }

    public void testActionZoom() {
        // Test that move 2 fingers result in zoom
        InputProcessor processor = InputProcessor.getInstance();

        processor.addMMIListener(this);
        processor.pointerEvent(PointerAction.DOWN, Type.FINGER, System.currentTimeMillis(), PointerData.POINTER_1,
                FINGER1_FIRST, 0);
        processor.pointerEvent(PointerAction.MOVE, Type.FINGER, System.currentTimeMillis(), PointerData.POINTER_1,
                FINGER1_SECOND, 0);
        processor.pointerEvent(PointerAction.DOWN, Type.FINGER, System.currentTimeMillis(), PointerData.POINTER_2,
                FINGER2_FIRST, 0);
        processor.pointerEvent(PointerAction.MOVE, Type.FINGER, System.currentTimeMillis(), PointerData.POINTER_2,
                FINGER2_SECOND, 0);

        Assert.assertEquals(5, pointerEvents.size());
        MMIPointerEvent event = pointerEvents.get(0);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.ACTIVE, event.getAction());
        event = pointerEvents.get(1);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.MOVE, event.getAction());
        event = pointerEvents.get(2);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.ACTIVE, event.getAction());
        event = pointerEvents.get(3);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.MOVE, event.getAction());
        event = pointerEvents.get(4);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.ZOOM, event.getAction());

    }

    public void testActionZoomOutValues() {

        int LOOPCOUNT = 100;
        int x1 = 100;
        int y1 = 100;
        int x2 = 200;
        int y2 = 100;
        float deltaX1 = -5;
        float deltaX2 = 5;
        InputProcessor processor = InputProcessor.getInstance();
        processor.addMMIListener(this);

        processor.pointerEvent(PointerAction.DOWN, Type.FINGER, System.currentTimeMillis(), PointerData.POINTER_1,
                new float[] { x1,
                        y1 },
                0);
        processor.pointerEvent(PointerAction.DOWN, Type.FINGER, System.currentTimeMillis(), PointerData.POINTER_2,
                new float[] { x2,
                        y2 },
                0);

        float[] data = new float[] { x1, y1, x2, y2, deltaX1, 0, deltaX2, 0 };
        createEvents(processor, PointerAction.MOVE, data, 100);
        Iterator<MMIPointerEvent> iterator = pointerEvents.iterator();
        int found = 0;
        while (found < LOOPCOUNT && iterator.hasNext()) {
            MMIPointerEvent event = iterator.next();
            if (event.getAction() == Action.ZOOM) {
                found++;
                Vec2 zoom = event.getZoom();
                Assert.assertEquals(deltaX2 - deltaX1, zoom.vector[Vec2.MAGNITUDE], 0f);
                Assert.assertNotNull(zoom);
            }
        }
        Assert.assertEquals(LOOPCOUNT, found);
    }

    /**
     * Array containing data for start and movement.
     * 
     * @param values startX1, startY1, start X2, startY2, deltaX1, deltaY1, deltaX2, deltaY2
     */
    private void createEvents(InputProcessor processor, PointerAction action, float[] values, int count) {

        for (int i = 1; i < count + 1; i++) {
            float[] pos1 = new float[] { values[0] + values[4] * i, values[1] + values[5] * i };
            float[] pos2 = new float[] { values[2] + values[6] * i, values[3] + values[7] * i };
            processor.pointerEvent(action, Type.FINGER, i, PointerData.POINTER_1, pos1, 0);
            processor.pointerEvent(action, Type.FINGER, i, PointerData.POINTER_2, pos2, 0);
        }

    }

    public void testActionZoomSmallValues() {
        // Test that move 2 fingers result in zoom
        InputProcessor processor = InputProcessor.getInstance();

        processor.addMMIListener(this);
        processor.pointerEvent(PointerAction.DOWN, Type.FINGER, System.currentTimeMillis(), PointerData.POINTER_1,
                FINGER1_FIRST, 0);
        processor.pointerEvent(PointerAction.MOVE, Type.FINGER, System.currentTimeMillis(), PointerData.POINTER_1,
                FINGER1_SECOND, 0);
        processor.pointerEvent(PointerAction.DOWN, Type.FINGER, System.currentTimeMillis(), PointerData.POINTER_2,
                FINGER2_FIRST_SMALL, 0);
        processor.pointerEvent(PointerAction.MOVE, Type.FINGER, System.currentTimeMillis(), PointerData.POINTER_2,
                FINGER2_SECOND_SMALL, 0);

        Assert.assertEquals(4, pointerEvents.size());
        MMIPointerEvent event = pointerEvents.get(0);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.ACTIVE, event.getAction());
        event = pointerEvents.get(1);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.MOVE, event.getAction());
        event = pointerEvents.get(2);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.ACTIVE, event.getAction());
        event = pointerEvents.get(3);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.MOVE, event.getAction());
        event = pointerEvents.get(4);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.ZOOM, event.getAction());
    }

    @Override
    public void onInputEvent(MMIPointerEvent event) {
        pointerEvents.add(event);

    }

}
