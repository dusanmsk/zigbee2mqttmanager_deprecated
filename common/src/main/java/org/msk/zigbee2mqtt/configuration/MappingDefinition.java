package org.msk.zigbee2mqtt.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor  // jackson
@Getter
@Setter
@EqualsAndHashCode
public class MappingDefinition {

    public DeviceType deviceType;
    public String path;
    public String originalValue;
    @EqualsAndHashCode.Exclude
    public String translatedValue;
}
