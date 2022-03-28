import sys


def machine_interpreter():
    global I
    while True:
        process_instruction()
        I += 1


def process_instruction():
    global I

    while True:
        words = instructions[I]
        if DEBUG:
            print("{:<20}".format(str(words)), LBR, str_data(), sep="\t")
        match words:
            case ["NOP"]:
                pass
            case ["HALT"]:
                exit()
            case ["LIT", v]:
                v = int(v)
                push_Lf(v)
            case ["LLV", i]:
                i = int(i)
                push_Lf(get_Lf(i))
            case ["LGV", i]:
                i = int(i)
                push_Lf(get_Gf(i))
            case ["SLV", i]:
                i = int(i)
                set_Lf(i, pop_Lf())
            case ["SGV", i]:
                i = int(i)
                set_Gf(i, pop_Lf())
            case ["LLA", i]:
                i = int(i)
                push_Lf(local_address(i))
            case ["LGA", i]:
                i = int(i)
                push_Lf(global_address(i))
            case ["UOP", i]:
                X = pop_Lf()
                push_Lf(unop(i, X))
            case ["BOP", i]:
                Xr, Xl = pop_Lf(), pop_Lf()
                push_Lf(binop(i, Xl, Xr))
            case ["POP", n]:
                n = int(n)
                for i in range(n):
                    pop_Lf()
            case ["DUP"]:
                push_Lf(top_Lf())
            case ["SWAP"]:
                one, two = pop_Lf(), pop_Lf()
                push_Lf(one)
                push_Lf(two)
            case ["CALL", n]:
                n = int(n)
                Return_Stack.append(I)
                I = pop_Lf()
                open_frame(n)
                continue
            case ["RTN", n]:
                n = int(n)
                start = depth_lf() - n
                if start > 0:
                    for j in range(n):
                        set_Lf(j, get_Lf(start + j))  # Changed
                    pop_n_Lf(start)  # Changed
                I = Return_Stack.pop()
                close_frame(int(instructions[I][1]))
            case ["GOTO", L]:
                L = labels[L]
                I = L
                continue
            case ["COND", L, M]:
                L = labels[L]
                M = labels[M]
                if pop_Lf():
                    I = L
                else:
                    I = M
                continue
            case ["CODE", F]:
                F = labels[F]
                push_Lf(F)
            case ["SOS", i]:
                operating_system(i)
            case x:
                raise NotImplementedError(x)
        break


def unop(i, X):
    match i:
        case "UNOT":
            return int(not X)
        case "UNEG":
            return -X
        case "USUCC":
            return X + 1
        case "UPRED":
            return X - 1
    raise NotImplementedError(i)


def binop(i, Xl, Xr):
    match i:
        case "BAND":
            return Xl and Xr
        case "BOR":
            return Xl or Xr
        case "BPLUS":
            return Xl + Xr
        case "BMINUS":
            return Xl - Xr
        case "BMULT":
            return Xl * Xr
        case "BDIV":
            return Xl // Xr
        case "BMOD":
            return Xl % Xr
        case "BEQ":
            return int(Xl == Xr)
        case "BNE":
            return int(Xl != Xr)
        case "BLE":
            return int(Xl <= Xr)
        case "BGE":
            return int(Xl >= Xr)
        case "BLT":
            return int(Xl < Xr)
        case "BGT":
            return int(Xl > Xr)
    raise NotImplementedError(i)


def operating_system(i):
    match i:
        case "INPUT":
            return push_Lf(int(input()))
        case "INPUTC":
            return push_Lf(ord(input()))
        case "OUTPUT":
            return print(pop_Lf(), end="")
        case "OUTPUTC":
            return print(chr(pop_Lf()), end="")
        case "OUTPUTL":
            return print()
        case "EOF":
            # TODO: Not implemented
            return push_Lf(int(False))
    raise NotImplementedError(i)


def local_address(i):
    return LBR + i - GBR


def global_address(i):
    return i


def open_frame(i):
    global LBR
    LBR = LBR + i


def close_frame(i):
    global LBR
    LBR = LBR - i


def depth_lf():
    return STR - LBR + 1


def push_Lf(v):
    global STR
    STR += 1
    Data_memory[STR] = v


def top_Lf():
    Data_memory[STR]


def pop_Lf():
    global STR
    v = Data_memory[STR]
    Data_memory[STR] = -1
    STR -= 1
    return v


def set_Lf(i, v):
    Data_memory[GBR + local_address(i)] = v


def get_Lf(i):
    return Data_memory[GBR + local_address(i)]


def set_Gf(i, v):
    Data_memory[GBR + global_address(i)] = v


def get_Gf(i):
    return Data_memory[GBR + global_address(i)]


def pop_n_Lf(n):
    global STR
    STR -= n


def str_data():
    if Data_memory:
        return str([Data_memory.get(i, -1) for i in range(max(Data_memory.keys()) + 1)][:STR + 1])
    return ""


Data_memory = {}
Return_Stack = []
I = 0
GBR = LBR = 0
STR = -1
DEBUG = False

if __name__ == "__main__":
    instructions = []
    labels = {}
    DEBUG = "-d" in sys.argv

    if len(sys.argv) > 1:
        with open(sys.argv[1], "r") as fr:
            for line in fr.readlines():
                line = line[: line.find("#")].rstrip()
                label, *rest = ("|" + line).split()
                labels[label[1:]] = len(instructions)
                instructions.append(rest)

        print(machine_interpreter())
    else:
        print("Usage: python", sys.argv[0], "filename")
