package omero.gateway.model;

public class TableDataColumn {

    private String name;

    private int index;

    private Class<?> type;

    public TableDataColumn(String name, int index, Class<?> type) {
        this.name = name;
        this.index = index;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + index;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((type == null) ? 0 : type.getCanonicalName().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TableDataColumn other = (TableDataColumn) obj;
        if (index != other.index)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.getCanonicalName().equals(
                other.type.getCanonicalName()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String s = name != null ? name : "";
        if (index > -1) {
            s += " (" + index + ")";
        }
        s += " [" + type.getSimpleName() + "]";
        return s;
    }

}
