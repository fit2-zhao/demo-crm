package io.demo.common.pager;

import lombok.Data;

/**
 * Pagination class that encapsulates pagination data.
 * <p>
 * This class is used to represent the results of a paginated query, including the data list, total record count, number of records per page, and current page number.
 * </p>
 *
 * @param <T> Type of the data list
 */
@Data
public class Pager<T> {

    /**
     * Data list, the specific data of the paginated query results.
     */
    private T list;

    /**
     * Total record count, indicating the total number of records that match the query criteria.
     */
    private long total;

    /**
     * Number of records per page, indicating the number of records displayed on each page.
     */
    private long pageSize;

    /**
     * Current page number, indicating which page is currently displayed.
     */
    private long current;

    /**
     * No-argument constructor, initializes an empty pagination object.
     */
    public Pager() {
    }

    /**
     * Parameterized constructor, used to initialize the pagination object.
     *
     * @param list      Data list
     * @param total     Total record count
     * @param pageSize  Number of records per page
     * @param current   Current page number
     */
    public Pager(T list, long total, long pageSize, long current) {
        this.list = list;
        this.total = total;
        this.pageSize = pageSize;
        this.current = current;
    }
}