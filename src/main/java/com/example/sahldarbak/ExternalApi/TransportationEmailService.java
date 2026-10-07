package com.example.sahldarbak.ExternalApi;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.TransportationRouteDTO;
import com.example.sahldarbak.ExternalApi.EmailService;
import com.example.sahldarbak.Model.Trip;
import com.example.sahldarbak.Repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportationEmailService {

    private final EmailService emailService;
    private final TripRepository tripRepository;

    public void sendTransportationEmail(Integer tripId, List<TransportationRouteDTO> routes) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        if (routes == null || routes.isEmpty()) {
            throw new ApiException("transportation suggestions cannot be empty");
        }

        String email = trip.getUser().getEmail();

        String subject = "Your SahlDarbak transportation suggestions for " + trip.getCountry();

        // create the email content
        String htmlContent = buildTransportationEmail(trip.getCountry(), routes);
        // send the email
        emailService.sendEmail(email, subject, htmlContent);
    }

    private String buildTransportationEmail(
            String country,
            List<TransportationRouteDTO> routes) {

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

        email.append("<h2>Your transportation suggestions for ")
                .append(country)
                .append("</h2>");

        email.append("""
                <p>
                    Here are the transportation recommendations
                    generated for your trip.
                </p>
                """);

        for (TransportationRouteDTO route : routes) {

            email.append("""
                    <div style="
                        margin-top:20px;
                        padding:15px;
                        border:1px solid #dddddd;
                        border-radius:8px;
                    ">
                    """);

            email.append("<h3>")
                    .append(route.getFrom())
                    .append(" → ")
                    .append(route.getTo())
                    .append("</h3>");

            if (route.getRecommendedMethod() != null) {
                email.append("<p><strong>Recommended method:</strong> ")
                        .append(route.getRecommendedMethod())
                        .append("</p>");
            }

            if (route.getTransportType() != null) {
                email.append("<p><strong>Transport type:</strong> ")
                        .append(route.getTransportType())
                        .append("</p>");
            }

            if (route.getLine() != null) {
                email.append("<p><strong>Line:</strong> ")
                        .append(route.getLine())
                        .append("</p>");
            }

            if (route.getDistanceKm() != null) {
                email.append("<p><strong>Distance:</strong> ")
                        .append(route.getDistanceKm())
                        .append(" km</p>");
            }

            if (route.getDurationMinutes() != null) {
                email.append("<p><strong>Duration:</strong> ")
                        .append(route.getDurationMinutes())
                        .append(" minutes</p>");
            }

            if (route.getInstructions() != null) {
                email.append("<p><strong>Instructions:</strong><br>")
                        .append(route.getInstructions())
                        .append("</p>");
            }

            if (route.getReason() != null) {
                email.append("<p><strong>Why this method?</strong><br>")
                        .append(route.getReason())
                        .append("</p>");
            }

            email.append("</div>");
        }

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
}
