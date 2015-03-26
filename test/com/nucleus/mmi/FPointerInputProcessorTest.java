package com.nucleus.mmi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nucleus.mmi.MMIPointerEvent.Action;
import com.nucleus.mmi.PointerData.PointerAction;

public class FPointerInputProcessorTest implements MMIEventListener {

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
        PointerInputProcessor processor = new PointerInputProcessor();

        ArrayList<AssertMMIAction> assertValues = new ArrayList<AssertMMIAction>();
        processor.addMMIListener(this);
        createAndSend(PointerAction.DOWN, processor, PointerInputProcessor.MAX_POINTERS, assertValues);
        createAndSend(PointerAction.UP, processor, PointerInputProcessor.MAX_POINTERS, assertValues);

        Assert.assertEquals(PointerInputProcessor.MAX_POINTERS * 2, pointerEvents.size());
        assertActionPosition(pointerEvents, assertValues, 0, PointerInputProcessor.MAX_POINTERS * 2, 0);

    }

    private void assertActionPosition(List<MMIPointerEvent> events, ArrayList<AssertMMIAction> assertValues,
            int offset, int count, int assertOffset) {

        for (int i = 0; i < count; i++) {
            MMIPointerEvent event = events.get(i + offset);
            AssertMMIAction assertAction = assertValues.get(i + assertOffset);
            Assert.assertEquals(assertAction.action, event.getAction());
            Assert.assertArrayEquals(assertAction.position, event.getPointerData().getCurrentPosition(), 0);
            Assert.assertEquals(assertAction.timestamp, event.getPointerData().getCurrent().timeStamp);
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
    private void createAndSend(PointerAction action, PointerInputProcessor processor, int count,
            ArrayList<AssertMMIAction> positions) {

        for (int i = 0; i < count; i++) {
            float[] position = new float[] { i * 100, i * 100 + 1 };
            AssertMMIAction a = new AssertMMIAction(getFromPointerAction(action), i, position,
                    System.currentTimeMillis());
            positions.add(a);
            processor.pointerEvent(action, a.timestamp, i, position);
        }
    }

    private AssertMMIAction sendEvent(PointerAction action, PointerInputProcessor processor, int pointer,
            float[] coordinates,
            int pos) {

        float[] coord = new float[] { coordinates[pos++], coordinates[pos++] };
        AssertMMIAction check = new AssertMMIAction(getFromPointerAction(action), pointer, coord,
                System.currentTimeMillis());
        processor.pointerEvent(action, check.timestamp, pointer, coord);
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

    @Test
    public void testActionDownMove() {
        // Test that down + one move results in correct action.
        PointerInputProcessor processor = new PointerInputProcessor();

        processor.addMMIListener(this);
        processor.pointerEvent(PointerAction.DOWN, System.currentTimeMillis(), PointerData.POINTER_1, FIRST_POS);
        processor.pointerEvent(PointerAction.MOVE, System.currentTimeMillis(), PointerData.POINTER_1, SECOND_POS);

        Assert.assertEquals(2, pointerEvents.size());
        MMIPointerEvent event = pointerEvents.get(0);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.ACTIVE, event.getAction());
        event = pointerEvents.get(1);
        Assert.assertEquals(com.nucleus.mmi.MMIPointerEvent.Action.MOVE, event.getAction());
        for (PointerData pointer : event.getPointerData().pointerMovement) {
            Assert.assertEquals(PointerData.POINTER_1, pointer.pointer);
        }
        float[] delta = event.getPointerData().getDelta(2);
        Assert.assertArrayEquals(new float[] { SECOND_POS[0] - FIRST_POS[0], SECOND_POS[1] - FIRST_POS[1] }, delta, 0);
    }

    @Test
    public void testManyActionDownUp() {
        // Test that many action down and up in different order results in correct action
        PointerInputProcessor processor = new PointerInputProcessor();
        processor.addMMIListener(this);

        ArrayList<AssertMMIAction> checkList = new ArrayList<>();
        float[] positions = createRandomPositions(10);
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_1, positions, 0));
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_2, positions, 0));
        checkList.add(sendEvent(PointerAction.UP, processor, PointerData.POINTER_1, positions, 0));
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_1, positions, 0));
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_3, positions, 0));
        checkList.add(sendEvent(PointerAction.UP, processor, PointerData.POINTER_2, positions, 0));
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_2, positions, 0));
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_4, positions, 0));
        checkList.add(sendEvent(PointerAction.DOWN, processor, PointerData.POINTER_5, positions, 0));
        checkList.add(sendEvent(PointerAction.UP, processor, PointerData.POINTER_1, positions, 0));

        assertActionPosition(pointerEvents, checkList, 0, checkList.size(), 0);

    }

    @Test
    public void testActionZoom() {
        // Test that move 2 fingers result in zoom
        PointerInputProcessor processor = new PointerInputProcessor();

        processor.addMMIListener(this);
        processor.pointerEvent(PointerAction.DOWN, System.currentTimeMillis(), PointerData.POINTER_1, FINGER1_FIRST);
        processor.pointerEvent(PointerAction.MOVE, System.currentTimeMillis(), PointerData.POINTER_1, FINGER1_SECOND);
        processor.pointerEvent(PointerAction.DOWN, System.currentTimeMillis(), PointerData.POINTER_2, FINGER2_FIRST);
        processor.pointerEvent(PointerAction.MOVE, System.currentTimeMillis(), PointerData.POINTER_2, FINGER2_SECOND);

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

    @Test
    public void testActionZoomSmallValues() {
        // Test that move 2 fingers result in zoom
        PointerInputProcessor processor = new PointerInputProcessor();

        processor.addMMIListener(this);
        processor.pointerEvent(PointerAction.DOWN, System.currentTimeMillis(), PointerData.POINTER_1, FINGER1_FIRST);
        processor.pointerEvent(PointerAction.MOVE, System.currentTimeMillis(), PointerData.POINTER_1, FINGER1_SECOND);
        processor.pointerEvent(PointerAction.DOWN, System.currentTimeMillis(), PointerData.POINTER_2,
                FINGER2_FIRST_SMALL);
        processor.pointerEvent(PointerAction.MOVE, System.currentTimeMillis(), PointerData.POINTER_2,
                FINGER2_SECOND_SMALL);

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

    @Override
    public void inputEvent(MMIPointerEvent event) {
        pointerEvents.add(event);

    }
}
