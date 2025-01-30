package io.syemessenger.api.room;

import java.util.List;
import java.util.StringJoiner;

public class RoomInfoList {

  private List<RoomInfo> roomInfos;
  private Integer offset;
  private Integer limit;
  private Integer totalCount;

  public List<RoomInfo> roomInfos() {
    return roomInfos;
  }

  public RoomInfoList roomInfos(List<RoomInfo> roomInfos) {
    this.roomInfos = roomInfos;
    return this;
  }

  public Integer offset() {
    return offset;
  }

  public RoomInfoList offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public Integer limit() {
    return limit;
  }

  public RoomInfoList limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public Integer totalCount() {
    return totalCount;
  }

  public RoomInfoList totalCount(Integer totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RoomInfoList.class.getSimpleName() + "[", "]")
        .add("roomInfos=" + roomInfos)
        .add("offset=" + offset)
        .add("limit=" + limit)
        .add("totalCount=" + totalCount)
        .toString();
  }
}
