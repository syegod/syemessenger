package io.syemessenger.api.account;

import io.syemessenger.api.room.RoomInfo;
import java.util.List;
import java.util.StringJoiner;

public class GetRoomsResponse {

  private List<RoomInfo> roomInfos;
  private Integer offset;
  private Integer limit;
  private Integer totalCount;

  public List<RoomInfo> roomInfos() {
    return roomInfos;
  }

  public GetRoomsResponse roomInfos(List<RoomInfo> roomInfos) {
    this.roomInfos = roomInfos;
    return this;
  }

  public Integer offset() {
    return offset;
  }

  public GetRoomsResponse offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public Integer limit() {
    return limit;
  }

  public GetRoomsResponse limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public Integer totalCount() {
    return totalCount;
  }

  public GetRoomsResponse totalCount(Integer totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GetRoomsResponse.class.getSimpleName() + "[", "]")
        .add("roomInfos=" + roomInfos)
        .add("offset=" + offset)
        .add("limit=" + limit)
        .add("totalCount=" + totalCount)
        .toString();
  }
}
