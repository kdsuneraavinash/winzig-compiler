package semantic.attrs;

public enum Instruction {
    NOP("NOP"), // -----------------------------------------------------------------------------------------------
    //                      | NOP      |                                   | Do nothing.
    HALT("HALT"), // ---------------------------------------------------------------------------------------------
    //                      | HALT     | halt                              | Stop.
    LIT("LIT"), // -----------------------------------------------------------------------------------------------
    //                      | LIT  v   | Push v on Lf                      | Literal v.
    LLV("LLV"), // -----------------------------------------------------------------------------------------------
    //                      | LLV  i   | Push (Lf i) on Lf                 | Load Local Value i.
    LGV("LGV"), // -----------------------------------------------------------------------------------------------
    //                      | LGV  i   | Push (Gf i) on Lf                 | Load Global Value i.
    SLV("SLV"), // -----------------------------------------------------------------------------------------------
    //                      | SLV  i   | Lf i <- Pop Lf                    | Store Local Value i.
    SGV("SGV"), // -----------------------------------------------------------------------------------------------
    //                      | SGV  i   | Gf i <- Pop Lf                    | Store Global Value i.
    LLA("LLA"), // -----------------------------------------------------------------------------------------------
    //                      | LLA  i   | Push (Local_Address i) on Lf      | Load Local Address i.
    LGA("LGA"), // -----------------------------------------------------------------------------------------------
    //                      | LGA  i   | Push (Global_Address i) on Lf     | Load Global Address i.
    UOP("UOP"), // -----------------------------------------------------------------------------------------------
    //                      | UOP  i   | const X = Pop Lf                  | Unary Operation i.
    //                      | Push (Unop(i,X)) on Lf            |
    BOP("BOP"), // -----------------------------------------------------------------------------------------------
    //                      | BOP  i   | const Xr,Xl = Pop Lf, Pop Lf      | Binary Operation i.
    //                      | Push (Binop(i,Xl,Xr)) on Lf       |
    POP("POP"), // -----------------------------------------------------------------------------------------------
    //                      | POP  n   | Pop n off Lf                      | Pop n values.
    DUP("DUP"), // -----------------------------------------------------------------------------------------------
    //                      | DUP      | Push (Top Lf ) on Lf              | Duplicate top of stack.
    SWAP("SWAP"), //                      | SWAP     | const One,Two = Pop Lf, Pop Lf    | Swap top two values.
    //                      | Push One on Lf                    |
    CALL("CALL"), // ---------------------------------------------------------------------------------------------
    //                      | CALL n   | Push I on Return_Stack            | Save current instruction.
    //                      | I <- Pop Lf                       | Entry point.
    //                      | Open_Frame n                      | Bump LBR (see below).
    //                      | repeat                            | back to top of loop.
    RTN("RTN"), // ----------------------------------------------------------------------------------------------
    //                      | RTN n    | const Start = Depth Lf - n        | Return top n values to caller
    //                      | if Start > 0 then                 |
    //                      |   for j=0 to n-1 do               | Move values to bottom of  frame.
    //                      |       LF j <- Lf (Start+j)        |
    //                      |   Pop Start off Lf                | Get rid of the rest.
    //                      | fi                                |
    //                      | I <- Pop Return_Stack             | Branch to return address.
    //                      | Close_Frame i                     | Un-bump (De-bump?) LBR (see below).
    GOTO("GOTO"), // ---------------------------------------------------------------------------------------------
    //                      | GOTO L   | I <- L                            | branch to L
    //                      | repeat                            | Back to top of loop.
    COND("COND"), // ---------------------------------------------------------------------------------------------
    //                      | COND L M | I <- if Pop Lf = True             | Pop Stack.  If value is:
    //                      |    then L                         |   true,  go to L
    //                      |    else M                         |   false, go to M.
    //                      | fi                                |
    //                      | repeat                            | Back to top of loop.
    CODE("CODE"), // ---------------------------------------------------------------------------------------------
    //                      | CODE F   | Push F on Lf                      | Push entry point.
    SOS("SOS"); // -----------------------------------------------------------------------------------------------
    //                      | SOS i    |Operating_System i                 | May change Lf.

    private final String name;

    Instruction(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
