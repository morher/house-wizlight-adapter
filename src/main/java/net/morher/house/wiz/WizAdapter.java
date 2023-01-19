package net.morher.house.wiz;

import net.morher.house.api.context.HouseAdapter;
import net.morher.house.api.context.HouseMqttContext;
import net.morher.house.wiz.bulb.WizBulbManager;
import net.morher.house.wiz.bulb.WizBulbManagerImpl;
import net.morher.house.wiz.config.WizAdapterConfig;
import net.morher.house.wiz.config.WizSection;
import net.morher.house.wiz.lamp.WizLampController;
import net.morher.house.wiz.style.StyleManager;
import net.morher.house.wiz.style.StyleManagerImpl;
import net.morher.house.wiz.udp.WizUdpConnector;

public class WizAdapter implements HouseAdapter {

  public static void main(String[] args) {
    new WizAdapter().run(new HouseMqttContext("wiz-adapter"));
  }

  @Override
  public void run(HouseMqttContext ctx) {
    WizSection config = ctx.loadAdapterConfig(WizAdapterConfig.class).getWiz();

    WizBulbManager bulbManager = new WizBulbManagerImpl(new WizUdpConnector(), config.getOptions());

    StyleManager styleManager = new StyleManagerImpl().configure(config.getStyles());

    WizLampController wiz = new WizLampController(ctx.deviceManager(), bulbManager, styleManager);
    wiz.configure(config);
  }
}
