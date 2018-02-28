package ee.eesti.riha.rest.util;

/**
 * Represents ordering information
 */
public class SortParameter {

    private String property;
    private boolean ascending = true;

    public SortParameter(String property, boolean ascending) {
        this.property = property;
        this.ascending = ascending;
    }

    public String getProperty() {
        return property;
    }

    public boolean isAscending() {
        return ascending;
    }

    @Override
    public String toString() {
        return "SortParameter{" +
                "property='" + property + '\'' +
                ", ascending=" + ascending +
                '}';
    }
}
