# WinZig Compiler

Compiler that generates assembly code for the specified abstract machine for programs written in WinZig language. Developed for CS4542 - Compiler Design module.
Final report is found [here](170081L.pdf).

## Abstract Machine Specification

See [here](machine/machine-details.txt) for the specification for the assembly code of the abstract machine targeted by this compiler.

Generated assembly code for this assembly machine can be run using [this](machine/winzig-machine.py) script.

## Instructions

From current directory,

```bash
# Extract
$ tar xvf 170081L.tar

# Build,
$ make

# Parse only and show AST Tree,
$ java winzigc -ast examples/wizig_01

# Comoile
$ java winzigc examples/wizig_01
```

