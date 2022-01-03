import javax.swing.event.ListDataEvent;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(final String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);
        Grammar grammar = null;
        ParserLL1 parser = new ParserLL1("C:\\Users\\alexa\\IdeaProjects\\Lab5Flcd\\src\\g1.txt");
        try {
            try {
                String file = "C:\\Users\\alexa\\IdeaProjects\\Lab5Flcd\\src\\g1.txt";
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
                String nonTerminal = "A";
                var production = grammar.getProductionsForNonterminal(nonTerminal);
                System.out.println(production);
                System.out.println("--------------");
                System.out.println(ParserLL1.first(grammar));
                System.out.println(ParserLL1.follow(grammar));
                //List<String> w = Arrays.asList(scanner.nextLine().replace("\n", "").split(" "));
                //parser.printStringOfProductions(w);

                BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\alexa\\IdeaProjects\\Lab5Flcd\\src\\seq.txt"));
                List<String> w = Arrays.asList(bufferedReader.readLine().replace("\n", "").split(" "));
                if (parser.printStringOfProductions(w)) {
                    Tree tree = new Tree(grammar, parser);
                    BufferedWriter bufferedWriter;
                    bufferedWriter = new BufferedWriter(new FileWriter("C:\\Users\\alexa\\IdeaProjects\\Lab5Flcd\\src\\out1.txt"));
                    final String parserOutput = tree.getTableOutput(parser.getPiProductions());
                    System.out.println(parserOutput);
                    bufferedWriter.write(parserOutput);
                    bufferedWriter.close();
                }



                bufferedReader = new BufferedReader(new FileReader("C:\\Users\\alexa\\IdeaProjects\\Lab5Flcd\\src\\PIF.out"));
                w = Arrays.asList(bufferedReader.readLine().replace("\n", "").split(" "));
                if (parser.printStringOfProductions(w)) {
                    Tree tree = new Tree(grammar, parser);
                    BufferedWriter bufferedWriter;
                    bufferedWriter = new BufferedWriter(new FileWriter("C:\\Users\\alexa\\IdeaProjects\\Lab5Flcd\\src\\out2.txt"));
                    final String parserOutput = tree.getTableOutput(parser.getPiProductions());
                    System.out.println(parserOutput);
                    bufferedWriter.write(parserOutput);
                    bufferedWriter.close();
                }
            } catch (final Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
