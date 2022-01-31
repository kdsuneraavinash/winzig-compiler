import lexer.CharReader;
import lexer.WinZigLexer;
import parser.Node;
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
        Node node = parser.parse();
        printTree(node, 0);
        return 0;
    }

    public static void printTree(Node node, int depth) {
        System.out.println("....".repeat(depth) + node.toString());
        for (Node child : node.getChildren()) {
            printTree(child, depth + 1);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new WinZig()).execute(args);
        System.exit(exitCode);
    }
}
