import lexer.CharReader;
import lexer.WinZigLexer;
import parser.WinZigParser;
import parser.nodes.ASTNode;
import semantic.SemanticAnalyzer;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Callable;

public class WinZig implements Callable<Integer> {
    private final File file;

    public WinZig(File file) {
        this.file = file;
    }

    @Override
    public Integer call() throws Exception {
        String sourceCode = Files.readString(file.toPath());
        CharReader charReader = CharReader.from(sourceCode);
        WinZigLexer lexer = new WinZigLexer(charReader);
        WinZigParser parser = new WinZigParser(lexer);
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        ASTNode node = parser.parse();
        analyzer.analyze(node);
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
            System.err.println("Something went wrong...");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
