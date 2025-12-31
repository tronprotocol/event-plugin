package org.tron.mongodb.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Pager<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4059909958051796316L;
	public static final String CURRENT_PAGE = "curPage";
	public static final String PAGE_SIZE = "pageSize";

	private List<T> list = null;

	private int nextPage = 0;

	private int previousPage = 0;

	private boolean hasNext = false;

	private boolean hasPrevious = false;

	private long totalPage = 0;

	private int currentPage = 0;

	private int pageSize = 10;

	private long totalRecords = 0;

	public static void handle(HashMap<String, Integer> hs, int currentPage, int pageSize) {
		if (currentPage < 1) {
			currentPage = 1;
		}
		int start = (currentPage - 1) * pageSize;
		int offset = pageSize;

		hs.put("start", start);
		hs.put("offset", offset);
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
		if (currentPage < 1)
			currentPage = 1;
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
		if (currentPage < 1)
			currentPage = 1;
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

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public boolean isHasNext() {
		return hasNext;
	}

	public void setHasNext(boolean hasNext) {
		this.hasNext = hasNext;
	}

	public boolean isHasPrevious() {
		return hasPrevious;
	}

	public void setHasPrevious(boolean hasPrevious) {
		this.hasPrevious = hasPrevious;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public int getNextPage() {
		return nextPage;
	}

	public void setNextPage(int nextPage) {
		this.nextPage = nextPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPreviousPage() {
		return previousPage;
	}

	public void setPreviousPage(int previousPage) {
		this.previousPage = previousPage;
	}

	public long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(long totalPage) {
		this.totalPage = totalPage;
	}

	public long getTotalRecords() {
		return totalRecords;
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
