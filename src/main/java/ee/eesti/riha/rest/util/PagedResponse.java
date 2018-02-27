package ee.eesti.riha.rest.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent response with paging and content information.
 */
public class PagedResponse {

    private long totalElements;
    private int size;
    private int page;
    private List content = new ArrayList<>();

    public PagedResponse(List content, long totalElements, int size, int page) {
        this.content.addAll(content);
        this.totalElements = totalElements;
        this.size = size;
        this.page = page;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List getContent() {
        return content;
    }

    public void setContent(List content) {
        this.content = content;
    }
}
