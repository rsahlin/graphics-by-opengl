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
    INT_ARRAY(4, 4),
    FLOAT_ARRAY(5, 4),
    SHORT_ARRAY(6, 2),
    STRING_ARRAY(7, 0),
    RESOLUTION(8, 0),
    TEXTURE_PARAMETER(9, 0);

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
