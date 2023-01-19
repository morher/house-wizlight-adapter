package net.morher.house.wiz.style;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import net.morher.house.api.entity.light.LightState;
import net.morher.house.api.entity.light.LightState.PowerState;
import net.morher.house.api.utils.color.Brightness;
import net.morher.house.api.utils.color.Color;
import net.morher.house.wiz.bulb.WizBulbState;
import net.morher.house.wiz.bulb.WizBulbState.WizBulbStateBuilder;
import net.morher.house.wiz.config.WizSection.PresetConfiguration;
import net.morher.house.wiz.config.WizSection.StyleConfiguration;

public class StyleManagerImpl implements StyleManager {
  private final Map<String, Style> styles = new HashMap<>();

  @Override
  public WizBulbState calculateState(LightState state, Collection<String> channels) {
    if (PowerState.OFF.equals(state.getState())) {
      return WizBulbState.builder().powerOn(false).build();
    }

    Brightness brightness =
        Optional.ofNullable(state.brightnessNormalized()).orElse(Brightness.FULL);
    WizBulbStateBuilder builder =
        WizBulbState.builder().powerOn(true).brightness(brightness).effect(null).temperature(2700);

    Style style = styles.get(state.getEffect());
    if (style != null) {
      style.applyTo(builder, channels, brightness);
    }

    return builder.build();
  }

  @Override
  public List<String> getStyles() {
    return new ArrayList<>(styles.keySet());
  }

  @Builder
  private static class Preset {
    private Optional<Boolean> power;
    private Optional<WizBulbEffect> effect;
    private Optional<Integer> temp;
    private Optional<Brightness> coldWhite;
    private Optional<Brightness> warmWhite;
    private Optional<Color> color;
    private Optional<Double> brightnessMultiplier;

    public void applyTo(WizBulbStateBuilder builder, Brightness brightness) {
      if (color.isPresent()) {
        builder.color(color.get());
        builder.effect(null);
        builder.temperature(null);
      }
      power.ifPresent(builder::powerOn);
      effect.ifPresent(builder::effect);
      temp.ifPresent(builder::temperature);
      coldWhite.ifPresent(builder::coldWhite);
      warmWhite.ifPresent(builder::warmWhite);
      brightnessMultiplier.ifPresent(m -> builder.brightness(brightness.multiply(m)));
    }
  }

  @Data
  private static class Style {
    private final Preset main;
    private final Map<String, Preset> channelPresets = new HashMap<>();

    public void applyTo(
        WizBulbStateBuilder builder, Collection<String> channels, Brightness brightness) {
      if (main != null) {
        main.applyTo(builder, brightness);
      }
      for (String channel : channels) {
        Preset preset = channelPresets.get(channel);
        if (preset != null) {
          preset.applyTo(builder, brightness);
          break;
        }
      }
    }
  }

  public StyleManagerImpl configure(Map<String, StyleConfiguration> stylesConfig) {
    styles.put("Auto", null);
    for (Map.Entry<String, StyleConfiguration> styleConfigEntry : stylesConfig.entrySet()) {
      styles.put(styleConfigEntry.getKey(), toStyle(styleConfigEntry.getValue()));
    }
    return this;
  }

  private Style toStyle(StyleConfiguration styleConfig) {
    Style style = new Style(toPreset(styleConfig.getMain()));
    for (Entry<String, PresetConfiguration> channelEntry : styleConfig.getChannels().entrySet()) {
      style.getChannelPresets().put(channelEntry.getKey(), toPreset(channelEntry.getValue()));
    }
    return style;
  }

  private Preset toPreset(PresetConfiguration presetConfig) {
    Optional<WizBulbEffect> effect = Optional.empty();
    if (!"White".equals(presetConfig.getScene())) {
      effect = Optional.ofNullable(WizBulbEffect.fromName(presetConfig.getScene()));
    }

    return Preset.builder()
        .power(Optional.empty())
        .effect(effect)
        .temp(Optional.ofNullable(presetConfig.getTemp()))
        .color(Optional.ofNullable(presetConfig.getColor()))
        .coldWhite(Brightness.ofNullable(presetConfig.getColdWhite()))
        .warmWhite(Brightness.ofNullable(presetConfig.getWarmWhite()))
        .brightnessMultiplier(Optional.ofNullable(presetConfig.getBrightness()))
        .build();
  }
}
