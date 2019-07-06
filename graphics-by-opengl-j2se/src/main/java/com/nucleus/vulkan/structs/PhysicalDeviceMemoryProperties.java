package com.nucleus.vulkan.structs;

import java.util.ArrayList;

public class PhysicalDeviceMemoryProperties {

    public static class MemoryType {

        protected int bits;
        protected int heapIndex;

        public MemoryType(int bits, int heapIndex) {
            this.bits = bits;
            this.heapIndex = heapIndex;
        }

        public enum MemoryPropertyFlagBit {
            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT(1),
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT(2),
            VK_MEMORY_PROPERTY_HOST_COHERENT_BIT(4),
            VK_MEMORY_PROPERTY_HOST_CACHED_BIT(8),
            VK_MEMORY_PROPERTY_LAZILY_ALLOCATED_BIT(10),
            VK_MEMORY_PROPERTY_PROTECTED_BIT(20);

            public final int mask;

            private MemoryPropertyFlagBit(int mask) {
                this.mask = mask;
            }
        };

        public ArrayList<MemoryPropertyFlagBit> getFlagBits(int bits) {
            ArrayList<MemoryPropertyFlagBit> result = new ArrayList<MemoryPropertyFlagBit>();
            for (MemoryPropertyFlagBit flag : MemoryPropertyFlagBit.values()) {
                if ((flag.mask & bits) != 0) {
                    result.add(flag);
                }
            }
            return result;
        }

        @Override
        public String toString() {
            StringBuffer result = new StringBuffer("Heapindex: " + heapIndex + "\n");
            ArrayList<MemoryPropertyFlagBit> flags = getFlagBits(bits);
            for (MemoryPropertyFlagBit f : flags) {
                result.append(f.name() + "   ");
            }
            return result.toString();
        }

    }

    public static class MemoryHeap {

        protected long size;
        protected int bits;

        public MemoryHeap(long size, int bits) {
            this.size = size;
            this.bits = bits;
        }

        public enum MemoryHeapFlagBits {
            VK_MEMORY_HEAP_DEVICE_LOCAL_BIT(1),
            VK_MEMORY_HEAP_MULTI_INSTANCE_BIT(2),
            VK_MEMORY_HEAP_MULTI_INSTANCE_BIT_KHR(VK_MEMORY_HEAP_MULTI_INSTANCE_BIT.mask);

            public final int mask;

            private MemoryHeapFlagBits(int mask) {
                this.mask = mask;
            }

        };

    }

    protected MemoryType[] memoryTypes;
    protected MemoryHeap[] memoryHeap;

    protected String getString() {
        StringBuffer result = new StringBuffer(Integer.toString(memoryTypes.length) + " Memory types:\n");
        for (MemoryType mt : memoryTypes) {
            result.append(mt.toString() + "\n");
        }
        return result.toString();

    }

}
