package net.morher.house.wiz.udp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WizUdpParams {
  private String mac;
  private Integer rssi;

  private Integer sceneId;
  private Integer r;
  private Integer g;
  private Integer b;
  private Integer c;
  private Integer w;
  private Integer dimming;
  private Boolean state;
  private Integer temp;
  private Boolean success;
}
