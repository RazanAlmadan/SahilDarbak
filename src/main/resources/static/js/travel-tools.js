// =====================================================
// SAHIL DARBAK
// TRAVEL TOOLS
// =====================================================

let tripId = null;
let currentTrip = null;


document.addEventListener("DOMContentLoaded", async () => {

    setupButtons();

    await loadTrips();

});


// =====================================================
// SETUP
// =====================================================

function setupButtons() {

    document
        .getElementById("getWeatherButton")
        ?.addEventListener("click", getWeather);

    document
        .getElementById("getHolidaysButton")
        ?.addEventListener("click", getHolidays);

    document
        .getElementById("generatePackingButton")
        ?.addEventListener("click", generatePackingList);

    document
        .getElementById("generateTransportationButton")
        ?.addEventListener("click", generateTransportation);

}


// =====================================================
// LOAD ALL USER TRIPS
// =====================================================

async function loadTrips() {

    const tripChoices = document.getElementById("tripChoices");
    const userId = localStorage.getItem("userId");

    if (!userId) {
        showNoTripState();
        return;
    }

    try {

        tripChoices?.classList.remove("hidden");

        const travelRequests = await apiRequest(
            `/api/v1/travel-request/get-by-user/${userId}`,
            "GET"
        );

        const trips = (Array.isArray(travelRequests) ? travelRequests : [])
            .map(request => request?.trip)
            .filter(trip => trip && trip.id != null);

        const uniqueTrips = Array.from(
            new Map(trips.map(trip => [String(trip.id), trip])).values()
        );

        if (!uniqueTrips.length) {
            showNoTripState();
            return;
        }

        renderTripChoices(uniqueTrips);

        const params = new URLSearchParams(window.location.search);
        const queryTripId = params.get("tripId");
        const storedTripId = localStorage.getItem("tripId");

        const preferredTripId = queryTripId || storedTripId;

        const preferredTrip =
            uniqueTrips.find(trip => String(trip.id) === String(preferredTripId))
            || uniqueTrips[0];

        await selectTrip(preferredTrip.id);

    } catch (error) {

        console.error("Error loading trips:", error);

        if (tripChoices) {
            tripChoices.innerHTML = `
                <div class="tools-state error-state">
                    <span class="material-symbols-rounded">error</span>
                    <p>${escapeHtml(error.message || "تعذر تحميل رحلاتك.")}</p>
                </div>
            `;
        }

    }

}


// =====================================================
// RENDER TRIP CHOICES
// =====================================================

function renderTripChoices(trips) {

    const tripChoices = document.getElementById("tripChoices");

    if (!tripChoices) {
        return;
    }

    tripChoices.innerHTML = trips.map(trip => `
        <button
            type="button"
            class="trip-choice"
            data-trip-id="${escapeHtml(trip.id)}"
        >
            <strong>
                ${escapeHtml(trip.country || "رحلتك")}
            </strong>

            <span>
                ${escapeHtml(trip.city || "وجهتك")}
            </span>
        </button>
    `).join("");

    tripChoices.querySelectorAll(".trip-choice").forEach(button => {

        button.addEventListener("click", () => {
            selectTrip(button.dataset.tripId);
        });

    });

}


// =====================================================
// SELECT TRIP
// =====================================================

async function selectTrip(selectedTripId) {

    tripId = selectedTripId;

    localStorage.setItem("tripId", String(selectedTripId));

    document
        .querySelectorAll(".trip-choice")
        .forEach(button => {
            button.classList.toggle(
                "active",
                String(button.dataset.tripId) === String(selectedTripId)
            );
        });

    await loadTrip();

}


// =====================================================
// LOAD SELECTED TRIP
// =====================================================

async function loadTrip() {

    try {

        currentTrip = await apiRequest(
            `/api/v1/trip/get-by-id/${tripId}`,
            "GET"
        );

        document
            .getElementById("selectedTripHeader")
            .classList.remove("hidden");

        document
            .getElementById("toolsGrid")
            .classList.remove("hidden");

        document
            .getElementById("tripTitle")
            .textContent =
            currentTrip?.city
                ? `${currentTrip.city}، ${currentTrip.country}`
                : currentTrip?.country || "رحلتك";

        document
            .getElementById("tripDetails")
            .textContent =
            currentTrip?.status
                ? `حالة الرحلة: ${currentTrip.status}`
                : "أدوات مخصصة لرحلتك";

        localStorage.setItem(
            "tripId",
            String(tripId)
        );

    } catch (error) {

        showError(
            error.message ||
            "تعذر تحميل معلومات الرحلة."
        );

    }

}


