package io.demo.common.pager;

import com.github.pagehelper.Page;

/**
 * Pagination utility class that provides pagination information settings.
 * <p>
 * This class is used to convert the PageHelper pagination object into a custom pagination object {@link Pager}.
 * </p>
 */
public class PageHelper {

    /**
     * Set pagination information and return a custom pagination object.
     * <p>
     * This method converts the pagination data of PageHelper (such as current page, number of records per page, total number of records) into a custom {@link Pager} object.
     * </p>
     *
     * @param page PageHelper pagination object containing pagination-related information
     * @param list List of pagination query result data
     * @param <T>  Type of the data list
     * @return Custom pagination object {@link Pager} containing pagination information
     * @throws RuntimeException If an error occurs while setting pagination information, a runtime exception is thrown
     */
    public static <T> Pager<T> setPageInfo(Page page, T list) {
        try {
            Pager<T> pager = new Pager<>();
            pager.setList(list);
            pager.setPageSize(page.getPageSize());
            pager.setCurrent(page.getPageNum());
            pager.setTotal(page.getTotal());
            return pager;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while saving the current page data!", e);
        }
    }
}