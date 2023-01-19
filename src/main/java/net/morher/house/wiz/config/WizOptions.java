package net.morher.house.wiz.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import lombok.Data;
import net.morher.house.wiz.bulb.WizBulbManagerOptions;

@Data
public class WizOptions implements WizBulbManagerOptions {
  private String broadcast = "127.255.255.255";

  private int pollInterval = 15; // Given in seconds
  private int availabilityTimeout = 120; // Given in seconds

  @Override
  public InetAddress broadcastAddress() {
    try {
      return InetAddress.getByName(broadcast);

    } catch (UnknownHostException e) {
      throw new IllegalStateException("Invalid broadcast address: " + broadcast, e);
    }
  }

  @Override
  public Duration pollInterval() {
    return Duration.ofSeconds(pollInterval);
  }

  @Override
  public Duration availablilityTimeout() {
    return Duration.ofSeconds(availabilityTimeout);
  }
}
