package net.morher.house.wiz.udp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class WizUdpParams {
    private Integer sceneId;
    private Integer r;
    private Integer g;
    private Integer b;
    private Integer c;
    private Integer w;
    private Integer dimming;
    private Boolean state;
}
