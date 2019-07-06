package com.nucleus.vulkan.structs;

public abstract class QueueFamilyProperties {

    public enum QueueFlagBits {
        VK_QUEUE_GRAPHICS_BIT(1),
        VK_QUEUE_COMPUTE_BIT(2),
        VK_QUEUE_TRANSFER_BIT(4),
        VK_QUEUE_SPARSE_BINDING_BIT(8),
        VK_QUEUE_PROTECTED_BIT(16);

        public final int mask;

        private QueueFlagBits(int mask) {
            this.mask = mask;
        }

        public static String toString(int flags) {
            StringBuffer result = new StringBuffer();
            for (QueueFlagBits bits : QueueFlagBits.values()) {
                if ((flags & bits.mask) != 0) {
                    result.append(result.length() > 0 ? " | " + bits.name() : bits.name());
                }
            }
            return result.toString();
        }

    };

    protected int queueIndex;
    protected int queueFlags;
    protected int queueCount;
    protected int timestampValidBits;
    protected Extent3D minImageTransferGranularity;
    protected boolean surfaceSupportsPresent;

    /**
     * Returns the queue support flags
     * 
     * @return
     */
    public int getQueueFlags() {
        return queueFlags;
    }

    /**
     * Returns true if queue has support for the queueFlag
     * 
     * @param queueFlag
     * @return
     */
    public boolean hasSupport(QueueFlagBits queueFlag) {
        return (queueFlag.mask & getQueueFlags()) != 0;
    }

    public boolean isSurfaceSupportsPresent() {
        return surfaceSupportsPresent;
    }

    public int getQueueIndex() {
        return queueIndex;
    }

    public int getQueueCount() {
        return queueCount;
    }

    @Override
    public String toString() {
        return QueueFlagBits.toString(getQueueFlags()) + " - surface present: " +
                isSurfaceSupportsPresent() + "\n";
    }

}