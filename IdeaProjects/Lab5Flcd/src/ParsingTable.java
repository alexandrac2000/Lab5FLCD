import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsingTable {
    private Map<TableEntityStructure<String, String>, TableEntityStructure<List<String>, Integer>> parsingTable = new HashMap<>();

    public void put(TableEntityStructure<String, String> key, TableEntityStructure<List<String>, Integer> value) {
        parsingTable.put(key, value);
    }

    public TableEntityStructure<List<String>, Integer> get(TableEntityStructure<String, String> key) {
        for (Map.Entry<TableEntityStructure<String, String>, TableEntityStructure<List<String>, Integer>> entry : parsingTable.entrySet()) {
            if (entry.getValue() != null) {
                TableEntityStructure<String, String> currentKey = entry.getKey();
                TableEntityStructure<List<String>, Integer> currentValue = entry.getValue();

                if (currentKey.getPosition().equals(key.getPosition()) && currentKey.getValue().equals(key.getValue())) {
                    return currentValue;
                }
            }
        }

        return null;
    }

    public boolean containsKey(TableEntityStructure<String, String> key) {
        boolean result = false;
        for (TableEntityStructure<String, String> currentKey : parsingTable.keySet()) {
            if (currentKey.getPosition().equals(key.getPosition()) && currentKey.getPosition().equals(key.getPosition())) {
                result = true;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<TableEntityStructure<String, String>, TableEntityStructure<List<String>, Integer>> entry : parsingTable.entrySet()) {
            if (entry.getValue() != null) {
                TableEntityStructure<String, String> key = entry.getKey();
                TableEntityStructure<List<String>, Integer> value = entry.getValue();

                sb.append("M[").append(key.getPosition()).append(",").append(key.getValue()).append("] = [")
                        .append(value.getValue()).append(",").append(value.getValue()).append("]\n");
            }
        }

        return sb.toString();
    }
}
