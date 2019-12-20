package de.jpx3.ips.connect.bukkit.protocol.packets;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import de.jpx3.ips.connect.bukkit.protocol.Packet;

import java.util.UUID;

public final class PacketInCommandExecution extends Packet {
  private UUID playerId;
  private String command;

  public PacketInCommandExecution() {
  }

  public PacketInCommandExecution(UUID playerId, String command) {
    this.playerId = playerId;
    this.command = command;
  }

  @Override
  public void applyFrom(ByteArrayDataInput input) throws IllegalStateException, AssertionError {
    Preconditions.checkNotNull(input);

    playerId = UUID.fromString(input.readUTF());
    command = input.readUTF();
  }

  @Override
  public void applyTo(ByteArrayDataOutput output) {
    Preconditions.checkNotNull(output);

    output.writeUTF(playerId.toString());
    output.writeUTF(command);
  }

  public UUID getPlayerId() {
    return playerId;
  }

  public String getCommand() {
    return command;
  }
}