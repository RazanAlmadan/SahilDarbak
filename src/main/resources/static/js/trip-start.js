const userId =
    localStorage.getItem("userId");

let travelRequestId = null;

let travelRequest = null;


document.addEventListener(
    "DOMContentLoaded",
    async () => {

        if (!userId) {
            window.location.href = "/login";
            return;
        }

        const params =
            new URLSearchParams(
                window.location.search
            );

        travelRequestId =
            Number(
                params.get("requestId")
            );

        if (!travelRequestId) {

            showMessage(
                "ما قدرنا نحدد طلب الرحلة",
                "error"
            );

            return;
        }

        setupButtons();

        await loadTravelRequest();
    }
);


function setupButtons() {

    document
        .getElementById("showManualButton")
        .addEventListener(
            "click",
            showManualSection
        );


    document
        .getElementById("aiDestinationButton")
        .addEventListener(
            "click",
            generateAIDestinations
        );


    document
        .getElementById("createTripButton")
        .addEventListener(
            "click",
            createTrip
        );
}


async function loadTravelRequest() {

    try {

        travelRequest =
            await apiRequest(
                `/api/v1/travel-request/get-by-id/${travelRequestId}`,
                "GET"
            );

        setupPageForTravelMode();

        renderRequestSummary();

    } catch (error) {

        showMessage(
            error.message,
            "error"
        );
    }
}


function setupPageForTravelMode() {

    const cityGroup =
        document.getElementById(
            "cityGroup"
        );

    const cityLabel =
        document.getElementById(
            "cityLabel"
        );

    const cityHint =
        document.getElementById(
            "cityHint"
        );

    const manualDescription =
        document.getElementById(
            "manualDescription"
        );


    if (
        travelRequest.cityPlanMode ===
        "single_city"
    ) {

        cityGroup.style.display =
            "flex";

        cityLabel.textContent =
            "المدينة";

        cityHint.textContent =
            "بتكون هذه المدينة وجهتك الأساسية طوال الرحلة.";

        manualDescription.textContent =
            "حدد الدولة والمدينة اللي ودك تقضي فيها رحلتك.";

    } else {

        cityGroup.style.display =
            "none";

        manualDescription.textContent =
            "حدد الدولة فقط، وبعدها نرتب المدن في الخطوة التالية.";

    }
}


function renderRequestSummary() {

    const container =
        document.getElementById(
            "requestSummary"
        );


    const modeNames = {

        single_city:
            "مدينة واحدة",

        multi_city_manual:
            "عدة مدن - أختارها بنفسي",

        multi_city_ai:
            "عدة مدن - الذكاء يختارها لي"
    };


    container.innerHTML = `

        <span class="summary-chip">
            ${travelRequest.startDate}
            -
            ${travelRequest.endDate}
        </span>

        <span class="summary-chip">
            ${travelRequest.budget}
            ر.س
        </span>

        <span class="summary-chip">
            ${
        modeNames[
            travelRequest.cityPlanMode
            ] || ""
    }
        </span>

    `;
}


function showManualSection() {

    const section =
        document.getElementById(
            "manualDestinationSection"
        );

    section.classList.add(
        "show"
    );

    section.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}


async function createTrip() {

    clearMessage();


    const country =
        document
            .getElementById("country")
            .value
            .trim();


    const city =
        document
            .getElementById("city")
            .value
            .trim();


    const needsCity =
        travelRequest.cityPlanMode ===
        "single_city";


    if (!country) {

        showMessage(
            "اكتب الدولة أول",
            "error"
        );

        return;
    }


    if (
        needsCity &&
        !city
    ) {

        showMessage(
            "اكتب المدينة أول",
            "error"
        );

        return;
    }


    const button =
        document.getElementById(
            "createTripButton"
        );


    try {

        button.disabled =
            true;


        await apiRequest(
            `/api/v1/trip/add/${userId}/${travelRequestId}`,
            "POST",
            {
                country:
                country,

                city:
                    needsCity
                        ? city
                        : null,

                status:
                    "planned"
            }
        );


        const trip =
            await apiRequest(
                `/api/v1/trip/get-by-travel-request/${travelRequestId}`,
                "GET"
            );


        if (!trip?.id) {

            throw new Error(
                "تم إنشاء الرحلة لكن تعذر تحديد رقمها"
            );
        }


        localStorage.setItem(
            "tripId",
            trip.id
        );


        if (
            travelRequest.cityPlanMode ===
            "single_city"
        ) {

            window.location.href =
                `/trip?tripId=${trip.id}`;

        } else {

            window.location.href =
                `/city-plan?tripId=${trip.id}`;

        }


    } catch (error) {

        button.disabled =
            false;

        showMessage(
            error.message,
            "error"
        );
    }
}


