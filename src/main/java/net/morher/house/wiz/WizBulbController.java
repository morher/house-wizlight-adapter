package net.morher.house.wiz;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.morher.house.api.devicetypes.LampDevice;
import net.morher.house.api.entity.DeviceId;
import net.morher.house.api.entity.DeviceInfo;
import net.morher.house.api.entity.DeviceManager;
import net.morher.house.api.entity.light.LightEntity;
import net.morher.house.api.entity.light.LightOptions;
import net.morher.house.api.entity.light.LightState;
import net.morher.house.api.entity.light.LightState.PowerState;
import net.morher.house.api.entity.light.LightStateHandler;
import net.morher.house.api.schedule.HouseScheduler;
import net.morher.house.wiz.config.WizSection;
import net.morher.house.wiz.udp.WizUdpMessage;
import net.morher.house.wiz.udp.WizUdpParams;

@Slf4j
public class WizBulbController {
    private final ScheduledExecutorService scheduler = HouseScheduler.get();
    private final ObjectMapper mapper = new ObjectMapper();
    private final DeviceManager deviceManager;
    private final DatagramSocket socket;

    public WizBulbController(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException("Could not create UDP socket", e);
        }
    }

    public void configure(WizSection wiz) {
        if (wiz == null) {
            return;
        }
        List<String> effects = new ArrayList<>();
        effects.add("Auto");

        for (WizSection.LampConfiguration lampConfig : wiz.getLamps()) {
            configureLamp(lampConfig, effects);
        }
    }

    private void configureLamp(WizSection.LampConfiguration lampConfig, List<String> effects) {
        DeviceId deviceId = lampConfig.getDevice().toDeviceId();

        LightEntity light = deviceManager.device(deviceId).entity(LampDevice.LIGHT);

        DeviceInfo info = new DeviceInfo();
        info.setManufacturer("Wiz");

        WizLamp wizLamp = new WizLamp(light, info, effects);
        for (WizSection.BulbConfiguration bulbConfig : lampConfig.getBulbs()) {
            try {
                InetAddress address = InetAddress.getByName(bulbConfig.getIp());
                wizLamp.addBulb(new WizBulb(address, bulbConfig.getChannel()));

            } catch (Exception e) {
                log.warn("Failed to add bulb {}", bulbConfig, e);
            }
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

    private class WizBulb {
        private final InetAddress address;

        public WizBulb(InetAddress address, String channel) {
            this.address = address;
            // TODO: Support for presets and channels
            // this.channel = channel;
        }

        public void onLightState(LightState state) {
            WizUdpParams params = new WizUdpParams();
            if (PowerState.ON.equals(state.getState())) {
                params.setState(true);
                params.setDimming((state.getBrightness() * 90 / 256) + 10);

            } else {
                params.setState(false);

            }
            WizUdpMessage message = new WizUdpMessage("setPilot", params);

            scheduler.execute(new SendBulbMessageTask(address, message));
        }
    }

    private class SendBulbMessageTask implements Runnable {
        private final InetAddress addr;
        private final WizUdpMessage message;

        public SendBulbMessageTask(InetAddress addr, WizUdpMessage message) {
            this.addr = addr;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                byte[] buf = mapper.writeValueAsBytes(message);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, 38899);
                socket.send(packet);

            } catch (Exception e) {
                log.warn("Could not send packet: ", e);
            }
        }
    }
}
