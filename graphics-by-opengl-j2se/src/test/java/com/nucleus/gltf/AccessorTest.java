package com.nucleus.gltf;

import org.junit.Assert;
import org.junit.Test;

import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.Accessor.ComponentType;
import com.nucleus.scene.gltf.Accessor.Type;
import com.nucleus.scene.gltf.BufferView;

public class AccessorTest {

    @Test
    public void testConstructor() {
        BufferView bv = new BufferView();
        Accessor a = new Accessor(bv, 4, ComponentType.SHORT, 2, Type.VEC3);
        Assert.assertTrue(a.getBufferView().equals(bv));
        Assert.assertTrue(a.getByteOffset() == 4);
        Assert.assertTrue(a.getComponentType() == ComponentType.SHORT);
        Assert.assertTrue(a.getCount() == 2);
        Assert.assertTrue(a.getType() == Type.VEC3);
    }

}
