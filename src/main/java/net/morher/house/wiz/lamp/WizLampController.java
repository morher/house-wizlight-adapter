package net.morher.house.wiz.lamp;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.morher.house.api.devicetypes.LampDevice;
import net.morher.house.api.entity.DeviceId;
import net.morher.house.api.entity.DeviceInfo;
import net.morher.house.api.entity.DeviceManager;
import net.morher.house.api.entity.light.LightEntity;
import net.morher.house.api.entity.light.LightOptions;
import net.morher.house.api.entity.light.LightState;
import net.morher.house.api.entity.light.LightStateHandler;
import net.morher.house.wiz.bulb.WizBulbManager;
import net.morher.house.wiz.config.WizSection;
import net.morher.house.wiz.style.StyleManager;

@RequiredArgsConstructor
public class WizLampController {
  private final DeviceManager deviceManager;
  private final WizBulbManager bulbManager;
  private final StyleManager styleManager;

  public void configure(WizSection wiz) {
    if (wiz == null) {
      return;
    }

    for (WizSection.LampConfiguration lampConfig : wiz.getLamps()) {
      configureLamp(lampConfig);
    }
  }

  private void configureLamp(WizSection.LampConfiguration lampConfig) {
    DeviceId deviceId = lampConfig.getDevice().toDeviceId();

    LightEntity light = deviceManager.device(deviceId).entity(LampDevice.LIGHT);

    DeviceInfo info = new DeviceInfo();
    info.setManufacturer("Wiz");

    WizLamp wizLamp = new WizLamp(light, info, styleManager.getStyles());
    for (WizSection.BulbConfiguration bulbConfig : lampConfig.getBulbs()) {
      wizLamp.addBulb(
          new WizBulb(bulbConfig.getMac().replace(":", ""), bulbConfig.getAllChannels()));
    }
  }

  private class WizLamp {
    private final List<WizBulb> bulbs = new ArrayList<>();

    public WizLamp(LightEntity lightEntity, DeviceInfo deviceInfo, List<String> effects) {
      new LightStateHandler(lightEntity, deviceInfo, this::onLightState);
      lightEntity.setOptions(new LightOptions(true, effects));
    }

    public void addBulb(WizBulb bulb) {
      this.bulbs.add(bulb);
    }

    public void onLightState(LightState lampState) {
      this.bulbs.forEach(bulb -> bulb.onLightState(lampState));
    }
  }

  @RequiredArgsConstructor
  private class WizBulb {
    private final String mac;
    private final List<String> channels;

    public void onLightState(LightState state) {
      bulbManager.updateBulb(mac, styleManager.calculateState(state, channels));
    }
  }
}
