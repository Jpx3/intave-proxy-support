package de.jpx3.ips.punish.driver;

import java.util.UUID;

public interface PunishmentDriver {
  void kickPlayer(UUID playerId, String kickMessage);

  void banPlayerTemporarily(UUID playerId, long endOfBanTimestamp, String banMessage);

  void banPlayer(UUID playerId, String banMessage);
}