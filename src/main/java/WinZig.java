import parser.nodes.ASTNode;
import parser.nodes.IdentifierNode;
import parser.nodes.Node;
import lexer.CharReader;
import lexer.WinZigLexer;
import parser.WinZigParser;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "winzig", mixinStandardHelpOptions = true, version = "winzig 0.1",
        description = "Compiles a winzig program.")
public class WinZig implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "The source to compile.")
    private File file;

    @Override
    public Integer call() throws Exception {
        String sourceCode = Files.readString(file.toPath());
        CharReader charReader = CharReader.from(sourceCode);
        WinZigLexer lexer = new WinZigLexer(charReader);
        WinZigParser parser = new WinZigParser(lexer);
        ASTNode node = parser.parse();
        printTree(node, 0);
        return 0;
    }

    public static void printTree(ASTNode node, int depth) {
        System.out.println(". ".repeat(depth) + node.toString());
        for (Node child : node.getChildren()) {
            if (child instanceof ASTNode) {
                printTree((ASTNode) child, depth + 1);
            } else if (child instanceof IdentifierNode) {
                System.out.println(". ".repeat(depth + 1) + child);
                System.out.println(". ".repeat(depth + 2) + ((IdentifierNode) child).getChild());
            }
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new WinZig()).execute(args);
        System.exit(exitCode);
    }
}
