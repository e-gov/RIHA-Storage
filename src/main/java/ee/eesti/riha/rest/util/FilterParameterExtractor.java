package ee.eesti.riha.rest.util;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Helper class that encapsulates key-value filter parameter extraction.
 */
public class FilterParameterExtractor {

    private FilterParameterExtractor() {
    }

    public static List<FilterParameter> extract(List<String> filterParams, String separator) {
        if (filterParams == null || filterParams.isEmpty()) {
            return new ArrayList<>();
        }

        List<FilterParameter> filterParameters = new ArrayList<>();
        for (String filterParamStr : filterParams) {
            FilterParameter filterParameter = extract(filterParamStr, separator);
            if (filterParameter != null) {
                filterParameters.add(filterParameter);
            }
        }

        return filterParameters;
    }

    public static FilterParameter extract(String filterParameterStr, String separator) {
        String[] terms = splitPreserveAllTokens(filterParameterStr, separator, 2);
        if (isEmpty(terms)) {
            return null;
        }

        if (isBlank(terms[0])) {
            return null;
        }

        String filterParameter = trimToNull(terms[0]);
        String filterValue = terms.length > 1 ? terms[1] : null;

        return new FilterParameter(filterParameter, filterValue);
    }

}
