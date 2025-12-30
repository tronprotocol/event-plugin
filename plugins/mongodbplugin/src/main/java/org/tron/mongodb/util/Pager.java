package org.tron.mongodb.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Pager<T> implements Serializable {

  private static final long serialVersionUID = -4059909958051796316L;
  public static final String CURRENT_PAGE = "curPage";
  public static final String PAGE_SIZE = "pageSize";

  @Setter
  private List<T> list;

  @Setter
  private int nextPage = 0;

  @Setter
  private int previousPage = 0;

  @Setter
  private boolean hasNext = false;

  @Setter
  private boolean hasPrevious = false;

  @Setter
  private long totalPage = 0;

  @Setter
  private int currentPage = 0;

  @Setter
  private int pageSize = 10;

  private long totalRecords = 0;

  public static void handle(HashMap<String, Integer> hs, int currentPage, int pageSize) {
    if (currentPage < 1) {
      currentPage = 1;
    }
    int start = (currentPage - 1) * pageSize;

    hs.put("start", start);
    hs.put("offset", pageSize);
  }

  public Pager() {
    list = new ArrayList<T>();
  }

  public Pager(T t) {
    list = new ArrayList<T>();
    list.add(t);
    this.currentPage = 1;
    this.totalRecords = list.size();
    this.pageSize = 10;
    if (currentPage < 1) {
      currentPage = 1;
    }
    if ((totalRecords / pageSize) * pageSize < totalRecords) {
      setTotalPage(totalRecords / pageSize + 1);
    } else {
      setTotalPage(totalRecords / pageSize);
    }
    if (currentPage > this.getTotalPage()) {
      this.setCurrentPage(1);
    }

    if (currentPage == 1) {
      setHasPrevious(false);
    } else {
      setHasPrevious(true);
      setPreviousPage(currentPage - 1);
    }

    if (currentPage * pageSize < totalRecords) {
      setHasNext(true);
      setNextPage(currentPage + 1);
    } else {
      setHasNext(false);
    }
  }

  public Pager(List<T> list) {
    this(list, list.size(), 1, 10);
  }

  public Pager(List<T> list, long totalRecords, int currentPage) {
    this(list, totalRecords, currentPage, 10);
  }

  public Pager(List<T> list, long totalRecords, int currentPage, int pageSize) {
    this.list = list;
    this.currentPage = currentPage;
    this.totalRecords = totalRecords;
    this.pageSize = pageSize;
    if (currentPage < 1) {
      currentPage = 1;
    }
    if ((totalRecords / pageSize) * pageSize < totalRecords) {
      setTotalPage(totalRecords / pageSize + 1);
    } else {
      setTotalPage(totalRecords / pageSize);
    }
    if (currentPage > this.getTotalPage()) {
      this.setCurrentPage(1);
    }

    if (currentPage == 1) {
      setHasPrevious(false);
    } else {
      setHasPrevious(true);
      setPreviousPage(currentPage - 1);
    }

    if (currentPage * pageSize < totalRecords) {
      setHasNext(true);
      setNextPage(currentPage + 1);
    } else {
      setHasNext(false);
    }
  }

  public void setTotalRecords(long totalRecords) {
    if ((totalRecords / pageSize) * pageSize < totalRecords) {
      setTotalPage(totalRecords / pageSize + 1);
    } else {
      setTotalPage(totalRecords / pageSize);
    }
    this.totalRecords = totalRecords;
  }

  public Pagination getPagination() {
    Pagination pagination = new Pagination();
    pagination.setTotalRecords(String.valueOf(this.getTotalRecords()));
    pagination.setCurPage(String.valueOf(this.getCurrentPage()));
    pagination.setTotalPage(String.valueOf(this.getTotalPage()));
    pagination.setPrePage(String.valueOf(this.getPreviousPage()));
    pagination.setNextPage(String.valueOf(this.getNextPage()));
    pagination.setNextPage(String.valueOf(this.getNextPage()));
    pagination.setLastPage(String.valueOf(this.getTotalPage()));
    return pagination;
  }

}
