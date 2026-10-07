package com.example.sahldarbak.ExternalApi;

import com.example.sahldarbak.DTO.SmartItinerary.ItineraryDayDTO;
import com.example.sahldarbak.DTO.SmartItinerary.PlaceRecommendationDTO;
import com.example.sahldarbak.DTO.SmartItinerary.SmartItineraryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;


    private final JavaMailSender javaMailSender;

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


    // SEND FULL ITINERARY - BACKWARD COMPATIBLE
    public void sendFullItineraryEmail(
            String toEmail,
            String country,
            SmartItineraryDTO itinerary) {

        sendFullItineraryEmail(
                toEmail,
                country,
                itinerary,
                "ar"
        );
    }

    private void sendItineraryEmailWithLogo(
            String toEmail,
            String subject,
            String htmlContent) {

        try {

            MimeMessage message =
                    javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(
                    senderEmail,
                    senderName
            );

            helper.setTo(toEmail);

            helper.setSubject(subject);

            helper.setText(
                    htmlContent,
                    true
            );


            ClassPathResource logo =
                    new ClassPathResource(
                            "static/images/logo.png"
                    );

            helper.addInline(
                    "sahldarbakLogo",
                    logo
            );


            javaMailSender.send(
                    message
            );


        } catch (Exception e) {

            System.out.println(
                    "failed to send itinerary email: "
                            + e.getMessage()
            );
        }
    }

    // SEND FULL ITINERARY WITH UI LANGUAGE
    public void sendFullItineraryEmail(
            String toEmail,
            String country,
            SmartItineraryDTO itinerary,
            String lang) {

        boolean english =
                "en".equalsIgnoreCase(lang);

        String subject =
                english
                        ? "Your SahlDarbak itinerary for " + country
                        : "برنامج رحلتك إلى " + country + " جاهز ✨";

        String htmlContent =
                buildItineraryEmail(
                        country,
                        itinerary,
                        english
                );

        sendItineraryEmailWithLogo(
                toEmail,
                subject,
                htmlContent
        );
    }


    // BUILD FULL EMAIL
    private String buildItineraryEmail(
            String country,
            SmartItineraryDTO itinerary,
            boolean english) {

        StringBuilder email =
                new StringBuilder();


        String direction =
                english ? "ltr" : "rtl";

        String align =
                english ? "left" : "right";


        email.append("""
            <!DOCTYPE html>
            <html>
            <body style="
                margin:0;
                padding:0;
                background:#F8F5EF;
                font-family:Arial,Tahoma,sans-serif;
            ">
            
            <table width="100%"
                   cellpadding="0"
                   cellspacing="0"
                   border="0"
                   style="background:#F8F5EF;">
            
                <tr>
                    <td align="center"
                        style="padding:30px 12px;">
            
                        <table width="680"
                               cellpadding="0"
                               cellspacing="0"
                               border="0"
                               style="
                                   max-width:680px;
                                   width:100%;
                                   background:#ffffff;
                                   border-radius:24px;
                                   overflow:hidden;
                               ">
            """);


        // HEADER
        email.append("""
            <tr>
                <td style="
                    background:#0F4C5C;
                    padding:30px;
                    text-align:center;
                ">
            """);


        email.append("""
        <img src="cid:sahldarbakLogo"
             alt="SahlDarbak"
             style="
                 max-width:180px;
                 max-height:75px;
                 display:block;
                 margin:0 auto 12px;
             ">
        """);


        email.append("""
                <div style="
                    color:#E9C46A;
                    font-size:14px;
                    font-weight:700;
                ">
            """);

        email.append(
                english
                        ? "Your trip, made easier."
                        : "رحلتك أسهل من أول خطوة ✈️"
        );

        email.append("""
                </div>
            </td>
            </tr>
            """);


        // INTRO
        email.append("""
            <tr>
                <td dir="%s"
                    style="
                        padding:28px 30px 10px;
                        text-align:%s;
                    ">
            
                    <div style="
                        background:#F8F5EF;
                        border-radius:18px;
                        padding:22px;
                    ">
            """.formatted(
                direction,
                align
        ));


        email.append("<div style=\"font-size:13px;color:#2A9D8F;font-weight:700;\">");

        email.append(
                english
                        ? "YOUR TRIP IS READY"
                        : "رحلتك جاهزة"
        );

        email.append("</div>");


        email.append("""
            <h1 style="
                margin:8px 0 8px;
                color:#0F4C5C;
                font-size:26px;
            ">
            """);

        email.append(
                english
                        ? "Your trip to "
                        : "رحلتك إلى "
        );

        email.append(
                escapeHtml(country)
        );

        email.append(" ✨</h1>");


        email.append("""
            <p style="
                margin:0;
                color:#65777B;
                line-height:1.8;
                font-size:15px;
            ">
            """);

        email.append(
                english
                        ? "We arranged your itinerary by city so every stop is clear and easy to follow."
                        : "رتبنا لك برنامج الرحلة حسب المدن عشان كل محطة تكون واضحة وسهلة عليك."
        );

        email.append("""
                    </p>
                </div>
            </td>
            </tr>
            """);


        Set<String> cities =
                getItineraryCities(
                        itinerary,
                        country
                );


        // EACH CITY
        for (String city : cities) {

            appendCitySection(
                    email,
                    city,
                    itinerary,
                    english,
                    direction,
                    align
            );
        }


        // OLD / UNASSIGNED RECOMMENDATIONS
        appendUnassignedRecommendations(
                email,
                itinerary,
                english,
                direction,
                align
        );


        // FOOTER
        email.append("""
            <tr>
                <td style="
                    padding:28px 30px;
                    text-align:center;
                    background:#0F4C5C;
                ">
            
                    <div style="
                        color:#ffffff;
                        font-size:15px;
                        margin-bottom:6px;
                    ">
            """);

        email.append(
                english
                        ? "Have a beautiful trip ✈️"
                        : "رحلة سعيدة وطريق سهل ✈️"
        );

        email.append("""
                    </div>
            
                    <div style="
                        color:#E9C46A;
                        font-weight:700;
                    ">
                        SahlDarbak
                    </div>
            
                </td>
            </tr>
            
            
                        </table>
            
                    </td>
                </tr>
            
            </table>
            
            </body>
            </html>
            """);


        return email.toString();
    }


    // APPEND ONE CITY
    private void appendCitySection(
            StringBuilder email,
            String city,
            SmartItineraryDTO itinerary,
            boolean english,
            String direction,
            String align) {

        List<ItineraryDayDTO> cityDays =
                itinerary.getDays() == null
                        ? List.of()
                        : itinerary.getDays()
                        .stream()
                        .filter(
                                day ->
                                        sameCity(
                                                day.getCity(),
                                                city
                                        )
                        )
                        .toList();


        List<PlaceRecommendationDTO> hotels =
                filterRecommendationsByCity(
                        itinerary.getHotelRecommendations(),
                        city
                );

        List<PlaceRecommendationDTO> restaurants =
                filterRecommendationsByCity(
                        itinerary.getRestaurantRecommendations(),
                        city
                );

        List<PlaceRecommendationDTO> activities =
                filterRecommendationsByCity(
                        itinerary.getActivityRecommendations(),
                        city
                );


        email.append("""
            <tr>
                <td dir="%s"
                    style="
                        padding:20px 30px 10px;
                        text-align:%s;
                    ">
            
                    <div style="
                        border:1px solid #DDEBE8;
                        border-radius:22px;
                        overflow:hidden;
                    ">
            
                        <div style="
                            background:#EAF6F3;
                            padding:20px 22px;
                        ">
            
                            <div style="
                                color:#2A9D8F;
                                font-size:12px;
                                font-weight:700;
                                margin-bottom:5px;
                            ">
            """.formatted(
                direction,
                align
        ));

        email.append(
                english
                        ? "TRIP STOP"
                        : "محطة من رحلتك"
        );

        email.append("""
                            </div>
            
                            <div style="
                                font-size:23px;
                                font-weight:800;
                                color:#0F4C5C;
                            ">
            """);

        email.append(
                escapeHtml(city)
        );

        email.append("""
                            </div>
                        </div>
            
                        <div style="padding:18px 20px;">
            """);


        // DAYS
        for (ItineraryDayDTO day : cityDays) {

            appendDay(
                    email,
                    day,
                    english
            );
        }


        appendRecommendationSection(
                email,
                english
                        ? "Suggested Hotels"
                        : "فنادق مقترحة",
                hotels,
                english
        );


        appendRecommendationSection(
                email,
                english
                        ? "Suggested Restaurants"
                        : "مطاعم مقترحة",
                restaurants,
                english
        );


        appendRecommendationSection(
                email,
                english
                        ? "More Activities"
                        : "أنشطة وتجارب إضافية",
                activities,
                english
        );


        email.append("""
                        </div>
            
                    </div>
            
                </td>
            </tr>
            """);
    }


    // APPEND DAY
    private void appendDay(
            StringBuilder email,
            ItineraryDayDTO day,
            boolean english) {

        email.append("""
            <div style="
                background:#FCFBF8;
                border:1px solid #ECE7DF;
                border-radius:16px;
                padding:16px;
                margin-bottom:14px;
            ">
            
                <div style="
                    color:#0F4C5C;
                    font-size:17px;
                    font-weight:800;
                    margin-bottom:12px;
                ">
            """);


        email.append(
                english
                        ? "Day "
                        : "اليوم "
        );

        email.append(
                day.getDayNumber()
        );

        if (day.getDate() != null) {

            email.append(" • ")
                    .append(
                            escapeHtml(
                                    day.getDate().toString()
                            )
                    );
        }


        email.append("</div>");


        if (day.getPlaces() != null) {

            for (
                    PlaceRecommendationDTO place
                    : day.getPlaces()
            ) {

                appendPlace(
                        email,
                        place,
                        english,
                        true
                );
            }
        }


        email.append("</div>");
    }


    // RECOMMENDATION SECTION
    private void appendRecommendationSection(
            StringBuilder email,
            String title,
            List<PlaceRecommendationDTO> recommendations,
            boolean english) {

        if (
                recommendations == null ||
                        recommendations.isEmpty()
        ) {

            return;
        }


        email.append("""
            <div style="
                margin-top:22px;
                border-top:1px solid #E4ECEA;
                padding-top:18px;
            ">
            
                <div style="
                    color:#0F4C5C;
                    font-size:17px;
                    font-weight:800;
                    margin-bottom:12px;
                ">
            """);

        email.append(
                escapeHtml(title)
        );

        email.append("</div>");


        for (
                PlaceRecommendationDTO place
                : recommendations
        ) {

            appendPlace(
                    email,
                    place,
                    english,
                    false
            );
        }


        email.append("</div>");
    }


    // ONE PLACE / RECOMMENDATION
    private void appendPlace(
            StringBuilder email,
            PlaceRecommendationDTO place,
            boolean english,
            boolean showTime) {

        email.append("""
            <div style="
                padding:13px 14px;
                margin-bottom:10px;
                background:#ffffff;
                border:1px solid #EDF1F0;
                border-radius:13px;
            ">
            """);


        if (
                showTime &&
                        place.getSuggestedTime() != null
        ) {

            email.append("""
                <div style="
                    color:#F4A261;
                    font-size:12px;
                    font-weight:700;
                    margin-bottom:4px;
                ">
                """);

            email.append(
                    escapeHtml(
                            place.getSuggestedTime()
                                    .toString()
                    )
            );

            email.append("</div>");
        }


        email.append("""
            <div style="
                color:#183A40;
                font-size:15px;
                font-weight:800;
            ">
            """);

        email.append(
                escapeHtml(
                        place.getName()
                )
        );

        email.append("</div>");


        if (place.getReason() != null) {

            email.append("""
                <div style="
                    color:#68787B;
                    font-size:14px;
                    line-height:1.7;
                    margin-top:6px;
                ">
                """);

            email.append(
                    escapeHtml(
                            place.getReason()
                    )
            );

            email.append("</div>");
        }


        // HOTEL PRICE
        if (
                place.getEstimatedPrice() != null &&
                        place.getEstimatedPrice()
                                .getEstimatedPricePerNight() != null
        ) {

            email.append("""
                <div style="
                    display:inline-block;
                    background:#F8F5EF;
                    color:#36545A;
                    border-radius:20px;
                    padding:5px 10px;
                    margin-top:9px;
                    font-size:12px;
                    font-weight:700;
                ">
                """);

            email.append(
                    english
                            ? "Estimated: "
                            : "تقريبًا: "
            );

            email.append(
                    place.getEstimatedPrice()
                            .getEstimatedPricePerNight()
            );

            email.append(" ");

            if (
                    place.getEstimatedPrice()
                            .getCurrency() != null
            ) {

                email.append(
                        escapeHtml(
                                place.getEstimatedPrice()
                                        .getCurrency()
                        )
                );
            }

            email.append(
                    english
                            ? " / night"
                            : " / ليلة"
            );

            email.append("</div>");
        }


        // HALAL
        if (
                place.getHalalInfo() != null &&
                        place.getHalalInfo()
                                .getHalalStatus() != null
        ) {

            email.append("""
                <div style="
                    display:inline-block;
                    background:#EAF6F3;
                    color:#0F4C5C;
                    border-radius:20px;
                    padding:5px 10px;
                    margin-top:9px;
                    margin-left:5px;
                    margin-right:5px;
                    font-size:12px;
                    font-weight:700;
                ">
                """);

            email.append(
                    escapeHtml(
                            formatHalalStatus(
                                    place.getHalalInfo()
                                            .getHalalStatus(),
                                    english
                            )
                    )
            );

            email.append("</div>");
        }


        // WEBSITE
        String website =
                safeUrl(
                        place.getOfficialWebsite()
                );


        if (website != null) {

            email.append("""
                <div style="margin-top:12px;">
                    <a href="%s"
                       target="_blank"
                       style="
                           display:inline-block;
                           background:#2A9D8F;
                           color:#ffffff;
                           text-decoration:none;
                           padding:8px 14px;
                           border-radius:10px;
                           font-size:13px;
                           font-weight:700;
                       ">
                """.formatted(
                    escapeHtml(website)
            ));

            email.append(
                    english
                            ? "Official Website"
                            : "الموقع الرسمي"
            );

            email.append("""
                    </a>
                </div>
                """);
        }


        email.append("</div>");
    }


    // COLLECT CITIES IN ORIGINAL ORDER
    private Set<String> getItineraryCities(
            SmartItineraryDTO itinerary,
            String country) {

        Set<String> cities =
                new LinkedHashSet<>();


        if (itinerary.getDays() != null) {

            for (
                    ItineraryDayDTO day
                    : itinerary.getDays()
            ) {

                if (
                        day.getCity() != null &&
                                !day.getCity().isBlank()
                ) {

                    cities.add(
                            day.getCity()
                    );
                }
            }
        }


        addRecommendationCities(
                cities,
                itinerary.getHotelRecommendations()
        );

        addRecommendationCities(
                cities,
                itinerary.getRestaurantRecommendations()
        );

        addRecommendationCities(
                cities,
                itinerary.getActivityRecommendations()
        );


        if (cities.isEmpty()) {

            cities.add(country);
        }


        return cities;
    }


    private void addRecommendationCities(
            Set<String> cities,
            List<PlaceRecommendationDTO> recommendations) {

        if (recommendations == null) {
            return;
        }


        for (
                PlaceRecommendationDTO recommendation
                : recommendations
        ) {

            if (
                    recommendation.getCity() != null &&
                            !recommendation.getCity()
                                    .isBlank()
            ) {

                cities.add(
                        recommendation.getCity()
                );
            }
        }
    }


    // FILTER RECOMMENDATIONS BY CITY
    private List<PlaceRecommendationDTO>
    filterRecommendationsByCity(
            List<PlaceRecommendationDTO> recommendations,
            String city) {

        if (recommendations == null) {
            return List.of();
        }


        return recommendations
                .stream()
                .filter(
                        item ->
                                sameCity(
                                        item.getCity(),
                                        city
                                )
                )
                .toList();
    }


    // OLD ITINERARY FALLBACK
    private void appendUnassignedRecommendations(
            StringBuilder email,
            SmartItineraryDTO itinerary,
            boolean english,
            String direction,
            String align) {

        List<PlaceRecommendationDTO> hotels =
                getUnassigned(
                        itinerary.getHotelRecommendations()
                );

        List<PlaceRecommendationDTO> restaurants =
                getUnassigned(
                        itinerary.getRestaurantRecommendations()
                );

        List<PlaceRecommendationDTO> activities =
                getUnassigned(
                        itinerary.getActivityRecommendations()
                );


        if (
                hotels.isEmpty() &&
                        restaurants.isEmpty() &&
                        activities.isEmpty()
        ) {

            return;
        }


        email.append("""
            <tr>
                <td dir="%s"
                    style="
                        padding:20px 30px;
                        text-align:%s;
                    ">
            
                    <div style="
                        border-top:1px solid #E4ECEA;
                        padding-top:20px;
                    ">
            """.formatted(
                direction,
                align
        ));


        appendRecommendationSection(
                email,
                english
                        ? "Other Hotel Recommendations"
                        : "فنادق إضافية",
                hotels,
                english
        );

        appendRecommendationSection(
                email,
                english
                        ? "Other Restaurant Recommendations"
                        : "مطاعم إضافية",
                restaurants,
                english
        );

        appendRecommendationSection(
                email,
                english
                        ? "Other Activities"
                        : "أنشطة إضافية",
                activities,
                english
        );


        email.append("""
                    </div>
                </td>
            </tr>
            """);
    }


    private List<PlaceRecommendationDTO>
    getUnassigned(
            List<PlaceRecommendationDTO> recommendations) {

        if (recommendations == null) {
            return List.of();
        }


        return recommendations
                .stream()
                .filter(
                        item ->
                                item.getCity() == null ||
                                        item.getCity()
                                                .isBlank()
                )
                .toList();
    }


    // CITY COMPARISON
    private boolean sameCity(
            String first,
            String second) {

        if (
                first == null ||
                        second == null
        ) {

            return false;
        }


        return first.trim()
                .equalsIgnoreCase(
                        second.trim()
                );
    }


    // HALAL LABEL
    private String formatHalalStatus(
            String status,
            boolean english) {

        if (english) {

            return switch (status) {
                case "FULLY_HALAL" ->
                        "Verified halal";

                case "PARTIAL_HALAL" ->
                        "Partially verified halal";

                case "NOT_VERIFIED" ->
                        "Halal status not verified";

                default ->
                        status;
            };
        }


        return switch (status) {
            case "FULLY_HALAL" ->
                    "حلال مؤكد";

            case "PARTIAL_HALAL" ->
                    "حلال جزئي";

            case "NOT_VERIFIED" ->
                    "غير متحقق من الحلال";

            default ->
                    status;
        };
    }


    // SAFE URL
    private String safeUrl(
            String value) {

        if (
                value == null ||
                        value.isBlank()
        ) {

            return null;
        }


        String trimmed =
                value.trim();


        if (
                trimmed.startsWith("https://") ||
                        trimmed.startsWith("http://")
        ) {

            return trimmed;
        }


        return null;
    }


    // ESCAPE HTML
    private String escapeHtml(
            Object value) {

        if (value == null) {
            return "";
        }


        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#039;");
    }
}