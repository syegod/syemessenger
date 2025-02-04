package io.syemessenger.api;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetPageable implements Pageable {
  private final int offset;
  private final int limit;
  private final Sort sort;

  public OffsetPageable(int offset, int limit, Sort sort) {
    if (offset < 0) {
      throw new IllegalArgumentException("Offset must not be less than zero!");
    }
    if (limit < 1) {
      throw new IllegalArgumentException("Limit must be greater than zero!");
    }
    this.offset = offset;
    this.limit = limit;
    this.sort = sort != null ? sort : Sort.unsorted();
  }

  @Override
  public int getPageNumber() {
    return offset / limit;
  }

  @Override
  public int getPageSize() {
    return limit;
  }

  @Override
  public long getOffset() {
    return offset;
  }

  @Override
  public Sort getSort() {
    return sort;
  }

  @Override
  public Pageable next() {
    return new OffsetPageable(offset + limit, limit, sort);
  }

  @Override
  public Pageable previousOrFirst() {
    return hasPrevious() ? new OffsetPageable(offset - limit, limit, sort) : this;
  }

  @Override
  public Pageable first() {
    return new OffsetPageable(0, limit, sort);
  }

  @Override
  public boolean hasPrevious() {
    return offset > 0;
  }

  // TODO: deal with it
  @Override
  public Pageable withPage(int offset) {
    offset += offset;
    return new OffsetPageable(offset, limit, sort);
  }
}
