package ee.eesti.riha.rest.util;

import org.springframework.stereotype.Component;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

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
    private static final String FILTER_PARAMETER_SEPARATOR = ":";

    public PagedRequest resolve(MultivaluedMap<String, String> queryParameters) {
        int page = extractPage(queryParameters.getFirst(PAGE_PARAMETER));
        int size = extractSize(queryParameters.getFirst(SIZE_PARAMETER));
        List<FilterParameter> filterParameters = FilterParameterExtractor.extract(
                queryParameters.get(FILTER_PARAMETER), FILTER_PARAMETER_SEPARATOR);
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
