package com.nucleus.scene.gltf;

import org.junit.Assert;
import org.junit.Test;

import com.nucleus.BaseTestCase;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.Accessor.ComponentType;
import com.nucleus.scene.gltf.Accessor.Type;
import com.nucleus.scene.gltf.Buffer;
import com.nucleus.scene.gltf.BufferView;
import com.nucleus.scene.gltf.BufferView.Target;
import com.nucleus.scene.gltf.GLTF;

public class AccessorTest extends BaseTestCase {

    @Test
    public void testConstructor() {
        GLTF gltf = new GLTF();
        BufferView bv = gltf.createBufferView("TEST", 100, 10, 4, Target.ARRAY_BUFFER);
        Accessor a = new Accessor(bv, 4, ComponentType.SHORT, 2, Type.VEC3);
        Assert.assertTrue(a.getBufferView().equals(bv));
        Assert.assertTrue(a.getByteOffset() == 4);
        Assert.assertTrue(a.getComponentType() == ComponentType.SHORT);
        Assert.assertTrue(a.getCount() == 2);
        Assert.assertTrue(a.getType() == Type.VEC3);
    }

}
