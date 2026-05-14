package org.ulpgc.starlink.monitor.control;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RainFadeServiceTest {

    @Mock
    private DataMart dataMart;

    @Test
    void should_return_high_risk_when_attenuation_is_above_3db() {
        // GIVEN
        RainFadeService service = new RainFadeService(dataMart);
        double attenuation = 3.5;

        // WHEN
        String risk = service.determineRisk(attenuation);

        // THEN
        assertThat(risk).isEqualTo("HIGH");
    }

    @Test
    void should_calculate_correct_distance_between_two_points() {
        // GIVEN
        RainFadeService service = new RainFadeService(dataMart);
        // Las Palmas de Gran Canaria
        double lat1 = 28.1235;
        double lon1 = -15.4363;
        // London
        double lat2 = 51.5074;
        double lon2 = -0.1278;

        // WHEN
        double distance = service.calculateDistance(lat1, lon1, lat2, lon2);

        // THEN
        // Distance between Las Palmas and London is approx 2897 km according to Haversine
        assertThat(distance).isCloseTo(2897.0, org.assertj.core.data.Offset.offset(1.0));
    }
}
