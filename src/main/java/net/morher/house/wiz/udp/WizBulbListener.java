package net.morher.house.wiz.udp;

import net.morher.house.wiz.bulb.WizBulbState;

public interface WizBulbListener {
  void onState(WizDeviceInfo deviceInfo, WizBulbState state);

  void onSetStateResponse(WizDeviceInfo deviceInfo, boolean success);
}
