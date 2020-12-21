package logiusAlgorithms.interfaces;

public enum ChunkTypes {
    TEXT_CHUNK (0),
    PARAGRAPH (1),
    UNDEFINED (1000);

    private final int value;

    ChunkTypes(int value) { this.value = value; }

    public int getValue() { return value; }

    public static ChunkTypes ChunkTypeFromInt(int value) {
        switch (value) {
            case 0:
                return TEXT_CHUNK;
            case 1:
                return PARAGRAPH;
        }
        return UNDEFINED;
    }
}
