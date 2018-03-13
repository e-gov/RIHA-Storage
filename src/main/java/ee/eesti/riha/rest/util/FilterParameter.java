package ee.eesti.riha.rest.util;

import java.util.UUID;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;

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

    public String asNullSafeString() {
        return value != null ? value : "";
    }

    public UUID asUuid() {
        return value != null ? UUID.fromString(trimToEmpty(value)) : null;
    }

    @Override
    public String toString() {
        return "FilterParameter{" +
                "property='" + property + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
