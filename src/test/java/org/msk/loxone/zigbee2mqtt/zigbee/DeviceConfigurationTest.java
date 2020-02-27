package org.msk.loxone.zigbee2mqtt.zigbee;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DeviceConfigurationTest {

    private static final DeviceConfiguration.DeviceType type1 = DeviceConfiguration.DeviceType.builder().modelID("model1").manufacturerName("manu1").build();
    private static final DeviceConfiguration.DeviceType type2 = DeviceConfiguration.DeviceType.builder().modelID("model2").manufacturerName("manu1").build();
    private static final DeviceConfiguration.DeviceType type3 = DeviceConfiguration.DeviceType.builder().modelID("model1").manufacturerName("manu2").build();

    @Test
    public void testForwardTranslation() {
        DeviceConfiguration deviceConfiguration = new DeviceConfiguration();

        deviceConfiguration.addMapping(type1, "door/status", "open", "1");
        deviceConfiguration.addMapping(type1, "door/status", "closed", "0");
        deviceConfiguration.addMapping(type2, "global/status", "good", "1");
        deviceConfiguration.addMapping(type3, "global/status", "bad", "0");

        Assertions.assertEquals("1", deviceConfiguration.translateValueForward(type1, "door/status", "open"));
        Assertions.assertEquals("0", deviceConfiguration.translateValueForward(type1, "door/status", "closed"));
        Assertions.assertEquals("untranslated", deviceConfiguration.translateValueForward(type1, "door/status", "untranslated"));

        Assertions.assertEquals("good", deviceConfiguration.translateValueForward(type1, "global/status", "good"));
        Assertions.assertEquals("1", deviceConfiguration.translateValueForward(type2, "global/status", "good"));
        Assertions.assertEquals("good", deviceConfiguration.translateValueForward(type3, "global/status", "good"));

        Assertions.assertEquals("bad", deviceConfiguration.translateValueForward(type1, "global/status", "bad"));
        Assertions.assertEquals("bad", deviceConfiguration.translateValueForward(type2, "global/status", "bad"));
        Assertions.assertEquals("0", deviceConfiguration.translateValueForward(type3, "global/status", "bad"));

           }

    @Test
    public void testBackwardTranslation() {
        DeviceConfiguration deviceConfiguration = new DeviceConfiguration();

        deviceConfiguration.addMapping(type1, "door/status", "open", "1");
        deviceConfiguration.addMapping(type1, "door/status", "closed", "0");
        deviceConfiguration.addMapping(type2, "global/status", "good", "1");
        deviceConfiguration.addMapping(type3, "global/status", "bad", "0");

        Assertions.assertEquals("open", deviceConfiguration.translateValueBackward(type1, "door/status", "1"));
        Assertions.assertEquals("closed", deviceConfiguration.translateValueBackward(type1, "door/status", "0"));
        Assertions.assertEquals("untranslated", deviceConfiguration.translateValueBackward(type1, "door/status", "untranslated"));

        Assertions.assertEquals("1", deviceConfiguration.translateValueBackward(type1, "global/status", "1"));
        Assertions.assertEquals("good", deviceConfiguration.translateValueBackward(type2, "global/status", "1"));
        Assertions.assertEquals("1", deviceConfiguration.translateValueBackward(type3, "global/status", "1"));

        Assertions.assertEquals("0", deviceConfiguration.translateValueBackward(type1, "global/status", "0"));
        Assertions.assertEquals("0", deviceConfiguration.translateValueBackward(type2, "global/status", "0"));
        Assertions.assertEquals("bad", deviceConfiguration.translateValueBackward(type3, "global/status", "0"));

    }

}
