package ee.eesti.riha.rest.util;

import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represent request with paging, sorting and filter information.
 */
public class PagedRequest {

    private int pageNumber;
    private int pageSize;
    private MultiValueMap<String, FilterParameter> filters = new LinkedMultiValueMap<>();
    private List<SortParameter> sort = new ArrayList<>();

    public PagedRequest(int pageNumber, int pageSize, List<FilterParameter> filters, List<SortParameter> sort) {
        Assert.isTrue(pageNumber >= 0, "pageNumber must be greater than or equal to zero");
        Assert.isTrue(pageSize > 0, "pageSize must be greater than zero");

        this.pageNumber = pageNumber;
        this.pageSize = pageSize;

        if (filters != null) {
            for (FilterParameter filter : filters) {
                this.filters.add(filter.getProperty(), filter);
            }
        }

        if (sort != null) {
            this.sort.addAll(sort);
        }
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean containsFilter(String property) {
        return filters.containsKey(property);
    }

    public Set<String> getFilterProperties() {
        return filters.keySet();
    }

    public FilterParameter getFirstFilter(String property) {
        return filters.getFirst(property);
    }

    public List<FilterParameter> getFilter(String property) {
        return filters.get(property);
    }

    public List<SortParameter> getSort() {
        return sort;
    }

    @Override
    public String toString() {
        return "PagedRequest{" +
                "pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", filters=" + filters +
                ", sort=" + sort +
                '}';
    }
}
