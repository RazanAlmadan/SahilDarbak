package com.example.sahldarbak.ExternalApi;

import com.example.sahldarbak.DTO.SmartItinerary.ItineraryDayDTO;
import com.example.sahldarbak.DTO.SmartItinerary.PlaceRecommendationDTO;
import com.example.sahldarbak.DTO.SmartItinerary.SmartItineraryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;


    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.brevo.com")
            .build();


    // GENERAL EMAIL METHOD - CAN BE USED BY THE WHOLE TEAM
    public void sendEmail(
            String toEmail,
            String subject,
            String htmlContent) {

        try {

            Map<String, Object> sender = Map.of(
                    "name", senderName,
                    "email", senderEmail
            );

            List<Map<String, String>> receivers = List.of(
                    Map.of("email", toEmail)
            );

            Map<String, Object> requestBody = Map.of(
                    "sender", sender,
                    "to", receivers,
                    "subject", subject,
                    "htmlContent", htmlContent
            );


            restClient.post()
                    .uri("/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();


        } catch (Exception e) {

            // EMAIL FAILURE SHOULD NOT STOP THE MAIN BUSINESS FLOW
            System.out.println(
                    "failed to send email: " + e.getMessage()
            );
        }
    }


    // SEND FULL ACCEPTED ITINERARY
    public void sendFullItineraryEmail(
            String toEmail,
            String country,
            SmartItineraryDTO itinerary) {

        String subject =
                "Your SahlDarbak itinerary for " + country;

        String htmlContent =
                buildItineraryEmail(country, itinerary);

        sendEmail(
                toEmail,
                subject,
                htmlContent
        );
    }


    // BUILD ITINERARY EMAIL
    private String buildItineraryEmail(
            String country,
            SmartItineraryDTO itinerary) {

        StringBuilder email = new StringBuilder();


        email.append("""
                <html>
                <body style="
                    font-family: Arial, sans-serif;
                    line-height: 1.6;
                    max-width: 750px;
                    margin: auto;
                    padding: 20px;
                ">
                """);


        email.append("""
                <h1 style="text-align:center;">
                    SahlDarbak
                </h1>
                """);


        email.append("<h2>Your trip to ")
                .append(country)
                .append(" is ready!</h2>");


        email.append("""
                <p>
                    Your travel plan is ready!
                    Here is your complete SahlDarbak itinerary.
                </p>
                """);


        // DAILY PLAN
        email.append("<h2>Daily Itinerary</h2>");


        if (itinerary.getDays() != null) {

            for (ItineraryDayDTO day : itinerary.getDays()) {


                email.append("""
                        <div style="
                            margin-top:20px;
                            padding:15px;
                            border:1px solid #dddddd;
                            border-radius:8px;
                        ">
                        """);


                email.append("<h3>Day ")
                        .append(day.getDayNumber())
                        .append(" - ")
                        .append(day.getDate());


                if (day.getCity() != null) {

                    email.append(" - ")
                            .append(day.getCity());
                }


                email.append("</h3>");


                if (day.getPlaces() != null) {

                    for (PlaceRecommendationDTO place : day.getPlaces()) {

                        email.append("<div style=\"margin-bottom:15px;\">");


                        if (place.getSuggestedTime() != null) {

                            email.append("<strong>")
                                    .append(place.getSuggestedTime())
                                    .append("</strong> - ");
                        }


                        email.append("<strong>")
                                .append(place.getName())
                                .append("</strong>");


                        if (place.getType() != null) {

                            email.append(" (")
                                    .append(place.getType())
                                    .append(")");
                        }


                        if (place.getReason() != null) {

                            email.append("<br>")
                                    .append(place.getReason());
                        }


                        if (place.getHalalInfo() != null) {

                            email.append("<br>Halal status: ")
                                    .append(
                                            place.getHalalInfo()
                                                    .getHalalStatus()
                                    );
                        }


                        if (place.getOfficialWebsite() != null) {

                            email.append("<br><a href=\"")
                                    .append(place.getOfficialWebsite())
                                    .append("\">Official Website</a>");
                        }


                        email.append("</div>");
                    }
                }


                email.append("</div>");
            }
        }


        // HOTEL RECOMMENDATIONS
        appendRecommendations(
                email,
                "Hotel Recommendations",
                itinerary.getHotelRecommendations()
        );


        // RESTAURANT RECOMMENDATIONS
        appendRecommendations(
                email,
                "Restaurant Recommendations",
                itinerary.getRestaurantRecommendations()
        );


        // ACTIVITY RECOMMENDATIONS
        appendRecommendations(
                email,
                "Additional Activity Recommendations",
                itinerary.getActivityRecommendations()
        );


        email.append("""
                <hr style="margin-top:30px;">

                <p style="text-align:center;">
                    Have a great trip!<br>
                    <strong>SahlDarbak</strong>
                </p>

                </body>
                </html>
                """);


        return email.toString();
    }


    // ADD RECOMMENDATIONS
    private void appendRecommendations(
            StringBuilder email,
            String title,
            List<PlaceRecommendationDTO> recommendations) {

        if (recommendations == null
                || recommendations.isEmpty()) {

            return;
        }


        email.append("<h2>")
                .append(title)
                .append("</h2>");


        for (PlaceRecommendationDTO place : recommendations) {

            email.append("""
                    <div style="
                        margin-bottom:15px;
                        padding:12px;
                        border-bottom:1px solid #dddddd;
                    ">
                    """);


            email.append("<strong>")
                    .append(place.getName())
                    .append("</strong>");


            if (place.getReason() != null) {

                email.append("<br>")
                        .append(place.getReason());
            }


            // HOTEL PRICE
            if (place.getEstimatedPrice() != null) {

                email.append("<br>Estimated price per night: ")
                        .append(
                                place.getEstimatedPrice()
                                        .getEstimatedPricePerNight()
                        )
                        .append(" ")
                        .append(
                                place.getEstimatedPrice()
                                        .getCurrency()
                        );
            }


            // HALAL INFORMATION
            if (place.getHalalInfo() != null) {

                email.append("<br>Halal status: ")
                        .append(
                                place.getHalalInfo()
                                        .getHalalStatus()
                        );
            }


            // OFFICIAL WEBSITE
            if (place.getOfficialWebsite() != null) {

                email.append("<br><a href=\"")
                        .append(place.getOfficialWebsite())
                        .append("\">Official Website</a>");
            }


            email.append("</div>");
        }
    }
}