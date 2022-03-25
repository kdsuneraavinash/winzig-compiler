package semantic.attrs;

public enum BinaryOpType {
    BAND("BAND"), //  Xl and Xr
    BOR("BOR"), // Xl or  Xr
    BPLUS("BPLUS"), // Xl +   Xr
    BMINUS("BMINUS"), // Xl -   Xr
    BMULT("BMULT"), // Xl *   Xr
    BDIV("BDIV"), // Xl div Xr
    BMOD("BMOD"), // Xl mod Xr
    BEQ("BEQ"), // Xl =   Xr
    BNE("BNE"), // Xl <>  Xr
    BLE("BLE"), // Xl <=  Xr
    BGE("BGE"), // Xl >=  Xr
    BLT("BLT"), // Xl <   Xr
    BGT("BGT"); // Xl >   Xr

    private final String name;

    BinaryOpType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
