const userId =
    localStorage.getItem("userId");

let tripId = null;
let trip = null;
let travelRequest = null;

const lang =
    localStorage.getItem("language") === "en"
        ? "en"
        : "ar";
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

        tripId =
            Number(
                params.get("tripId")
            );

        if (!tripId) {
            showMessage(
                "ما قدرنا نحدد الرحلة",
                "error"
            );

            return;
        }

        setupButtons();

        await loadPage();
    }
);


function setupButtons() {

    document
        .getElementById("generateCityPlanButton")
        .addEventListener(
            "click",
            generateCityPlan
        );


    document
        .getElementById("regenerateCityPlanButton")
        .addEventListener(
            "click",
            generateCityPlan
        );


    document
        .getElementById("acceptCityPlanButton")
        .addEventListener(
            "click",
            acceptCityPlan
        );
}


async function loadPage() {

    try {

        trip =
            await apiRequest(
                `/api/v1/trip/get-by-id/${tripId}`,
                "GET"
            );


        await loadTravelRequest();


        renderTripSummary();


        await loadExistingCityPlan();


    } catch (error) {

        showMessage(
            error.message,
            "error"
        );
    }
}


async function loadTravelRequest() {

    const requests =
        await apiRequest(
            `/api/v1/travel-request/get-by-user/${userId}`,
            "GET"
        );


    travelRequest =
        requests.find(
            request =>
                request.trip &&
                Number(request.trip.id) ===
                Number(tripId)
        );


    if (!travelRequest) {

        throw new Error(
            "ما قدرنا نحدد طلب السفر المرتبط بهذه الرحلة"
        );
    }
}


function renderTripSummary() {

    document.getElementById(
        "tripCountry"
    ).textContent =
        trip.country || "—";


    document.getElementById(
        "tripDates"
    ).textContent =
        `${formatDate(travelRequest.startDate)} - ${formatDate(travelRequest.endDate)}`;


    const modeNames = {

        multi_city_ai:
            "الذكاء يختار المدن",

        multi_city_manual:
            "أختار المدن بنفسي",

        single_city:
            "مدينة واحدة"
    };


    document.getElementById(
        "cityPlanMode"
    ).textContent =
        modeNames[
            travelRequest.cityPlanMode
            ] || "—";
}


async function loadExistingCityPlan() {

    try {

        const cities =
            await apiRequest(
                `/api/v1/trip-city/get-by-trip/${tripId}`,
                "GET"
            );


        if (
            !Array.isArray(cities) ||
            cities.length === 0
        ) {

            showEmptyState();
            return;
        }


        const orderedCities =
            [...cities].sort(
                (a, b) =>
                    a.cityOrder -
                    b.cityOrder
            );


        showResults();

        renderCities(
            orderedCities
        );


        const accepted =
            orderedCities.every(
                city =>
                    city.status === "accepted"
            );


        if (accepted) {
            showAcceptedState();
        }


    } catch (error) {

        /*
         * إذا ما فيه مدن للحين
         * نعتبرها حالة طبيعية.
         */

        showEmptyState();
    }
}


async function generateCityPlan() {

    clearMessage();

    showLoadingState();


    try {

        const result =
            await apiRequest(
                `/api/v1/trip/generate-city-plan/${tripId}?lang=${lang}`,
                "POST"
            );


        if (
            !result ||
            !Array.isArray(result.cities) ||
            result.cities.length === 0
        ) {

            throw new Error(
                "الذكاء ما رجع مدن مقترحة"
            );
        }


        const orderedCities =
            [...result.cities].sort(
                (a, b) =>
                    a.cityOrder -
                    b.cityOrder
            );


        showResults();

        renderCities(
            orderedCities
        );


    } catch (error) {

        showEmptyState();

        showMessage(
            error.message,
            "error"
        );
    }
}


