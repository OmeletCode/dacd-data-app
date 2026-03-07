package org.ulpgc.dacd;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SpaceXParserTest {

    @Test
    public void testParseValidJsonReturnsSatelliteList() {
        // 1. PREPARACIÓN (Arrange)
        String fakeJson = "[{" +
                "\"spaceTrack\": {\"OBJECT_NAME\": \"STARLINK-CANARIAS-1\"}," +
                "\"id\": \"id-falso-123\"" +
                "}]";

        SpaceXParser parser = new SpaceXParser();

        // 2. EJECUCIÓN (Act)
        List<Satellite> result = parser.parse(fakeJson);

        // 3. COMPROBACIÓN (Assert)
        assertNotNull(result, "La lista no debería ser nula");
        assertEquals(1, result.size(), "Debería haber exactamente 1 satélite en la lista");
        assertEquals("STARLINK-CANARIAS-1", result.get(0).name(), "El nombre extraído no coincide");
    }
}