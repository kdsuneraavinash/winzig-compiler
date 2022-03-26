package semantic.attrs;

public enum OperatingSystemOpType {
    TRACEX("TRACEX"), // Trace_Execution <- not TraceExecution
    DUMPMEM("DUMPMEM"), // Dump_Memory
    INPUT("INPUT"), // readln(i); Push i on Lf
    INPUTC("INPUTC"), // readln(ch); Push Ord(ch) on Lf
    OUTPUT("OUTPUT"), // write (Pop Lf)
    OUTPUTC("OUTPUTC"), // write (Chr(Pop(Lf)))
    OUTPUTL("OUTPUTL"), // writeln
    EOF("EOF"); //if eof(input) then Push True  on Lf else Push False on Lf

    private final String name;

    OperatingSystemOpType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
