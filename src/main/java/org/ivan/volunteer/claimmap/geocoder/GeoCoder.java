package org.ivan.volunteer.claimmap.geocoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

@Component
public class GeoCoder {
    private static final String GEOCODER_URL = "https://geocode-maps.yandex.ru/1.x?apikey=4974aee3-5e70-4487-8a57-b7617d504b23&format=json";

    private final ConcurrentMap<String, GeoObject> geoObjectCache = new ConcurrentHashMap<>();
    // t0d0 feign client here?
    private final RestOperations restClient = new RestTemplate();
    private final UriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(GEOCODER_URL);
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public GeoObject resolve(String address) {
        return geoObjectCache.computeIfAbsent(address, this::resolveExternal);
    }

    private GeoObject resolveExternal(String address) {
        URI uri = uriBuilderFactory.builder().queryParam("geocode", address)
            .build();

        String responseStr = restClient.getForObject(uri, String.class);

        try {
            JsonNode tree = jsonMapper.readTree(responseStr);
            // t0d0 handle non-single and failed results gracefully
            String coordStr = tree.path("response").path("GeoObjectCollection").path("featureMember").path(0)
                .path("GeoObject").path("Point").path("pos").textValue();
            String[] parsedCoord = coordStr.split(" +");
            return new GeoObject(parsedCoord[1], parsedCoord[0]);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
