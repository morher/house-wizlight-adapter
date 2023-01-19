package net.morher.house.wiz.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.morher.house.api.config.DeviceName;
import net.morher.house.api.utils.color.Color;

@Data
public class WizSection {
  private WizOptions options = new WizOptions();
  private Map<String, StyleConfiguration> styles = new HashMap<>();
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
    private String mac;
    private String channel;
    private List<String> channels = new ArrayList<>();

    public List<String> getAllChannels() {
      ArrayList<String> allChannels = new ArrayList<>();
      if (channel != null) {
        allChannels.add(channel);
      }
      if (channels != null) {
        allChannels.addAll(channels);
      }
      return allChannels;
    }
  }

  @Data
  public static class StyleConfiguration {
    private PresetConfiguration main;
    private Map<String, PresetConfiguration> channels = new HashMap<>();
  }

  @Data
  public static class PresetConfiguration {
    private String scene;
    private Integer temp;
    private Color color;

    @JsonProperty("cold-white")
    private Double coldWhite;

    @JsonProperty("warm-white")
    private Double warmWhite;

    private Double brightness;
  }
}
