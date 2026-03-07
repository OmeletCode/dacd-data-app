package org.ulpgc.dacd;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SpaceXParserTest {

    @Test
    public void testParseValidJsonReturnsSatelliteListWithTelemetry() {
        // 1. PREPARACIÓN (Añadimos latitud, longitud y velocidad falsas)
        String fakeJson = "[{" +
                "\"spaceTrack\": {\"OBJECT_NAME\": \"STARLINK-CANARIAS-1\"}," +
                "\"latitude\": 28.1234," +
                "\"longitude\": -15.4321," +
                "\"velocity_kms\": 7.5," +
                "\"id\": \"id-falso-123\"" +
                "}]";

        SpaceXParser parser = new SpaceXParser();

        // 2. EJECUCIÓN
        List<Satellite> result = parser.parse(fakeJson);

        // 3. COMPROBACIÓN (Verificamos que extrae los números correctamente)
        assertNotNull(result);
        assertEquals(1, result.size());

        Satellite sat = result.get(0);
        assertEquals("STARLINK-CANARIAS-1", sat.name());
        assertEquals(28.1234, sat.latitude());
        assertEquals(-15.4321, sat.longitude());
        assertEquals(7.5, sat.velocity());
    }
}