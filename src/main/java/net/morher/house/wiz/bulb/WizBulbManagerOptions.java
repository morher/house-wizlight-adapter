package net.morher.house.wiz.bulb;

import java.net.InetAddress;
import java.time.Duration;

public interface WizBulbManagerOptions {

  InetAddress broadcastAddress();

  Duration pollInterval();

  Duration availablilityTimeout();
}
