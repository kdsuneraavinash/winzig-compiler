import lexer.CharReader;
import lexer.WinZigLexer;
import parser.WinZigParser;
import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import parser.nodes.Node;
import semantic.SemanticAnalyzer;
import semantic.attrs.Instruction;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class WinZig implements Callable<Integer> {
    private final File file;

    public WinZig(File file) {
        this.file = file;
    }

    public static void printTree(ASTNode node, int depth) {
        System.out.println(". ".repeat(depth) + node.toString());
        for (Node child : node.getChildren()) {
            if (child instanceof ASTNode) {
                printTree((ASTNode) child, depth + 1);
            } else if (child instanceof IdentifierNode) {
                System.out.println(". ".repeat(depth + 1) + child);
                String value = ((IdentifierNode) child).getIdentifierValue();
                System.out.println(". ".repeat(depth + 2) + String.format("%s(0)", value));
            }
        }
    }

    public static void printInstructions(List<Instruction> instructions) {
        for (Instruction instruction : instructions) {
            System.out.println(instruction);
        }
    }


    @Override
    public Integer call() throws Exception {
        List<String> errors = new ArrayList<>();

        String sourceCode = Files.readString(file.toPath());
        CharReader charReader = CharReader.from(sourceCode);
        WinZigLexer lexer = new WinZigLexer(charReader);
        WinZigParser parser = new WinZigParser(lexer);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(charReader);
        ASTNode node = parser.parse();
        List<Instruction> instructions = analyzer.codeGenerate(node);

        if (lexer.hasErrors()) {
            System.err.println("Lexer failed due to errors.");
            System.err.println("============================");
            System.err.println(lexer.collectErrors());
        } else if (parser.hasErrors()) {
            System.err.println("Parser failed due to errors.");
            System.err.println("============================");
            System.err.println(parser.collectErrors());
        } else if (analyzer.hasErrors()) {
            System.err.println("Semantic analyzer failed due to errors.");
            System.err.println("=======================================");
            System.err.println(analyzer.collectErrors());
        } else {
            // printTree(node, 0);
            printInstructions(instructions);
        }

        return 0;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("" +
                    "Winzigc - A winzig compiler\n" +
                    "How to run: java winzigc –ast winzig_test_programs/winzig_01");
            System.exit(1);
        }
        try {
            File file = new File(args[0]);
            WinZig compiler = new WinZig(file);
            int exitCode = compiler.call();
            System.exit(exitCode);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
