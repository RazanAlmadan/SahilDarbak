package com.example.sahldarbak.ExternalApi;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.SmartItinerary.HalalInfoDTO;
import com.example.sahldarbak.DTO.SmartItinerary.HotelInsightDTO;
import com.example.sahldarbak.DTO.SmartItinerary.HotelPriceDTO;
import com.example.sahldarbak.DTO.SmartItinerary.RatingDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TavilyService {

    @Value("${tavily.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.tavily.com")
            .build();


    // SEARCH HOTEL RATINGS + ESTIMATED PRICE
    public Map searchHotelInfo(
            String hotelName,
            String city,
            String country,
            LocalDate startDate,
            LocalDate endDate) {

        String query =
                "\"" + hotelName + "\" "
                        + city + " "
                        + country
                        + " hotel rating reviews price per night "
                        + startDate + " to " + endDate
                        + " Booking.com Tripadvisor Expedia";

        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("api_key", apiKey);
        requestBody.put("query", query);
        requestBody.put("search_depth", "basic");
        requestBody.put("max_results", 8);
        requestBody.put("include_answer", false);
        requestBody.put("include_raw_content", false);
        requestBody.put("include_images", false);

        Map response = restClient.post()
                .uri("/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new ApiException(
                    "failed to get hotel information from Tavily"
            );
        }

        return response;
    }


    // GET RATINGS + ESTIMATED PRICE FROM SAME SEARCH
    public HotelInsightDTO getHotelInfo(
            String hotelName,
            String city,
            String country,
            LocalDate startDate,
            LocalDate endDate) {

        Map response = searchHotelInfo(
                hotelName,
                city,
                country,
                startDate,
                endDate
        );

        List<Map<String, Object>> results =
                (List<Map<String, Object>>) response.get("results");

        List<RatingDTO> ratings = new ArrayList<>();
        HotelPriceDTO estimatedPrice = null;

        if (results == null || results.isEmpty()) {
            return new HotelInsightDTO(
                    ratings,
                    null
            );
        }

        boolean bookingAdded = false;
        boolean tripadvisorAdded = false;
        boolean expediaAdded = false;

        for (Map<String, Object> result : results) {

            String url = (String) result.get("url");
            String content = (String) result.get("content");

            if (url == null || content == null) {
                continue;
            }


            // BOOKING.COM
            if (url.contains("booking.com")) {

                if (!bookingAdded) {

                    Double rating =
                            extractBookingRating(content);

                    Integer reviewCount =
                            extractBookingReviewCount(content);

                    if (rating != null) {

                        ratings.add(
                                new RatingDTO(
                                        rating,
                                        10.0,
                                        reviewCount,
                                        "Booking.com",
                                        url
                                )
                        );

                        bookingAdded = true;
                    }
                }

                if (estimatedPrice == null) {

                    estimatedPrice =
                            extractHotelPrice(
                                    content,
                                    "Booking.com",
                                    url
                            );
                }
            }


            // TRIPADVISOR
            else if (url.contains("tripadvisor.com")) {

                if (!tripadvisorAdded) {

                    Double rating =
                            extractTripadvisorRating(content);

                    Integer reviewCount =
                            extractTripadvisorReviewCount(content);

                    if (rating != null) {

                        ratings.add(
                                new RatingDTO(
                                        rating,
                                        5.0,
                                        reviewCount,
                                        "Tripadvisor",
                                        url
                                )
                        );

                        tripadvisorAdded = true;
                    }
                }

                if (estimatedPrice == null) {

                    estimatedPrice =
                            extractHotelPrice(
                                    content,
                                    "Tripadvisor",
                                    url
                            );
                }
            }


            // EXPEDIA
            else if (url.contains("expedia.com")) {

                if (!expediaAdded) {

                    Double rating =
                            extractExpediaRating(content);

                    Integer reviewCount =
                            extractExpediaReviewCount(content);

                    if (rating != null) {

                        ratings.add(
                                new RatingDTO(
                                        rating,
                                        10.0,
                                        reviewCount,
                                        "Expedia",
                                        url
                                )
                        );

                        expediaAdded = true;
                    }
                }

                if (estimatedPrice == null) {

                    estimatedPrice =
                            extractHotelPrice(
                                    content,
                                    "Expedia",
                                    url
                            );
                }
            }
        }

        return new HotelInsightDTO(
                ratings,
                estimatedPrice
        );
    }


    // EXTRACT ESTIMATED HOTEL PRICE
    private HotelPriceDTO extractHotelPrice(
            String content,
            String source,
            String url) {

        // £250 / GBP 250 / £250 per night
        Pattern gbpPattern = Pattern.compile(
                "(?:£|GBP\\s*)([\\d,]+(?:\\.\\d{1,2})?)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher gbpMatcher =
                gbpPattern.matcher(content);

        if (gbpMatcher.find()) {

            Double price = Double.parseDouble(
                    gbpMatcher
                            .group(1)
                            .replace(",", "")
            );

            return new HotelPriceDTO(
                    price,
                    "GBP",
                    source,
                    url
            );
        }


        // $250 / USD 250
        Pattern usdPattern = Pattern.compile(
                "(?:\\$|USD\\s*)([\\d,]+(?:\\.\\d{1,2})?)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher usdMatcher =
                usdPattern.matcher(content);

        if (usdMatcher.find()) {

            Double price = Double.parseDouble(
                    usdMatcher
                            .group(1)
                            .replace(",", "")
            );

            return new HotelPriceDTO(
                    price,
                    "USD",
                    source,
                    url
            );
        }


        // €250 / EUR 250
        Pattern eurPattern = Pattern.compile(
                "(?:€|EUR\\s*)([\\d,]+(?:\\.\\d{1,2})?)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher eurMatcher =
                eurPattern.matcher(content);

        if (eurMatcher.find()) {

            Double price = Double.parseDouble(
                    eurMatcher
                            .group(1)
                            .replace(",", "")
            );

            return new HotelPriceDTO(
                    price,
                    "EUR",
                    source,
                    url
            );
        }

        return null;
    }


    // BOOKING RATING
    private Double extractBookingRating(String content) {

        Pattern pattern = Pattern.compile(
                "(\\d+(?:\\.\\d+)?)\\s+Rated:",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return Double.parseDouble(
                    matcher.group(1)
            );
        }

        return null;
    }


    // EXTRACT BOOKING.COM REVIEW COUNT
    private Integer extractBookingReviewCount(String content) {

        Pattern pattern = Pattern.compile(
                "based on\\s+([\\d,]+)\\s+reviews",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {

            return Integer.parseInt(matcher
                            .group(1)
                            .replace(",", ""));
        }

        return null;
    }


    // TRIPADVISOR RATING
    private Double extractTripadvisorRating(
            String content) {

        Pattern pattern = Pattern.compile(
                "\\b([1-5](?:\\.\\d)?)\\s+"
                        + "(?:Excellent|Very Good|Average|Poor|Terrible)\\s+"
                        + "\\([\\d,]+\\)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return Double.parseDouble(
                    matcher.group(1)
            );
        }

        return null;
    }





    // EXPEDIA RATING
    private Double extractExpediaRating(
            String content) {

        Pattern pattern = Pattern.compile(
                "(\\d+(?:\\.\\d+)?)\\s+out of\\s+10",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return Double.parseDouble(
                    matcher.group(1)
            );
        }

        return null;
    }


    // EXTRACT EXPEDIA REVIEW COUNT
    private Integer extractExpediaReviewCount(
            String content) {

        Pattern pattern = Pattern.compile(
                "([\\d,]+)\\s+reviews",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {

            return Integer.parseInt(
                    matcher
                            .group(1)
                            .replace(",", "")
            );
        }

        return null;
    }


    // GET RESTAURANT HALAL STATUS + EVIDENCE FROM TAVILY
    public HalalInfoDTO getRestaurantHalalInfo(String restaurantName, String city, String country) {

        String query =
                "\"" + restaurantName + "\" "
                        + city + " "
                        + country
                        + " halal certified all meat halal halal menu official";

        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("api_key", apiKey);
        requestBody.put("query", query);
        requestBody.put("search_depth", "basic");
        requestBody.put("max_results", 5);
        requestBody.put("include_answer", false);
        requestBody.put("include_raw_content", false);
        requestBody.put("include_images", false);

        Map response = restClient.post()
                .uri("/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            return new HalalInfoDTO(
                    "NOT_VERIFIED",
                    null,
                    null,
                    null
            );
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

        if (results == null || results.isEmpty()) {
            return new HalalInfoDTO(
                    "NOT_VERIFIED",
                    null,
                    null,
                    null
            );
        }


        for (Map<String, Object> result : results) {

            String url = (String) result.get("url");
            String content = (String) result.get("content");
            String title = (String) result.get("title");

            if (url == null || content == null) {
                continue;
            }

            // Make sure the result is actually about the same restaurant
            if (!isSameRestaurant(restaurantName, title, content)) {
                continue;
            }

            String lowerContent = content.toLowerCase();

            // STRONG HALAL EVIDENCE
            if (lowerContent.contains("halal certified")
                    || lowerContent.contains("halal-certified")
                    || lowerContent.contains("fully halal")
                    || lowerContent.contains("100% halal")
                    || lowerContent.contains("all meat is halal")
                    || lowerContent.contains("all meats are halal")) {

                return new HalalInfoDTO(
                        "FULLY_HALAL",
                        extractHalalEvidence(content),
                        getSourceName(url),
                        url
                );
            }

            // PARTIAL HALAL EVIDENCE
            if (lowerContent.contains("halal options")
                    || lowerContent.contains("halal option")
                    || lowerContent.contains("halal menu")) {

                return new HalalInfoDTO(
                        "PARTIAL_HALAL",
                        extractHalalEvidence(content),
                        getSourceName(url),
                        url
                );
            }
        }

        return new HalalInfoDTO(
                "NOT_VERIFIED",
                null,
                null,
                null
        );
    }


    //helper method

    // CHECK IF SEARCH RESULT BELONGS TO THE SAME RESTAURANT
    private boolean isSameRestaurant(String restaurantName, String title, String content) {

        String normalizedRestaurant = normalizeText(restaurantName);

        String normalizedTitle = normalizeText(title);

        String normalizedContent = normalizeText(content);

        return normalizedTitle.contains(normalizedRestaurant) || normalizedContent.contains(normalizedRestaurant);
    }

    // NORMALIZE TEXT FOR RESTAURANT NAME COMPARISON
    private String normalizeText(String text) {

        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }


    // GET SOURCE NAME FROM RESULT URL
    private String getSourceName(String url) {

        if (url.contains("tripadvisor")) {
            return "Tripadvisor";
        }

        if (url.contains("google")) {
            return "Google";
        }

        if (url.contains("facebook")) {
            return "Facebook";
        }

        return "Web Source";
    }

    // EXTRACT SHORT HALAL EVIDENCE FROM SEARCH CONTENT
    private String extractHalalEvidence(String content) {

        if (content == null) {
            return null;
        }

        int maxLength = 250;

        if (content.length() <= maxLength) {
            return content;
        }

        return content.substring(0, maxLength);
    }

    // EXTRACT TRIPADVISOR REVIEW COUNT
    private Integer extractTripadvisorReviewCount(
            String content) {

        Pattern pattern = Pattern.compile(
                "All reviews\\s*\\(([\\d,]+)\\)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {

            return Integer.parseInt(
                    matcher
                            .group(1)
                            .replace(",", "")
            );
        }

        return null;
    }
}