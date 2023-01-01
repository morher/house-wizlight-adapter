package net.morher.house.wiz.config;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.morher.house.api.config.DeviceName;

@Data
public class WizSection {
    private List<LampConfiguration> lamps = new ArrayList<>();

    @Data
    public static class LampConfiguration {
        private DeviceName device;
        private List<BulbConfiguration> bulbs = new ArrayList<>();
    }

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class BulbConfiguration {
        private String ip;
        private String channel;
    }
}