// =====================================================
// SAHIL DARBAK
// COUNTRY COMPARISON
// =====================================================

let requests = [];
let selectedTravelRequestId = null;

document.addEventListener("DOMContentLoaded", async () => {
    setupComparisonButton();
    await loadUserTravelRequests();
});

function setupComparisonButton() {
    document
        .getElementById("compareButton")
        ?.addEventListener("click", compareCountries);
}

async function loadUserTravelRequests() {
    const userId = localStorage.getItem("userId");
    const select = document.getElementById("travelRequestSelect");

    if (!userId) {
        showMessage("سجل دخولك أول عشان نقدر نستخدم تفضيلات رحلتك.", true);
        return;
    }

    try {
        requests = await apiRequest(
            `/api/v1/travel-request/get-by-user/${encodeURIComponent(userId)}`,
            "GET"
        );

        if (!Array.isArray(requests) || requests.length === 0) {
            select.innerHTML = `
                <option value="">ما عندك طلبات رحلات حتى الآن</option>
            `;
            showMessage("أنشئ طلب رحلة أولًا، وبعدها نقدر نقارن لك بين الوجهات.", true);
            return;
        }

        requests = [...requests].sort(
            (a, b) => Number(b.id || 0) - Number(a.id || 0)
        );

        select.innerHTML = requests.map((request, index) => {
            const id = request.id;
            const label = buildRequestLabel(request, index);

            return `
                <option value="${escapeHtml(id)}">
                    ${escapeHtml(label)}
                </option>
            `;
        }).join("");

        selectedTravelRequestId = Number(select.value);

        const storedRequestId =
            Number(localStorage.getItem("travelRequestId"));

        if (storedRequestId &&
            requests.some(request => Number(request.id) === storedRequestId)) {

            select.value = String(storedRequestId);
            selectedTravelRequestId = storedRequestId;
        }

        select.addEventListener("change", () => {
            selectedTravelRequestId = Number(select.value);
        });

    } catch (error) {
        select.innerHTML = `
            <option value="">تعذر تحميل رحلاتك</option>
        `;

        showMessage(
            error.message || "تعذر تحميل طلبات الرحلات.",
            true
        );
    }
}

function buildRequestLabel(request, index) {
    const date =
        request.startDate ||
        request.travelDate ||
        request.fromDate ||
        "";

    if (date) {
        return `رحلتي ${index + 1} - ${date}`;
    }

    return `رحلتي ${index + 1}`;
}

async function compareCountries() {
    clearMessage();

    const select =
        document.getElementById("travelRequestSelect");

    const country1 =
        document.getElementById("country1").value.trim();

    const country2 =
        document.getElementById("country2").value.trim();

    const button =
        document.getElementById("compareButton");

    const result =
        document.getElementById("comparisonResult");

    selectedTravelRequestId =
        Number(select.value);

    if (!selectedTravelRequestId) {
        showMessage("اختر طلب الرحلة أولًا.", true);
        return;
    }

    if (!country1 || !country2) {
        showMessage("اكتب اسم الوجهتين اللي تبي تقارن بينهم.", true);
        return;
    }

    if (country1.toLowerCase() === country2.toLowerCase()) {
        showMessage("اختر وجهتين مختلفتين للمقارنة.", true);
        return;
    }

    button.disabled = true;
    button.innerHTML = `
        <span class="material-symbols-rounded spin">
            progress_activity
        </span>
        جاري المقارنة...
    `;

    result.classList.remove("hidden");

    result.innerHTML = `
        <div class="result-heading">
            <span class="mini-label">🤖 الذكاء الاصطناعي</span>
            <h2>نقارن لك بين ${escapeHtml(country1)} و ${escapeHtml(country2)}...</h2>
        </div>
    `;

    try {
        const params = new URLSearchParams({
            travelRequestId: String(selectedTravelRequestId),
            country1,
            country2
        });

        const data = await apiRequest(
            `/api/v1/ai/compare-countries?${params.toString()}`,
            "POST"
        );

        renderComparison(data);

    } catch (error) {
        result.innerHTML = "";
        result.classList.add("hidden");

        showMessage(
            error.message || "تعذر مقارنة الوجهتين حاليًا.",
            true
        );

    } finally {
        button.disabled = false;
        button.innerHTML = `
            <span class="material-symbols-rounded">
                auto_awesome
            </span>
            قارن بين الوجهتين
        `;
    }
}

function renderComparison(data) {
    const result =
        document.getElementById("comparisonResult");

    result.innerHTML = `
        <div class="result-heading">
            <span class="mini-label">🤖 النتيجة</span>
            <h2>
                ${escapeHtml(data?.country1 || "")}
                <span> × </span>
                ${escapeHtml(data?.country2 || "")}
            </h2>
        </div>

        <div class="result-grid">

            ${comparisonItem(
                "🌤️ الأفضل للطقس",
                data?.betterForWeather
            )}

            ${comparisonItem(
                "🎯 الأفضل للأنشطة",
                data?.betterForActivities
            )}

            ${comparisonItem(
                "🌿 الأفضل للبيئة",
                data?.betterForEnvironment
            )}

            ${comparisonItem(
                "👥 الأفضل للزحمة",
                data?.betterForCrowds
            )}

            ${comparisonItem(
                "💰 الأفضل للميزانية",
                data?.betterForBudget
            )}

        </div>

        <div class="final-recommendation">
            <span>🏆 اختيارنا لك</span>

            <h3>
                ${escapeHtml(
                    data?.recommendation || "غير متوفر"
                )}
            </h3>

            <p>
                ${escapeHtml(
                    data?.reason || "ما فيه سبب متوفر حاليًا."
                )}
            </p>
        </div>
    `;

    result.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

function comparisonItem(label, value) {
    return `
        <div class="comparison-item">
            <span>${label}</span>
            <strong>
                ${escapeHtml(value || "غير متوفر")}
            </strong>
        </div>
    `;
}

async function apiRequest(url, method = "GET") {
    const response = await fetch(url, {
        method,
        headers: {
            "Accept": "application/json"
        }
    });

    const text = await response.text();

    let data = null;

    if (text) {
        try {
            data = JSON.parse(text);
        } catch (_) {
            data = text;
        }
    }

    if (!response.ok) {
        throw new Error(
            data?.message ||
            data ||
            "صار خطأ أثناء تنفيذ الطلب"
        );
    }

    return data;
}

function showMessage(message, isError = false) {
    const box =
        document.getElementById("comparePageMessage");

    box.textContent = message;
    box.classList.remove("hidden");

    if (isError) {
        box.style.display = "block";
    }
}

function clearMessage() {
    const box =
        document.getElementById("comparePageMessage");

    box.textContent = "";
    box.classList.add("hidden");
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
