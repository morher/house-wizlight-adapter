package net.morher.house.wiz.udp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WizDeviceInfo {
  private final String mac;
  private final InetAddress address;

  public static class WizDeviceInfoBuilder {
    public WizDeviceInfoBuilder ip(String ip) {
      try {
        return address(InetAddress.getByName(ip));

      } catch (UnknownHostException e) {
        throw new IllegalArgumentException("Invalid IP (" + ip + ")", e);
      }
    }
  }
}
