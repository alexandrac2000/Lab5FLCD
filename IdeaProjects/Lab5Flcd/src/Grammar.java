import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Grammar {
    private final Set<String> nonterminals;
    private final Set<String> terminals;
    private final String startingSymbol;
    private final List<Production> productions;




    public Set<String> getNonterminals(){
        return nonterminals;
    }
    public Set<String>getTerminals(){
        return terminals;
    }
    public  String getStartingSymbol(){
        return startingSymbol;
    }
    public List<Production> getProductions(){
        return productions;
    }



public Grammar(Set<String> nonterminals, Set<String> terminals, String startingSymbol, List<Production> productions){
    this.nonterminals = nonterminals;
    this.terminals = terminals;
    this.startingSymbol = startingSymbol;
    this.productions = productions;
};
    private void validate() {
        if (!nonterminals.contains(startingSymbol)) {
            throw new RuntimeException("Starting symbol is not one of the nonterminals.");
        }
        productions.forEach(production -> {
            if (!nonterminals.contains(production.getStartingNonTerminalSymbol())) {
                throw new RuntimeException(String.format("Starting nonterminal %s from production %s is not one of the nonterminals.", production.getStartingNonTerminalSymbol(), production));
            }
            production.getRules().forEach(rule -> rule.forEach(symbol -> {
                if (!(terminals.contains(symbol) || nonterminals.contains(symbol))) {
                    throw new RuntimeException(String.format("Symbol %s from production %s is neither terminal, nor nonterminal.", symbol, production));
                }
            }));
        });
    }

    public static Grammar ReadGrammar(String file) throws FileNotFoundException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            Set<String> nonterminals = Set.of(bufferedReader.readLine().split(","));
            Set<String> terminals = Set.of(bufferedReader.readLine().split(","));
            String startingSymbol = bufferedReader.readLine();
            List<Production> productions = new ArrayList<>();

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.strip();
                final String[] tokens = line.split("->");
                final String[] leftSide = tokens[0].split("\\s+");
                if (leftSide.length != 1) {
                    throw new RuntimeException("The given grammar is not context-free!");
                }

                final String[] rightSide = tokens[1].split("\\|");
                final Set<List<String>> productionRules = new HashSet<>();
                for (final String rule : rightSide) {
                    productionRules.add(List.of(rule.strip().split("\\s+")));
                }
                final Optional<Production> productionOptional = productions.stream().filter(production -> production.getStartingNonTerminalSymbol().equals(leftSide[0])).findAny();
                if (productionOptional.isPresent()) {
                    productionOptional.get().getRules().addAll(productionRules);
                } else {
                    productions.add(new Production(leftSide[0], productionRules));
                }
            }
            Grammar grammar = new Grammar(nonterminals, terminals, startingSymbol, productions);
            grammar.validate();
            return grammar;
        } catch(Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Could not read grammar");

    }
    public Production getProductionsForNonterminal(final String nonterminal) {
        if (!nonterminals.contains(nonterminal)) {
            throw new RuntimeException("Given nonterminal is not one of the nonterminals of the grammar.");
        }
        return productions
                .stream()
                .filter(production -> production.getStartingNonTerminalSymbol().equals(nonterminal))
                .findAny()
                .orElse(null);
    }
}