// =====================================================
// WEATHER
// =====================================================

async function getWeather() {

    if (!tripId) {
        showError("لم يتم العثور على رقم الرحلة.");
        return;
    }

    const button =
        document.getElementById("getWeatherButton");

    const result =
        document.getElementById("weatherResult");

    setButtonLoading(
        button,
        "جاري جلب الطقس..."
    );

    result.classList.remove("hidden");

    result.innerHTML = `
        <div class="result-placeholder">
            <span class="material-symbols-rounded spin">
                progress_activity
            </span>
            جاري جلب حالة الطقس...
        </div>
    `;

    try {

        const data = await apiRequest(
            `/api/v1/trip/weather/${tripId}`,
            "GET"
        );

        const temperature =
            data?.temperatureCelsius !== undefined
                ? `${data.temperatureCelsius}°C`
                : "غير متوفر";

        const condition =
            data?.condition || "غير متوفر";

        result.innerHTML = `
            <div class="weather-result-main">

                <span class="material-symbols-rounded"
                      style="font-size:42px;color:#f4a261;">
                    partly_cloudy_day
                </span>

                <div>
                    <div class="weather-temp">
                        ${escapeHtml(temperature)}
                    </div>

                    <div class="weather-condition">
                        ${escapeHtml(condition)}
                    </div>
                </div>

            </div>
        `;

    } catch (error) {

        result.innerHTML = `
            <div class="result-placeholder error-state">
                ${escapeHtml(error.message)}
            </div>
        `;

    } finally {

        resetButton(
            button,
            "partly_cloudy_day",
            "اعرض الطقس"
        );

    }

}


// =====================================================
// HOLIDAYS
// =====================================================

async function getHolidays() {

    if (!tripId) {
        showError("لم يتم العثور على رقم الرحلة.");
        return;
    }

    const button =
        document.getElementById("getHolidaysButton");

    const result =
        document.getElementById("holidaysResult");

    setButtonLoading(
        button,
        "جاري البحث..."
    );

    result.classList.remove("hidden");

    result.innerHTML = `
        <div class="result-placeholder">
            <span class="material-symbols-rounded spin">
                progress_activity
            </span>
            جاري البحث عن العطلات...
        </div>
    `;

    try {

        const data = await apiRequest(
            `/api/v1/trip/holidays/${tripId}`,
            "GET"
        );

        const holidays =
            Array.isArray(data?.holidays)
                ? data.holidays
                : [];

        if (!holidays.length) {

            result.innerHTML = `
                <div class="holiday-summary">
                    <span class="material-symbols-rounded">
                        event_available
                    </span>

                    لا توجد عطلات رسمية خلال فترة رحلتك.
                </div>
            `;

            return;
        }

        result.innerHTML = `

            <div class="holiday-summary">

                <span class="material-symbols-rounded">
                    celebration
                </span>

                ${data.holidayCount || holidays.length}
                عطلة خلال الرحلة

            </div>


            <div class="holiday-list">

                ${holidays.map(holiday => `

                    <div class="holiday-item">

                        <strong>
                            ${escapeHtml(holiday?.name || "عطلة")}
                        </strong>

                        <small>
                            ${escapeHtml(holiday?.date || "")}
                        </small>

                    </div>

                `).join("")}

            </div>

        `;

    } catch (error) {

        result.innerHTML = `
            <div class="result-placeholder error-state">
                ${escapeHtml(error.message)}
            </div>
        `;

    } finally {

        resetButton(
            button,
            "event",
            "اعرض العطلات"
        );

    }

}


// =====================================================
// PACKING
// =====================================================

async function generatePackingList() {

    if (!tripId) {
        showError("لم يتم العثور على رقم الرحلة.");
        return;
    }

    const button =
        document.getElementById("generatePackingButton");

    const result =
        document.getElementById("packingResult");

    setButtonLoading(
        button,
        "جاري التجهيز..."
    );

    result.classList.remove("hidden");

    result.innerHTML = `
        <div class="result-placeholder">
            <span class="material-symbols-rounded spin">
                progress_activity
            </span>
            الذكاء الاصطناعي يجهز قائمتك...
        </div>
    `;

    try {

        const data = await apiRequest(
            `/api/v1/trip/generate-packing-list/${tripId}`,
            "POST"
        );

        renderPackingList(
            data,
            result
        );

    } catch (error) {

        result.innerHTML = `
            <div class="result-placeholder error-state">
                ${escapeHtml(error.message)}
            </div>
        `;

    } finally {

        resetButton(
            button,
            "auto_awesome",
            "أنشئ قائمة التجهيز"
        );

    }

}


