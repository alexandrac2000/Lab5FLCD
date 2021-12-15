import java.util.Objects;

public class TableEntityStructure<K,V>{

    private K position;
    private V value;

    public TableEntityStructure(K p,V v){
        position=p;
        value=v;
    }
    public K getPosition(){
        return position;
    }
    public V getValue(){
        return value;
    }
    @Override
    public String toString() {
        return "Entity{" +
                "position=" + position +
                ", Value=" + value +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableEntityStructure)) return false;

        TableEntityStructure<?, ?> inst = (TableEntityStructure<?, ?>) o;

        if (position != null ? !position.equals(inst.position) : inst.position != null) return false;
        return value != null ? value.equals(inst.value) : inst.value == null;
    }
}