async function apiRequest(
    url,
    method = "GET",
    body = null
) {

    const options = {
        method: method,
        headers: {}
    };


    if (body !== null) {

        options.headers[
            "Content-Type"
            ] =
            "application/json";

        options.body =
            JSON.stringify(body);
    }


    const response =
        await fetch(
            url,
            options
        );


    const text =
        await response.text();


    let data = null;


    if (text) {

        try {

            data =
                JSON.parse(text);

        } catch (_) {

            data =
                text;
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


function showMessage(
    message,
    type
) {

    const element =
        document.getElementById(
            "tripStartMessage"
        );

    element.textContent =
        message;

    element.className =
        `trip-start-message ${type}`;
}


function clearMessage() {

    const element =
        document.getElementById(
            "tripStartMessage"
        );

    element.textContent =
        "";

    element.className =
        "trip-start-message";
}

/* =========================================================
   AI DESTINATION RECOMMENDATION
   Personal feature logic
   ========================================================= */

async function generateAIDestinations() {

    clearMessage();


    const aiButton =
        document.getElementById(
            "aiDestinationButton"
        );

    const resultsSection =
        document.getElementById(
            "aiResultsSection"
        );

    const loading =
        document.getElementById(
            "aiLoading"
        );

    const resultsContainer =
        document.getElementById(
            "aiResultsContainer"
        );


    if (!travelRequestId) {

        showMessage(
            "ما قدرنا نحدد طلب الرحلة",
            "error"
        );

        return;
    }


    try {

        aiButton.disabled = true;

        aiButton.innerHTML = `
            <span class="material-symbols-rounded">
                progress_activity
            </span>

            جاري البحث...
        `;


        resultsSection.classList.add(
            "show"
        );


        loading.classList.add(
            "show"
        );


        resultsContainer.innerHTML =
            "";


        resultsSection.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });


        const response =
            await apiRequest(
                `/api/v1/ai/get/Generated/countries/${travelRequestId}`,
                "GET"
            );


        loading.classList.remove(
            "show"
        );


        renderAIDestinations(
            response
        );


    } catch (error) {

        loading.classList.remove(
            "show"
        );


        resultsContainer.innerHTML = `

            <div class="ai-results-error">

                <span class="material-symbols-rounded">
                    error
                </span>

                ما قدرنا نجيب اقتراحات الوجهات حاليًا.
                <br>

                ${escapeHtml(error.message)}

            </div>

        `;

    } finally {

        aiButton.disabled = false;

        aiButton.innerHTML = `

            <span class="material-symbols-rounded">
                auto_awesome
            </span>

            اقترح لي وجهة

        `;
    }
}


/* =========================
   RENDER AI RESULTS
   ========================= */

function renderAIDestinations(
    response
) {

    const container =
        document.getElementById(
            "aiResultsContainer"
        );


    const destinations =
        response?.destinations || [];


    const summary =
        response?.travelSummary;


    if (!destinations.length) {

        container.innerHTML = `

            <div class="ai-results-error">

                <span class="material-symbols-rounded">
                    search_off
                </span>

                ما لقينا وجهات مناسبة لطلب رحلتك حاليًا.

            </div>

        `;

        return;
    }


    const description =
        document.getElementById(
            "aiResultsDescription"
        );


    if (
        summary?.overallRecommendation
    ) {

        description.textContent =
            summary.overallRecommendation;

    }


    container.innerHTML =
        destinations
            .map(
                (
                    destination,
                    index
                ) =>
                    createDestinationCard(
                        destination,
                        index
                    )
            )
            .join("");


    container
        .querySelectorAll(
            ".ai-select-button"
        )
        .forEach(
            button => {

                button.addEventListener(
                    "click",
                    () => {

                        selectAIDestination(
                            button.dataset.country,
                            button.dataset.capital
                        );

                    }
                );

            }
        );
}


/* =========================
   DESTINATION CARD
   ========================= */

function createDestinationCard(
    destination,
    index
) {

    const country =
        destination.country ||
        "وجهة غير معروفة";


    const countryCode =
        destination.countryCode ||
        "";


    const capital =
        destination.capital ||
        "";


    const score =
        destination.suitabilityScore ??
        0;


    const recommendation =
        destination.recommendation ||
        "هذه الوجهة قد تكون مناسبة لرحلتك.";


    const weather =
        destination.weather ||
        {};


    const holidays =
        destination.holidays ||
        {};


    const crowd =
        destination.crowd ||
        {};


    const headsUp =
        destination.headsUp ||
        "";


    const whyItMatches =
        Array.isArray(
            destination.whyItMatches
        )
            ? destination.whyItMatches
            : [];


    const isTopMatch =
        index === 0 ||
        destination.rank === 1;


    const weatherText =
        weather.temperatureCelsius !==
        undefined
            ? `${weather.temperatureCelsius}°C - ${weather.condition || ""}`
            : weather.condition || "غير متوفر";


    const holidayText =
        holidays.hasHolidayDuringTrip
            ? `${holidays.holidayCount || 0} عطلة خلال الرحلة`
            : "ما فيه عطلات مؤثرة خلال الرحلة";


    const crowdText =
        crowd.level ||
        crowd.risk ||
        "غير متوفر";


    return `

        <article class="
            ai-destination-card
            ${isTopMatch ? "top-match" : ""}
        ">


            ${
        isTopMatch
            ? `
                    <span class="ai-top-match">
                        ⭐ أفضل تطابق
                    </span>
                `
            : ""
    }


            <div class="ai-country-header">

                <div class="ai-country-info">

                    <div class="ai-country-flag">

                        ${
        escapeHtml(
            countryCode
        ) || "🌍"
    }

                    </div>


                    <div>

                        <h3 class="ai-country-name">
                            ${escapeHtml(country)}
                        </h3>

                        ${
        capital
            ? `
                                <p class="ai-country-capital">
                                    ${escapeHtml(capital)}
                                </p>
                            `
            : ""
    }

                    </div>

                </div>


                <div class="ai-score">

                    <strong>
                        ${escapeHtml(String(score))}
                    </strong>

                    <span>
                        تطابق
                    </span>

                </div>

            </div>


            <div class="ai-info-list">

                <div class="ai-info-item">

                    <span class="material-symbols-rounded">
                        partly_cloudy_day
                    </span>

                    <div>

                        <strong>
                            الطقس
                        </strong>

                        <span>
                            ${escapeHtml(weatherText)}
                        </span>

                    </div>

                </div>


                <div class="ai-info-item">

                    <span class="material-symbols-rounded">
                        event
                    </span>

                    <div>

                        <strong>
                            العطلات
                        </strong>

                        <span>
                            ${escapeHtml(holidayText)}
                        </span>

                    </div>

                </div>


                <div class="ai-info-item">

                    <span class="material-symbols-rounded">
                        groups
                    </span>

                    <div>

                        <strong>
                            الازدحام
                        </strong>

                        <span>
                            ${escapeHtml(crowdText)}
                        </span>

                    </div>

                </div>

            </div>


            <p class="ai-recommendation">

                ${escapeHtml(recommendation)}

            </p>


            ${
        whyItMatches.length
            ? `
                    <p class="ai-match-title">
                        ليش تناسبك؟
                    </p>

                    <ul class="ai-match-list">

                        ${whyItMatches
                .slice(0, 3)
                .map(
                    reason =>
                        `
                                        <li>
                                            ${escapeHtml(reason)}
                                        </li>
                                    `
                )
                .join("")}

                    </ul>
                `
            : ""
    }


            ${
        headsUp
            ? `
                    <div class="ai-heads-up">

                        <span class="material-symbols-rounded">
                            warning
                        </span>

                        <span>
                            ${escapeHtml(headsUp)}
                        </span>

                    </div>
                `
            : ""
    }


            <button
                type="button"
                class="ai-select-button"
                data-country="${escapeHtml(country)}"
                data-capital="${escapeHtml(capital)}"
            >

                اختر هذه الوجهة

                <span class="material-symbols-rounded">
                    arrow_back
                </span>

            </button>

        </article>

    `;
}


/* =========================
   SELECT AI DESTINATION
   ========================= */

function selectAIDestination(
    country,
    capital
) {

    const countryInput =
        document.getElementById(
            "country"
        );


    const cityInput =
        document.getElementById(
            "city"
        );


    countryInput.value =
        country;


    if (
        travelRequest.cityPlanMode ===
        "single_city"
    ) {

        cityInput.value =
            capital || "";

    }


    showManualSection();


    showMessage(
        `تم اختيار ${country} ✨ أكمل بيانات الرحلة.`,
        "info"
    );


    document
        .getElementById(
            "manualDestinationSection"
        )
        .scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
}


/* =========================
   SAFE TEXT
   ========================= */

function escapeHtml(
    value
) {

    return String(value ?? "")
        .replace(
            /&/g,
            "&amp;"
        )
        .replace(
            /</g,
            "&lt;"
        )
        .replace(
            />/g,
            "&gt;"
        )
        .replace(
            /"/g,
            "&quot;"
        )
        .replace(
            /'/g,
            "&#039;"
        );
}