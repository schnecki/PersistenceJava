package persistence;


public class DBEntry<K,V> {

    public boolean isPrimary = false;
    public K column;
    public V value;

    public DBEntry(K column, V value) {
        this.column = column;
        this.value = value;
    }

    public DBEntry(K column, V value, boolean isPrimary) {
        this.column = column;
        this.value = value;
        this.isPrimary = isPrimary;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
        public String toString() {
        return column + "=" + value + ((isPrimary) ? " (PK)" : "");
    }

}