function renderPackingList(data, container) {

    const categories = [

        {
            title: "👕 الملابس",
            items: data?.clothing
        },

        {
            title: "👟 الأحذية",
            items: data?.shoes
        },

        {
            title: "🌦️ مستلزمات الطقس",
            items: data?.weatherEssentials
        },

        {
            title: "🏕️ مستلزمات الأنشطة",
            items: data?.activityEssentials
        },

        {
            title: "🧳 مستلزمات السفر",
            items: data?.travelEssentials
        },

        {
            title: "🩺 الصحة والعناية الشخصية",
            items: data?.healthAndPersonal
        }

    ];

    const visibleCategories =
        categories
            .map(category => ({
                ...category,
                items: normalizeItems(category.items)
            }))
            .filter(category => category.items.length > 0);

    container.innerHTML = `

        ${
            data?.weatherSummary
                ? `
                    <div class="holiday-summary">
                        <span class="material-symbols-rounded">
                            partly_cloudy_day
                        </span>
                        ${escapeHtml(data.weatherSummary)}
                    </div>
                `
                : ""
        }

        <div class="packing-list">

            ${visibleCategories.map(category => `

                <div class="packing-category">

                    <h3>
                        ${category.title}
                    </h3>

                    <ul>

                        ${category.items.map(item => `
                            <li>
                                ${escapeHtml(item)}
                            </li>
                        `).join("")}

                    </ul>

                </div>

            `).join("")}

        </div>


        ${
            normalizeItems(data?.tips).length
                ? `
                    <div class="packing-category">

                        <h3>💡 نصائح لرحلتك</h3>

                        <ul>
                            ${normalizeItems(data.tips).map(tip => `
                                <li>
                                    ${escapeHtml(tip)}
                                </li>
                            `).join("")}
                        </ul>

                    </div>
                `
                : ""
        }

    `;

}


// =====================================================
// TRANSPORTATION
// =====================================================

async function generateTransportation() {

    if (!tripId) {
        showError("لم يتم العثور على رقم الرحلة.");
        return;
    }

    const button =
        document.getElementById(
            "generateTransportationButton"
        );

    const result =
        document.getElementById(
            "transportationResult"
        );

    setButtonLoading(
        button,
        "جاري البحث..."
    );

    result.classList.remove("hidden");

    result.innerHTML = `
        <div class="result-placeholder">
            <span class="material-symbols-rounded spin">
                progress_activity
            </span>
            نبحث عن أفضل طرق التنقل...
        </div>
    `;

    try {

        const data = await apiRequest(
            `/api/v1/trip/generate-transportation/${tripId}`,
            "POST"
        );

        renderTransportation(
            data,
            result
        );

    } catch (error) {

        result.innerHTML = `
            <div class="result-placeholder error-state">
                ${escapeHtml(error.message)}
            </div>
        `;

    } finally {

        resetButton(
            button,
            "route",
            "اقترح طريقة التنقل"
        );

    }

}


function renderTransportation(data, container) {

    const suggestions =
        Array.isArray(data)
            ? data
            : [];

    if (!suggestions.length) {

        container.innerHTML = `
            <div class="result-placeholder">
                <span class="material-symbols-rounded">
                    info
                </span>
                لم نجد اقتراحات للتنقل.
            </div>
        `;

        return;
    }

    container.innerHTML = `

        <div class="transport-list">

            ${suggestions.map(suggestion => {

                const method =
                    suggestion?.recommendedMethod ||
                    "TRANSPORT";

                const from =
                    suggestion?.from || "";

                const to =
                    suggestion?.to || "";

                const distance =
                    suggestion?.distanceKm !== undefined
                        ? `${suggestion.distanceKm} كم`
                        : "";

                const duration =
                    suggestion?.durationMinutes !== undefined
                        ? `${suggestion.durationMinutes} دقيقة`
                        : "";

                const reason =
                    suggestion?.reason ||
                    suggestion?.instructions ||
                    "";

                return `

                    <div class="transport-item">

                        <div>

                            <strong>
                                ${escapeHtml(from)}
                                ${
                                    from && to
                                        ? " → "
                                        : ""
                                }
                                ${escapeHtml(to)}
                            </strong>

                            ${
                                reason
                                    ? `
                                        <small>
                                            ${escapeHtml(reason)}
                                        </small>
                                    `
                                    : ""
                            }

                        </div>


                        <div class="transport-method">
                            ${escapeHtml(
                                formatTransportationMethod(method)
                            )}
                        </div>


                        ${
                            distance || duration
                                ? `
                                    <small>
                                        ${
                                            distance
                                                ? escapeHtml(distance)
                                                : ""
                                        }
                                        ${
                                            distance && duration
                                                ? " • "
                                                : ""
                                        }
                                        ${
                                            duration
                                                ? escapeHtml(duration)
                                                : ""
                                        }
                                    </small>
                                `
                                : ""
                        }

                    </div>

                `;

            }).join("")}

        </div>


        <button
                id="sendTransportationEmailButton"
                type="button"
                class="tool-action-button"
        >

            <span class="material-symbols-rounded">
                mail
            </span>

            أرسل الاقتراحات إلى بريدي

        </button>

    `;

    document
        .getElementById("sendTransportationEmailButton")
        ?.addEventListener(
            "click",
            sendTransportationEmail
        );

}


