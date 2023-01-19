package net.morher.house.wiz.udp;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.morher.house.api.subscription.Subscription;
import net.morher.house.api.utils.color.Brightness;
import net.morher.house.api.utils.color.Color;
import net.morher.house.wiz.bulb.WizBulbOperations;
import net.morher.house.wiz.bulb.WizBulbState;
import net.morher.house.wiz.style.WizBulbEffect;

@Slf4j
public class WizUdpConnector implements WizBulbOperations {
  private static final int PORT = 38899;
  private final Collection<WizBulbListener> listeners = new ArrayList<>();
  private final ObjectMapper mapper = new ObjectMapper();
  private final DatagramSocket socket;
  private final Thread listenerThread;
  private boolean closed;

  public WizUdpConnector() {
    try {
      socket = new DatagramSocket(38900);
      socket.setBroadcast(true);

      listenerThread = new Thread(this::listen, "Wiz Light UDP listener");
      listenerThread.start();

    } catch (SocketException e) {
      throw new RuntimeException("Could not start bulb interface", e);
    }
  }

  private void listen() {
    log.debug("Started listener");
    byte[] buf = new byte[512];
    DatagramPacket packet = new DatagramPacket(buf, buf.length);
    while (!closed) {
      String data = null;
      try {
        socket.receive(packet);
        data = new String(packet.getData(), packet.getOffset(), packet.getLength(), ISO_8859_1);

        if (log.isTraceEnabled()) {
          log.trace("Data received from {}: {} ", packet.getAddress(), data);
        }

        WizUdpMessage message = mapper.readValue(data, WizUdpMessage.class);
        handleMessage(message, packet.getAddress());

      } catch (IllegalArgumentException e) {
        log.error("Failed to handle message: {}\n{}", e.getMessage(), data);

      } catch (JsonProcessingException e) {
        log.error("Failed to parse message from {}: {}", packet.getAddress(), e.getMessage());

      } catch (Exception e) {
        if (!closed) {
          log.error("Exception while listening for UDP pasckets", e);
        }
      }
    }
    log.debug("Listener stopped...");
  }

  private void handleMessage(WizUdpMessage message, InetAddress addr) {
    WizDeviceInfo deviceInfo = message.toDeviceInfo(addr);

    switch (message.getMethod()) {
      case "getPilot":
        WizBulbState state = message.toBulbState();
        listeners.forEach(l -> l.onState(deviceInfo, state));
        break;

      case "setPilot":
        listeners.forEach(l -> l.onSetStateResponse(deviceInfo, message.isSuccess()));
        break;

      default:
        throw new IllegalArgumentException("Unknown method: " + message.getMethod());
    }
  }

  @Override
  public Subscription addListener(WizBulbListener listener) {
    listeners.add(listener);
    return () -> listeners.remove(listener);
  }

  @Override
  public void getState(InetAddress bulbAddress) {
    send(bulbAddress, "getPilot", new WizUdpParams());
  }

  @Override
  public void setState(InetAddress bulbAddress, WizBulbState stateUpdate) {
    WizUdpParams params = new WizUdpParams();
    params.setState(stateUpdate.getPowerOn());
    params.setDimming(toWizDimming(stateUpdate.getBrightness()));
    // TODO Other params
    params.setSceneId(
        Optional.ofNullable(stateUpdate.getEffect()).map(WizBulbEffect::getId).orElse(null));
    params.setTemp(stateUpdate.getTemperature());
    params.setC(toWizLevel(stateUpdate.getColdWhite()));
    params.setW(toWizLevel(stateUpdate.getWarmWhite()));
    Color color = stateUpdate.getColor();
    if (color != null) {
      params.setR(color.getRed());
      params.setG(color.getGreen());
      params.setB(color.getBlue());
    }

    send(bulbAddress, "setPilot", params);
  }

  private static Integer toWizDimming(Brightness level) {
    return level != null ? level.rebase(10, 100) : null;
  }

  private static Integer toWizLevel(Brightness level) {
    return level != null ? level.rebase(0, 255) : null;
  }

  private void send(InetAddress addr, String method, WizUdpParams params) {
    WizUdpMessage message = new WizUdpMessage();
    message.setMethod(method);
    message.setParams(params);
    try {

      byte[] buf = mapper.writeValueAsBytes(message);
      DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, PORT);
      socket.send(packet);

    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to send message" + message, e);
    }
  }

  public void close() {
    closed = true;
    socket.close();
  }
}
