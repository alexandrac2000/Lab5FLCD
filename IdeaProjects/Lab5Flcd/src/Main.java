import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(final String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);
        Grammar grammar = null;
        ParserLL1 parser = new ParserLL1("C:\\Users\\alexa\\IdeaProjects\\Lab5Flcd\\src\\g3.txt");
        try {
            try {
                String file = "C:\\Users\\alexa\\IdeaProjects\\Lab5Flcd\\src\\g3.txt";
                grammar = Grammar.ReadGrammar(file);

                System.out.println("Non-terminals");
                System.out.println(grammar.getNonterminals());
                System.out.println("Terminals");
                System.out.println(grammar.getTerminals());
                System.out.println("Productions");
                for (var prod : grammar.getProductions()) {
                    System.out.println(prod);
                }
                System.out.print("Non-terminal: ");
                String nonTerminal = scanner.nextLine();
                var production = grammar.getProductionsForNonterminal(nonTerminal);
                System.out.println(production);
                System.out.println("--------------");
                System.out.println(ParserLL1.first(grammar));
                System.out.println(ParserLL1.follow(grammar));
            } catch (final Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
