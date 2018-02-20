package ee.eesti.riha.rest.util;

import org.springframework.stereotype.Component;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Extracts {@link PagedRequest} from query parameters.
 */
@Component
public class PagedRequestArgumentResolver {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String PAGE_PARAMETER = "page";
    private static final String SIZE_PARAMETER = "size";
    private static final String SORT_PARAMETER = "sort";
    private static final String FILTER_PARAMETER = "filter";

    public PagedRequest resolve(MultivaluedMap<String, String> queryParameters) {
        int page = extractPage(queryParameters.getFirst(PAGE_PARAMETER));
        int size = extractSize(queryParameters.getFirst(SIZE_PARAMETER));
        List<FilterParameter> filterParameters = extractFilterParameters(
                queryParameters.get(FILTER_PARAMETER));
        List<SortParameter> sortParameters = extractSortParameters(queryParameters.get(SORT_PARAMETER));

        return new PagedRequest(page, size, filterParameters, sortParameters);
    }

    private int extractPage(String pageString) {
        int page = parseOrGetDefault(pageString, DEFAULT_PAGE);
        return page < 0 ? DEFAULT_PAGE : page;
    }

    private int extractSize(String sizeString) {
        int size = parseOrGetDefault(sizeString, DEFAULT_SIZE);
        return size < 1 ? DEFAULT_SIZE : size;
    }

    private List<FilterParameter> extractFilterParameters(List<String> filterParams) {
        if (filterParams == null || filterParams.isEmpty()) {
            return new ArrayList<>();
        }

        List<FilterParameter> filterParameters = new ArrayList<>();
        for (String filterParamStr : filterParams) {
            FilterParameter filterParameter = extractFilterParameter(filterParamStr);
            if (filterParameter != null) {
                filterParameters.add(filterParameter);
            }
        }

        return filterParameters;
    }

    private List<SortParameter> extractSortParameters(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<SortParameter> sortParameters = new ArrayList<>();
        for (String sortParam : sortParams) {
            SortParameter sortParameter = extractSortParameter(sortParam);
            if (sortParameter != null) {
                sortParameters.add(sortParameter);
            }
        }

        return sortParameters;
    }

    private int parseOrGetDefault(String stringValue, int defaultValue) {
        return isNotBlank(stringValue) ? Integer.parseInt(stringValue) : defaultValue;
    }

    private FilterParameter extractFilterParameter(String filterParameterStr) {
        String[] terms = split(stripToEmpty(filterParameterStr), ":", 2);
        if (isEmpty(terms)) {
            return null;
        }

        if (isBlank(terms[0])) {
            return null;
        }

        String filterParameter = trimToNull(terms[0]);
        String filterValue = terms.length > 1 ? trimToNull(terms[1]) : null;

        return new FilterParameter(filterParameter, filterValue);
    }

    private SortParameter extractSortParameter(String sortParameterStr) {
        String sortParam = stripToEmpty(sortParameterStr);
        String property = removeStart(sortParam, "-");
        if (isBlank(property)) {
            return null;
        }
        boolean descending = startsWith(sortParam, "-");

        return new SortParameter(property, !descending);
    }


}
