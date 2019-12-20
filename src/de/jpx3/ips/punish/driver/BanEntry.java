package de.jpx3.ips.punish.driver;

import com.google.common.base.Preconditions;

import java.util.UUID;

public final class BanEntry {
  private final UUID uuid;
  private String reason;
  private long end;

  private BanEntry(UUID uuid, String reason, long end) {
    this.uuid = uuid;
    this.reason = reason;
    this.end = end;
  }

  public static Builder builder() {
    return new Builder();
  }

  public UUID uuid() {
    return uuid;
  }

  public String reason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public long ending() {
    return end;
  }

  public void setEnd(long end) {
    this.end = end;
  }

  public boolean expired() {
    return end < System.currentTimeMillis();
  }

  public static final class Builder {
    private UUID uuid;
    private String reason;
    private long end;

    public Builder withId(UUID id) {
      Preconditions.checkNotNull(id);

      this.uuid = id;
      return this;
    }

    public Builder withReason(String reason) {
      Preconditions.checkNotNull(reason);

      this.reason = reason;
      return this;
    }

    public Builder withAnInfiniteDuration() {
      return withEnd(Long.MAX_VALUE);
    }

    public Builder withEnd(long end) {
      Preconditions.checkState(end > System.currentTimeMillis());

      this.end = end;
      return this;
    }

    public BanEntry construct() {
      Preconditions.checkNotNull(uuid);
      Preconditions.checkNotNull(reason);
      Preconditions.checkState(end > System.currentTimeMillis());

      return new BanEntry(uuid, reason, end);
    }
  }
}