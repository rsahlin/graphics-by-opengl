package com.nucleus.types;

/**
 * Identifier for different type of datatypes, can be used when importing/exporting to know the expected datatype.
 * 
 * @author Richard Sahlin
 *
 */
public enum DataType {

    INT(0, 4),
    FLOAT(1, 4),
    SHORT(2, 2),
    STRING(3, 0),
    RESOLUTION(4, 0),
    TEXTURE_PARAMETER(5, 0);

    private final int type;
    private final int size;

    private DataType(int type, int size) {
        this.type = type;
        this.size = size;
    }

    /**
     * Returns the identifier for the datatype, this can be used when importing or exporting to know the expected
     * datatype.
     * 
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the size, in bytes, of the internal datatype. INT = 4 bytes.
     * For variable size datatypes this has no meaning.
     * 
     * @return
     */
    public int getSize() {
        return size;
    }

}