// =====================================================
// SEND TRANSPORTATION EMAIL
// =====================================================

async function sendTransportationEmail() {

    if (!tripId) {
        showError("لم يتم العثور على رقم الرحلة.");
        return;
    }

    const button =
        document.getElementById(
            "sendTransportationEmailButton"
        );

    if (!button) {
        return;
    }

    setButtonLoading(
        button,
        "جاري الإرسال..."
    );

    try {

        await apiRequest(
            `/api/v1/trip/send-transportation-email/${tripId}`,
            "POST"
        );

        button.innerHTML = `
            <span class="material-symbols-rounded">
                check_circle
            </span>
            تم إرسال الاقتراحات إلى بريدك ✓
        `;

        button.disabled = true;

    } catch (error) {

        resetButton(
            button,
            "mail",
            "أرسل الاقتراحات إلى بريدي"
        );

        showError(
            error.message ||
            "تعذر إرسال الاقتراحات إلى بريدك."
        );

    }

}


// =====================================================
// HELPERS
// =====================================================

async function apiRequest(
    url,
    method = "GET",
    body = null
) {

    const options = {
        method,
        headers: {
            "Accept": "application/json"
        }
    };

    if (body !== null) {

        options.headers["Content-Type"] =
            "application/json";

        options.body =
            JSON.stringify(body);

    }

    const response =
        await fetch(url, options);

    const text =
        await response.text();

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


function setButtonLoading(
    button,
    text
) {

    if (!button) {
        return;
    }

    button.disabled = true;

    button.innerHTML = `
        <span class="material-symbols-rounded spin">
            progress_activity
        </span>
        ${text}
    `;

}


function resetButton(
    button,
    icon,
    text
) {

    if (!button) {
        return;
    }

    button.disabled = false;

    button.innerHTML = `
        <span class="material-symbols-rounded">
            ${icon}
        </span>
        ${text}
    `;

}


function showNoTripState() {

    document
        .getElementById("toolsState")
        .classList.remove("hidden");

}


function showError(message) {

    console.error(message);

    const errorBox =
        document.getElementById("pageError");

    if (errorBox) {

        errorBox.textContent =
            message ||
            "حدث خطأ. حاول مرة أخرى.";

        errorBox.classList.remove("hidden");

        errorBox.scrollIntoView({
            behavior: "smooth",
            block: "center"
        });

        return;
    }

    alert(
        message ||
        "حدث خطأ. حاول مرة أخرى."
    );

}


function normalizeItems(value) {

    if (Array.isArray(value)) {

        return value
            .map(item => String(item))
            .filter(item => item.trim() !== "");

    }

    if (typeof value === "string") {

        return value
            .split(/\r?\n/)
            .map(item =>
                item
                    .replace(/^[-•*]\s*/, "")
                    .trim()
            )
            .filter(item => item !== "");

    }

    return [];

}


function formatTransportationMethod(method) {

    const normalized =
        String(method)
            .toUpperCase();

    const names = {

        WALK: "المشي",
        PUBLIC_TRANSPORT: "النقل العام",
        CAR: "السيارة",
        TAXI: "تاكسي",
        BICYCLE: "الدراجة"

    };

    return (
        names[normalized] ||
        method
    );

}


function escapeHtml(value) {

    return String(value ?? "")

        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");

}
