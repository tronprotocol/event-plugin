package org.tron.mongodb.util;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Pagination {

  private String curPage;
  private String nextPage;
  private String prePage;
  private String lastPage;
  private String firstPage = "1";
  private String totalPage;
  private String totalRecords;
}
