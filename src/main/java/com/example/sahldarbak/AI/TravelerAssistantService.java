package com.example.sahldarbak.AI;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.TravelPresence;
import com.example.sahldarbak.Repository.TravelPresenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TravelerAssistantService {

    private final TravelPresenceRepository travelPresenceRepository;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent";

    // sends a prompt, returns the model's text answer
    public String ask(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
            );

            Map response = restTemplate.postForObject(URL, new HttpEntity<>(body, headers), Map.class);

            List<Map> candidates = (List<Map>) response.get("candidates");
            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            throw new ApiException("AI service failed: " + e.getMessage());
        }
    }

    // city guide for the city the user is currently in
    public String getCityGuide(Integer userId) {
        TravelPresence presence = travelPresenceRepository.findTravelPresenceById(userId);
        if (presence == null)
            throw new ApiException("check in to a city first");

        String prompt = """
                أنت مرشد سياحي محلي في مدينة {city} في {country}.
                مسافر موجود هناك الآن ويحتاج دليلاً قصيراً وعملياً.

                اكتب الإجابة باللغة العربية في 4 أقسام بالضبط، وبهذه العناوين بالحرف:

                نصائح محلية
                الآداب والثقافة
                السلامة
                أرقام الطوارئ

                قواعد التنسيق (مهمة جداً):
                - ضع كل عنوان في سطر مستقل.
                - اترك سطراً فارغاً بين كل قسم والذي يليه.
                - اكتب كل نقطة في سطر مستقل يبدأ بالرمز "-".
                - لا تضع أكثر من فكرة واحدة في النقطة الواحدة.
                - اجعل كل نقطة جملة قصيرة لا تتجاوز 20 كلمة.
                - لا تستخدم رموز التنسيق مثل ** أو # أو جداول.
                - لا تكتب مقدمة ولا خاتمة.

                قواعد المحتوى:
                - اكتب من 3 إلى 5 نقاط في كل قسم.
                - اجعل المعلومات خاصة بمدينة {city} وبلد {country}، لا نصائح سفر عامة.
                - لا تخترع معلومات أبداً. إذا لم تكن متأكداً من شيء فاتركه.
                - في قسم أرقام الطوارئ، اذكر فقط الأرقام الوطنية التي أنت متأكد منها (الشرطة، الإسعاف، الإطفاء)، واختم القسم بهذه الجملة:
                  "تأكد من هذه الأرقام محلياً قبل الاعتماد عليها."
                - الحد الأقصى 250 كلمة للإجابة كلها.
                """;

        return ask(prompt
                .replace("{city}", presence.getCity())
                .replace("{country}", presence.getCountry()));
    }
}