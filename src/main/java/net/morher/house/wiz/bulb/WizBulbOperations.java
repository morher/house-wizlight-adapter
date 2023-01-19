package net.morher.house.wiz.bulb;

import java.net.InetAddress;
import net.morher.house.api.subscription.Subscription;
import net.morher.house.wiz.udp.WizBulbListener;

public interface WizBulbOperations {

  Subscription addListener(WizBulbListener listener);

  /** Request bulb state */
  void getState(InetAddress bulbAddress);

  void setState(InetAddress bulbAddress, WizBulbState stateUpdate);
}