function renderCities(cities) {

    const container =
        document.getElementById(
            "citiesContainer"
        );


    container.innerHTML = "";


    cities.forEach(
        (city, index) => {

            const card =
                document.createElement(
                    "article"
                );


            card.className =
                "city-card";


            card.innerHTML = `

                <div class="city-order">
                    ${city.cityOrder || index + 1}
                </div>


                <div class="city-card-content">

                    <div class="city-card-header">

                        <div>

                            <span class="city-label">
                                المحطة ${city.cityOrder || index + 1}
                            </span>

                            <h3>
                                ${escapeHtml(city.city)}
                            </h3>

                        </div>


                        <span class="material-symbols-rounded city-pin">
                            location_on
                        </span>

                    </div>


                    <div class="city-dates">

                        <span class="material-symbols-rounded">
                            calendar_month
                        </span>

                        <span>
                            ${formatDate(city.startDate)}
                            -
                            ${formatDate(city.endDate)}
                        </span>

                    </div>


                    ${
                city.reason
                    ? `
                                <div class="city-reason">

                                    <span class="material-symbols-rounded">
                                        auto_awesome
                                    </span>

                                    <p>
                                        ${escapeHtml(city.reason)}
                                    </p>

                                </div>
                              `
                    : ""
            }

                </div>
            `;


            container.appendChild(
                card
            );
        }
    );
}


async function acceptCityPlan() {

    clearMessage();


    const button =
        document.getElementById(
            "acceptCityPlanButton"
        );


    try {

        button.disabled =
            true;


        await apiRequest(
            `/api/v1/trip-city/accept-city-plan/${tripId}`,
            "POST"
        );


        showMessage(
            "تم اعتماد خطة المدن بنجاح ✨",
            "success"
        );


        setTimeout(
            () => {

                window.location.href =
                    `/trip?tripId=${tripId}`;

            },
            700
        );


    } catch (error) {

        button.disabled =
            false;


        showMessage(
            error.message,
            "error"
        );
    }
}


function showEmptyState() {

    document.getElementById(
        "emptyCityPlan"
    ).style.display =
        "flex";


    document.getElementById(
        "cityPlanLoading"
    ).style.display =
        "none";


    document.getElementById(
        "cityPlanResults"
    ).style.display =
        "none";
}


function showLoadingState() {

    document.getElementById(
        "emptyCityPlan"
    ).style.display =
        "none";


    document.getElementById(
        "cityPlanResults"
    ).style.display =
        "none";


    document.getElementById(
        "cityPlanLoading"
    ).style.display =
        "flex";
}


function showResults() {

    document.getElementById(
        "emptyCityPlan"
    ).style.display =
        "none";


    document.getElementById(
        "cityPlanLoading"
    ).style.display =
        "none";


    document.getElementById(
        "cityPlanResults"
    ).style.display =
        "block";


    document.getElementById(
        "regenerateCityPlanButton"
    ).style.display =
        "inline-flex";


    document.getElementById(
        "acceptCityPlanButton"
    ).style.display =
        "inline-flex";
}


function showAcceptedState() {

    document.getElementById(
        "regenerateCityPlanButton"
    ).style.display =
        "none";


    const acceptButton =
        document.getElementById(
            "acceptCityPlanButton"
        );


    acceptButton.disabled =
        true;


    acceptButton.innerHTML = `

        <span class="material-symbols-rounded">
            check_circle
        </span>

        الخطة معتمدة
    `;
}


function formatDate(date) {

    if (!date) {
        return "—";
    }

    return new Date(
        `${date}T00:00:00`
    ).toLocaleDateString(
        "ar-SA",
        {
            day: "numeric",
            month: "short"
        }
    );
}


function escapeHtml(value) {

    if (value === null ||
        value === undefined) {

        return "";
    }


    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
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
            "cityPlanMessage"
        );


    element.textContent =
        message;


    element.className =
        `city-plan-message ${type}`;
}


function clearMessage() {

    const element =
        document.getElementById(
            "cityPlanMessage"
        );


    element.textContent =
        "";


    element.className =
        "city-plan-message";
}