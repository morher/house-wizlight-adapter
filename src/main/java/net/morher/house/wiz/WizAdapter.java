package net.morher.house.wiz;

import net.morher.house.api.context.HouseAdapter;
import net.morher.house.api.context.HouseMqttContext;
import net.morher.house.wiz.config.WizAdapterConfig;

public class WizAdapter implements HouseAdapter {

    public static void main(String[] args) {
        new WizAdapter().run(new HouseMqttContext("wiz-adapter"));
    }

    @Override
    public void run(HouseMqttContext ctx) {
        WizBulbController wiz = new WizBulbController(ctx.deviceManager());
        wiz.configure(ctx.loadAdapterConfig(WizAdapterConfig.class).getWiz());

    }

}
