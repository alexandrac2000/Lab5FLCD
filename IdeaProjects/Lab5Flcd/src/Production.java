import java.util.List;
import java.util.Set;

public class Production {
    private String startingNonTerminalSymbol;
    private Set<List<String>> rules;

    public String getStartingNonTerminalSymbol() {
        return startingNonTerminalSymbol;
    }
    public Set<List<String>> getRules(){
        return rules;
    }
    public Production(String strNon,Set<List<String>>rls){
        startingNonTerminalSymbol=strNon;
        rules=rls;
    }
    @Override
    public String toString() {
        String s = startingNonTerminalSymbol + " -> ";
        for (var rule : rules) {
            for (var token : rule) {
                s += token + " ";
            }
            s += "| ";
        }
        return s.substring(0, s.length() - 2);
    }
}
