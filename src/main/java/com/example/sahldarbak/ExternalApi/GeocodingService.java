package com.example.sahldarbak.ExternalApi;

import com.example.sahldarbak.Api.ApiException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate = new RestTemplate();

    // returns [country, city], both trimmed and lowercase
    public String[] reverseGeocode(Double lat, Double lon) {
        try {
            String url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&accept-language=en&lat="
                    + lat + "&lon=" + lon;

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "SahilDarbak/1.0");

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            Map address = (Map) response.getBody().get("address");
            if (address == null)
                throw new ApiException("could not determine location from coordinates");

            String country = (String) address.get("country");
            String city = (String) address.get("city");
            if (city == null) city = (String) address.get("town");
            if (city == null) city = (String) address.get("village");
            if (city == null) city = (String) address.get("state");

            if (country == null || city == null)
                throw new ApiException("could not determine country or city");

            return new String[]{country.trim().toLowerCase(), city.trim().toLowerCase()};
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("location service failed: " + e.getMessage());
        }
    }
}