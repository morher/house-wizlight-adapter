package net.morher.house.wiz.udp;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.net.InetAddress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.morher.house.api.utils.color.Brightness;
import net.morher.house.wiz.bulb.WizBulbState;
import net.morher.house.wiz.style.WizBulbEffect;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WizUdpMessage {
  private String method;

  @JsonAlias("result")
  private WizUdpParams params = new WizUdpParams();

  public WizDeviceInfo toDeviceInfo(InetAddress addr) {
    return WizDeviceInfo.builder().mac(getParams().getMac()).address(addr).build();
  }

  public WizBulbState toBulbState() {
    return WizBulbState.builder()
        .powerOn(params.getState())
        .brightness(Brightness.ofNullable(params.getDimming(), 10, 100).orElse(null))
        .effect(WizBulbEffect.fromId(params.getSceneId()))
        .build();
  }

  public boolean isSuccess() {
    return params.getSuccess() != null && params.getSuccess();
  }
}
