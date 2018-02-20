package ee.eesti.riha.rest.util;

/**
 * Filter parameter.
 */
public class FilterParameter {

    private String property;
    private String value;

    public FilterParameter(String property, String value) {
        this.property = property;
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }

    public String getString() {
        return value != null ? value : "";
    }

    @Override
    public String toString() {
        return "FilterParameter{" +
                "property='" + property + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
