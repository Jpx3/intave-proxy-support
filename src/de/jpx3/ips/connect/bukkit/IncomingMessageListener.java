package de.jpx3.ips.connect.bukkit;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import de.jpx3.ips.connect.bukkit.protocol.Packet;
import de.jpx3.ips.connect.bukkit.protocol.PacketSerialisationUtilities;
import de.jpx3.ips.connect.bukkit.protocol.exceptions.InvalidPacketException;
import de.jpx3.ips.connect.bukkit.protocol.exceptions.ProtocolVersionMismatchException;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import static de.jpx3.ips.connect.bukkit.MessengerService.*;

public final class IncomingMessageListener implements Listener {
  private MessengerService messengerService;

  private IncomingMessageListener(MessengerService messengerService) {
    this.messengerService = messengerService;
  }

  public static IncomingMessageListener create(
    MessengerService messengerService) {
    return new IncomingMessageListener(messengerService);
  }

  @SuppressWarnings("unused")
  @EventHandler(priority = EventPriority.LOWEST)
  public void onPluginMessageReceive(PluginMessageEvent event) {
    if (isUpstream(event.getSender()) ||
        !isMarkedAsIntaveChannel(event.getTag())
    ) {
      return;
    }

    event.setCancelled(true);

    UserConnection receiverConnection = (UserConnection) event.getReceiver();
    ByteArrayDataInput inputData = newByteArrayDataInputFrom(event.getData());

    try {
      String channelName = readChannelName(inputData);
      if (!channelName.equalsIgnoreCase(PROTOCOL_HEADER)) {
        return;
      }

      int protocolVersion = readProtocolVersion(inputData);
      if (protocolVersion != PROTOCOL_VERSION) {
        String invalidVersionExceptionMessage = String.format(
          "Invalid protocol version (Ours: %s Packet: %s)",
          PROTOCOL_VERSION,
          protocolVersion
        );

        throw new ProtocolVersionMismatchException(
          invalidVersionExceptionMessage
        );
      }

      Packet constructedPacket = constructPacketFrom(inputData);

      String footer = readFooter(inputData);
      if (!footer.equalsIgnoreCase("IPC_END")) {
        throw new InvalidPacketException("Invalid end of packet");
      }

      messengerService.broadcastPacketToListeners(
        receiverConnection,
        constructedPacket
      );
    } catch (Exception exception) {
      throw new IllegalStateException("Could not handle incoming packet", exception);
    }
  }

  private String readChannelName(ByteArrayDataInput byteArrayWrapper)
    throws IllegalStateException {
    return byteArrayWrapper.readUTF();
  }

  private int readProtocolVersion(ByteArrayDataInput byteArrayWrapper)
    throws IllegalStateException {
    return byteArrayWrapper.readInt();
  }

  private int readPacketIdentifier(ByteArrayDataInput byteArrayWrapper)
    throws IllegalStateException {
    return byteArrayWrapper.readInt();
  }

  private Packet constructPacketFrom(ByteArrayDataInput byteArrayDataInput)
    throws InstantiationException, IllegalAccessException {
    int packetId = readPacketIdentifier(byteArrayDataInput);
    return constructPacketFrom(byteArrayDataInput, packetId);
  }

  private Packet constructPacketFrom(ByteArrayDataInput byteArrayDataInput, int packetId)
    throws IllegalAccessException, InstantiationException {
    return PacketSerialisationUtilities.deserializeUsing(packetId, byteArrayDataInput);
  }

  private String readFooter(ByteArrayDataInput byteArrayWrapper)
    throws IllegalStateException {
    return byteArrayWrapper.readUTF();
  }

  private ByteArrayDataInput newByteArrayDataInputFrom(byte[] byteArray) {
    //noinspection UnstableApiUsage
    return ByteStreams.newDataInput(byteArray);
  }

  private boolean isMarkedAsIntaveChannel(String channelTag) {
    return channelTag.equalsIgnoreCase(INCOMING_CHANNEL) ||
      channelTag.equalsIgnoreCase(OUTGOING_CHANNEL);
  }

  private boolean isUpstream(Connection connection) {
    return connection instanceof UserConnection;
  }
}
