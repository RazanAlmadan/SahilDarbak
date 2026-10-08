const userId =
    localStorage.getItem("userId");

let tripId = null;

let trip = null;

let travelRequest = null;

let tripCities = [];

let itineraryStatus = null;

let currentItinerary = null;
let currentItineraryId = null;

let tripPlaces = [];

let editingTripPlaceId = null;

let currentBudget = null;

const lang =
    localStorage.getItem("language") === "en"
        ? "en"
        : "ar";
/* =========================
   START
========================= */

document.addEventListener(
    "DOMContentLoaded",
    async () => {

        if (!userId) {

            window.location.href =
                "/login";

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


        localStorage.setItem(
            "tripId",
            tripId
        );


        setupTabs();

        setupButtons();

        setupBudgetOriginFields();


        await loadTripPage();
        const requestedTab =
            params.get("tab");

        if (
            requestedTab === "overview" ||
            requestedTab === "itinerary" ||
            requestedTab === "places" ||
            requestedTab === "budget"
        ) {

            activateTab(requestedTab);
        }


    }
);


/* =========================
   SETUP
========================= */

function setupTabs() {

    const tabs =
        document.querySelectorAll(
            ".trip-tab"
        );


    tabs.forEach(tab => {

        tab.addEventListener(
            "click",
            () => {

                activateTab(
                    tab.dataset.tab
                );
            }
        );

    });
}


function activateTab(tabName) {

    document
        .querySelectorAll(".trip-tab")
        .forEach(tab => {

            tab.classList.toggle(
                "active",
                tab.dataset.tab === tabName
            );

        });


    document
        .querySelectorAll(
            ".trip-tab-content"
        )
        .forEach(content => {

            content.classList.remove(
                "active"
            );

        });


    const target =
        document.getElementById(
            `${tabName}Tab`
        );


    if (target) {

        target.classList.add(
            "active"
        );
    }
}


function setupButtons() {

    document.getElementById(
        "goToItineraryButton"
    ).addEventListener(
        "click",
        () => activateTab("itinerary")
    );


    document.getElementById(
        "generateItineraryButton"
    ).addEventListener(
        "click",
        generateItinerary
    );


    document.getElementById(
        "regenerateItineraryButton"
    ).addEventListener(
        "click",
        generateItinerary
    );


    document.getElementById(
        "acceptItineraryButton"
    ).addEventListener(
        "click",
        acceptItinerary
    );

    const cancelItineraryButton =
        document.getElementById(
            "cancelItineraryButton"
        );

    if (cancelItineraryButton) {

        cancelItineraryButton.addEventListener(
            "click",
            cancelItinerary
        );
    }
        const goToAcceptItineraryButton =
            document.getElementById(
                "goToAcceptItineraryButton"
            );

        if (goToAcceptItineraryButton) {

            goToAcceptItineraryButton.addEventListener(
                "click",
                () => activateTab("itinerary")
            );
        }


        const openAddTripPlaceButton =
            document.getElementById(
                "openAddTripPlaceButton"
            );

        if (openAddTripPlaceButton) {

            openAddTripPlaceButton.addEventListener(
                "click",
                () => openTripPlaceForm()
            );
        }


        const showAllTripPlacesButton =
            document.getElementById(
                "showAllTripPlacesButton"
            );

        if (showAllTripPlacesButton) {

            showAllTripPlacesButton.addEventListener(
                "click",
                loadTripPlaces
            );
        }


        const showTodayTripPlacesButton =
            document.getElementById(
                "showTodayTripPlacesButton"
            );

        if (showTodayTripPlacesButton) {

            showTodayTripPlacesButton.addEventListener(
                "click",
                loadTodayTripPlaces
            );
        }


        const filterTripPlacesByDateButton =
            document.getElementById(
                "filterTripPlacesByDateButton"
            );

        if (filterTripPlacesByDateButton) {

            filterTripPlacesByDateButton.addEventListener(
                "click",
                loadTripPlacesByDate
            );
        }


        const tripPlaceForm =
            document.getElementById(
                "tripPlaceForm"
            );

        if (tripPlaceForm) {

            tripPlaceForm.addEventListener(
                "submit",
                saveTripPlace
            );
        }


        const cancelTripPlaceFormButton =
            document.getElementById(
                "cancelTripPlaceFormButton"
            );

        if (cancelTripPlaceFormButton) {

            cancelTripPlaceFormButton.addEventListener(
                "click",
                closeTripPlaceForm
            );
        }


        const tripPlacesList =
            document.getElementById(
                "tripPlacesList"
            );

        if (tripPlacesList) {

            tripPlacesList.addEventListener(
                "click",
                handleTripPlaceAction
            );
        }



    const sendItineraryEmailButton =
        document.getElementById(
            "sendItineraryEmailButton"
        );

    if (sendItineraryEmailButton) {

        sendItineraryEmailButton.addEventListener(
            "click",
            sendItineraryEmail
        );
    }


    const sendTodayWhatsAppButton =
        document.getElementById(
            "sendTodayWhatsAppButton"
        );

    if (sendTodayWhatsAppButton) {

        sendTodayWhatsAppButton.addEventListener(
            "click",
            sendTodayWhatsApp
        );
    }

    document.getElementById(
        "generateBudgetButton"
    ).addEventListener(
        "click",
        generateBudget
    );


    document.getElementById(
        "refreshBudgetButton"
    ).addEventListener(
        "click",
        refreshBudget
    );
}


/* =========================
   LOAD PAGE
========================= */

async function loadTripPage() {

    try {

        trip =
            await apiRequest(
                `/api/v1/trip/get-by-id/${tripId}`,
                "GET"
            );


        await loadTravelRequest();


        renderTripHeader();

        renderTripSummary();


        await loadTripCities();

        await loadExistingItinerary();

        await loadExistingBudget();


    } catch (error) {

        showMessage(
            error.message,
            "error"
        );
    }
}


/* =========================
   TRAVEL REQUEST
========================= */

async function loadTravelRequest() {

    const storedRequestId =
        Number(
            localStorage.getItem(
                "travelRequestId"
            )
        );


    if (storedRequestId) {

        try {

            const request =
                await apiRequest(
                    `/api/v1/travel-request/get-by-id/${storedRequestId}`,
                    "GET"
                );


            if (
                !request.trip ||
                Number(request.trip.id) ===
                Number(tripId)
            ) {

                travelRequest =
                    request;

                return;
            }

        } catch (_) {
        }
    }


    const requests =
        await apiRequest(
            `/api/v1/travel-request/get-by-user/${userId}`,
            "GET"
        );


    if (!Array.isArray(requests)) {

        throw new Error(
            "ما قدرنا نحدد طلب السفر"
        );
    }


    travelRequest =
        requests.find(
            request =>
                request.trip &&
                Number(
                    request.trip.id
                ) === Number(tripId)
        );


    if (!travelRequest) {

        throw new Error(
            "ما قدرنا نحدد طلب السفر المرتبط بهذه الرحلة"
        );
    }


    localStorage.setItem(
        "travelRequestId",
        travelRequest.id
    );
}


/* =========================
   HEADER
========================= */

function renderTripHeader() {

    const title =
        travelRequest.cityPlanMode ===
        "single_city" &&
        trip.city

            ? `${trip.country} - ${trip.city}`

            : trip.country;


    document.getElementById(
        "tripTitle"
    ).textContent =
        `رحلتك إلى ${title} ✈️`;


    const statusElement =
        document.getElementById(
            "tripStatus"
        );


    const statusNames = {

        planned:
            "قيد التخطيط",

        active:
            "رحلة نشطة",

        completed:
            "مكتملة",

        cancelled:
            "ملغاة"

    };


    statusElement.innerHTML = `

        <span class="material-symbols-rounded">
            check_circle
        </span>

        ${
        statusNames[
            trip.status
            ] ||
        "قيد التخطيط"
    }
    `;
}


/* =========================
   SUMMARY
========================= */

function renderTripSummary() {

    document.getElementById(
        "tripCountry"
    ).textContent =
        travelRequest.cityPlanMode ===
        "single_city" &&
        trip.city

            ? `${trip.country} - ${trip.city}`

            : trip.country || "—";


    document.getElementById(
        "tripDates"
    ).textContent =
        `${formatDate(
            travelRequest.startDate
        )} - ${formatDate(
            travelRequest.endDate
        )}`;


    document.getElementById(
        "tripBudget"
    ).textContent =
        travelRequest.budget != null

            ? `${formatNumber(
                travelRequest.budget
            )} ر.س`

            : "—";


    document.getElementById(
        "tripType"
    ).textContent =
        formatTravelType(
            travelRequest.travelType
        );
}


/* =========================
   CITIES
========================= */

async function loadTripCities() {

    const container =
        document.getElementById(
            "tripCities"
        );


    if (
        travelRequest.cityPlanMode ===
        "single_city"
    ) {

        container.innerHTML =
            createCityChip(
                trip.city
            );

        return;
    }


    try {

        const cities =
            await apiRequest(
                `/api/v1/trip-city/get-by-trip/${tripId}`,
                "GET"
            );


        if (!Array.isArray(cities)) {

            throw new Error();
        }


        tripCities =
            cities
                .filter(
                    city =>
                        city.status ===
                        "accepted"
                )
                .sort(
                    (a, b) =>
                        a.cityOrder -
                        b.cityOrder
                );


        if (
            tripCities.length === 0
        ) {

            container.innerHTML = `

                <div class="trip-placeholder">

                    <span class="material-symbols-rounded">
                        location_off
                    </span>

                    ما فيه مدن معتمدة للحين

                </div>
            `;

            return;
        }


        container.innerHTML =
            tripCities
                .map(
                    city =>
                        createCityChip(
                            city.city
                        )
                )
                .join("");


    } catch (_) {

        container.innerHTML = `

            <div class="trip-placeholder">

                تعذر تحميل المدن

            </div>
        `;
    }
}


function createCityChip(city) {

    return `

        <div class="trip-city-chip">

            <span class="material-symbols-rounded">
                location_on
            </span>

            ${escapeHtml(city || "—")}

        </div>
    `;
}


/* =========================
   ITINERARY LOAD
========================= */

async function loadExistingItinerary() {

    const data =
        await optionalApiRequest(
            `/api/v1/itinerary/get-by-trip/${tripId}`,
            "GET"
        );


    if (!data) {

        itineraryStatus = null;
        currentItineraryId = null;

        showItineraryEmpty();

        syncTripPlacesAccess();

        return;
    }


    itineraryStatus =
        data.status || null;

    currentItineraryId =
        data.id || null;


    currentItinerary =
        normalizeItinerary(
            data
        );


    if (!currentItinerary) {

        showItineraryEmpty();

        return;
    }


    renderItinerary(
        currentItinerary
    );


    showItineraryResult();


    if (
        itineraryStatus ===
        "accepted"
    ) {

        setItineraryAcceptedState();
    }
    syncTripPlacesAccess();

    if (
        itineraryStatus === "accepted"
    ) {

        await loadTripPlaces();
    }
}


/* =========================
   GENERATE ITINERARY
========================= */

async function generateItinerary() {

    clearMessage();

    showItineraryLoading();


    try {

        const result =
            await apiRequest(
                `/api/v1/trip/generate-smart-itinerary/${tripId}?lang=${lang}`,
                "POST"
            );


        currentItinerary =
            result;


        itineraryStatus =
            "suggested";
        currentItineraryId = null;

        syncTripPlacesAccess();


        renderItinerary(
            currentItinerary
        );


        showItineraryResult();


        resetItineraryButtons();


    } catch (error) {

        showItineraryEmpty();


        showMessage(
            error.message,
            "error"
        );
    }
}


/* =========================
   ACCEPT ITINERARY
========================= */

async function acceptItinerary() {

    clearMessage();


    const button =
        document.getElementById(
            "acceptItineraryButton"
        );


    try {

        button.disabled =
            true;


        await apiRequest(
            `/api/v1/itinerary/accept/${tripId}`,
            "POST"
        );


        itineraryStatus =
            "accepted";

        const itineraryData =
            await optionalApiRequest(
                `/api/v1/itinerary/get-by-trip/${tripId}`,
                "GET"
            );

        currentItineraryId =
            itineraryData?.id ||
            currentItineraryId;


        setItineraryAcceptedState();
        syncTripPlacesAccess();

        await loadTripPlaces();


        showMessage(
            "تم اعتماد برنامج الرحلة بنجاح ✨",
            "success"
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

/* =========================
   CANCEL ITINERARY
========================= */

async function cancelItinerary() {

    clearMessage();

    try {

        await apiRequest(
            `/api/v1/itinerary/cancel/${tripId}`,
            "DELETE"
        );

        currentItinerary = null;
        itineraryStatus = null;
        currentItineraryId = null;

        syncTripPlacesAccess();

        showItineraryEmpty();

        showMessage(
            lang === "en"
                ? "Suggested itinerary cancelled successfully"
                : "تم إلغاء برنامج الرحلة المقترح",
            "success"
        );

    } catch (error) {

        showMessage(
            error.message,
            "error"
        );
    }
}


/* =========================
   SEND ITINERARY EMAIL
========================= */

async function sendItineraryEmail() {

    clearMessage();

    const button =
        document.getElementById(
            "sendItineraryEmailButton"
        );

    try {

        if (button) {
            button.disabled = true;
        }

        await apiRequest(
            `/api/v1/itinerary/send-email/${tripId}?lang=${lang}`,
            "POST"
        );

        showMessage(
            lang === "en"
                ? "Itinerary sent to your email successfully"
                : "تم إرسال برنامج الرحلة إلى بريدك بنجاح ✨",
            "success"
        );

    } catch (error) {

        showMessage(
            error.message,
            "error"
        );

    } finally {

        if (button) {
            button.disabled = false;
        }
    }
}


/* =========================
   SEND TODAY WHATSAPP
========================= */

async function sendTodayWhatsApp() {

    clearMessage();

    const button =
        document.getElementById(
            "sendTodayWhatsAppButton"
        );

    try {

        if (button) {
            button.disabled = true;
        }

        await apiRequest(
            `/api/v1/itinerary/send-today-whatsapp/${tripId}`,
            "POST"
        );

        showMessage(
            lang === "en"
                ? "Today's plan sent to WhatsApp successfully"
                : "تم إرسال خطة اليوم إلى الواتساب بنجاح 💚",
            "success"
        );

    } catch (error) {

        showMessage(
            error.message,
            "error"
        );

    } finally {

        if (button) {
            button.disabled = false;
        }
    }
}

/* =========================
   NORMALIZE ITINERARY
========================= */
function normalizeItinerary(data) {

    if (!data) {
        return null;
    }

    // إذا رجع الـ SmartItineraryDTO مباشرة
    if (Array.isArray(data.days)) {
        return data;
    }

    // ItineraryResponseDTO
    if (data.plan) {

        if (typeof data.plan === "object") {
            return data.plan;
        }

        if (typeof data.plan === "string") {
            try {
                return JSON.parse(data.plan);
            } catch (_) {
                return null;
            }
        }
    }

    // دعم النسخ القديمة
    if (data.planJson) {
        try {
            return JSON.parse(data.planJson);
        } catch (_) {
            return null;
        }
    }

    return null;
}


/* =========================
   RENDER ITINERARY
========================= */
function renderItinerary(plan) {

    const days =
        Array.isArray(plan.days)
            ? plan.days
            : [];

    const hotels =
        Array.isArray(plan.hotelRecommendations)
            ? plan.hotelRecommendations
            : [];

    const restaurants =
        Array.isArray(plan.restaurantRecommendations)
            ? plan.restaurantRecommendations
            : [];

    const activities =
        Array.isArray(plan.activityRecommendations)
            ? plan.activityRecommendations
            : [];


    renderItineraryByCity(
        days,
        hotels,
        restaurants,
        activities
    );


    hideOldRecommendationSections();
}

function renderItineraryByCity(
    days,
    hotels,
    restaurants,
    activities
) {

    const container =
        document.getElementById(
            "itineraryDays"
        );


    if (!container) {
        return;
    }


    let cities = [];


    // SINGLE CITY
    if (
        travelRequest.cityPlanMode ===
        "single_city"
    ) {

        if (trip.city) {
            cities.push(trip.city);
        }

    }

    // MULTI CITY
    else {

        cities =
            tripCities.map(
                city =>
                    city.city
            );
    }


    // Safety:
    // إذا فيه مدينة موجودة في النتيجة
    // وما كانت موجودة في القائمة
    days.forEach(day => {

        if (
            day.city &&
            !cities.some(
                city =>
                    sameCity(
                        city,
                        day.city
                    )
            )
        ) {

            cities.push(
                day.city
            );
        }
    });


    if (cities.length === 0) {

        container.innerHTML = `

            <div class="trip-placeholder">
                ما قدرنا نحدد مدن البرنامج
            </div>
        `;

        return;
    }


    container.innerHTML =
        cities
            .map(city => {

                const cityDays =
                    days.filter(
                        day =>
                            sameCity(
                                day.city,
                                city
                            )
                    );


                const cityHotels =
                    hotels.filter(
                        item =>
                            sameCity(
                                item.city,
                                city
                            )
                    );


                const cityRestaurants =
                    restaurants.filter(
                        item =>
                            sameCity(
                                item.city,
                                city
                            )
                    );


                const cityActivities =
                    activities.filter(
                        item =>
                            sameCity(
                                item.city,
                                city
                            )
                    );


                return createCityItinerarySection(
                    city,
                    cityDays,
                    cityHotels,
                    cityRestaurants,
                    cityActivities
                );

            })
            .join("");


    // فقط لدعم itinerary قديم
    // قبل ما نضيف city للتوصيات
    const unassignedHotels =
        hotels.filter(
            item =>
                !item.city
        );

    const unassignedRestaurants =
        restaurants.filter(
            item =>
                !item.city
        );

    const unassignedActivities =
        activities.filter(
            item =>
                !item.city
        );


    if (
        cities.length > 1 &&
        (
            unassignedHotels.length ||
            unassignedRestaurants.length ||
            unassignedActivities.length
        )
    ) {

        container.insertAdjacentHTML(
            "beforeend",
            createOldRecommendationsSection(
                unassignedHotels,
                unassignedRestaurants,
                unassignedActivities
            )
        );
    }
}


function createCityItinerarySection(
    city,
    days,
    hotels,
    restaurants,
    activities
) {

    const dateRange =
        getCityDateRange(
            city,
            days
        );


    const daysHtml =
        days.length > 0

            ? days
                .map(
                    day =>
                        createItineraryDayCard(
                            day
                        )
                )
                .join("")

            : `

                <div class="trip-placeholder">
                    ما فيه أيام لهذه المدينة
                </div>
              `;


    return `

        <section class="itinerary-city-group">

            <div class="section-card-heading">

                <div>

                    <span class="section-small-label">
                        محطة من رحلتك
                    </span>

                    <h2>
                        ${escapeHtml(city)}
                    </h2>

                    ${
        dateRange
            ? `
                                <small>
                                    ${dateRange}
                                </small>
                              `
            : ""
    }

                </div>


                <span class="material-symbols-rounded heading-icon">
                    location_city
                </span>

            </div>


            <div class="city-itinerary-days">

                ${daysHtml}

            </div>


            ${createCityRecommendationGroup(
        "فنادق مقترحة",
        "hotel",
        hotels,
        "hotel"
    )}


            ${createCityRecommendationGroup(
        "مطاعم مقترحة",
        "restaurant",
        restaurants,
        "restaurant"
    )}


            ${createCityRecommendationGroup(
        "أنشطة وتجارب إضافية",
        "local_activity",
        activities,
        "activity"
    )}

        </section>
    `;
}


function createItineraryDayCard(day) {

    const places =
        Array.isArray(day.places)
            ? day.places
            : [];


    const placesHtml =
        places.length > 0

            ? places
                .map(
                    place =>
                        renderDayPlace(
                            place
                        )
                )
                .join("")

            : `

                <div class="day-activity">

                    <span class="material-symbols-rounded">
                        hotel
                    </span>

                    <p>
                        وقت مرن للراحة أو الاستكشاف.
                    </p>

                </div>
              `;


    return `

        <article class="itinerary-day">

            <div class="itinerary-day-header">

                <div class="itinerary-day-number">

                    <div class="day-number-circle">
                        ${day.dayNumber || ""}
                    </div>

                    <div>

                        <h3>
                            اليوم ${day.dayNumber || ""}
                        </h3>

                        <small>
                            ${formatDate(day.date)}
                        </small>

                    </div>

                </div>

            </div>


            <div class="day-activities">

                ${placesHtml}

            </div>

        </article>
    `;
}


function createCityRecommendationGroup(
    title,
    icon,
    items,
    type
) {

    if (
        !Array.isArray(items) ||
        items.length === 0
    ) {

        return "";
    }


    return `

        <div class="city-recommendation-group">

            <div class="section-card-heading">

                <div>

                    <h3>
                        ${title}
                    </h3>

                </div>

                <span class="material-symbols-rounded">
                    ${icon}
                </span>

            </div>


            <div class="recommendation-grid">

                ${
        items
            .map(
                item =>
                    createRecommendationCard(
                        item,
                        type
                    )
            )
            .join("")
    }

            </div>

        </div>
    `;
}


function getCityDateRange(
    city,
    days
) {

    // SINGLE CITY
    if (
        travelRequest.cityPlanMode ===
        "single_city"
    ) {

        return `${formatDate(
            travelRequest.startDate
        )} - ${formatDate(
            travelRequest.endDate
        )}`;
    }


    // MULTI CITY
    const cityData =
        tripCities.find(
            item =>
                sameCity(
                    item.city,
                    city
                )
        );


    if (cityData) {

        return `${formatDate(
            cityData.startDate
        )} - ${formatDate(
            cityData.endDate
        )}`;
    }


    if (
        Array.isArray(days) &&
        days.length > 0
    ) {

        return `${formatDate(
            days[0].date
        )} - ${formatDate(
            days[days.length - 1].date
        )}`;
    }


    return "";
}


function sameCity(
    first,
    second
) {

    if (!first || !second) {
        return false;
    }


    return String(first)
            .trim()
            .toLowerCase() ===
        String(second)
            .trim()
            .toLowerCase();
}


function hideOldRecommendationSections() {

    const hotelContainer =
        document.getElementById(
            "hotelRecommendations"
        );

    const restaurantContainer =
        document.getElementById(
            "restaurantRecommendations"
        );


    const hotelSection =
        hotelContainer
            ?.closest(
                ".trip-section-card"
            );

    const restaurantSection =
        restaurantContainer
            ?.closest(
                ".trip-section-card"
            );


    if (hotelSection) {
        hotelSection.style.display =
            "none";
    }


    if (restaurantSection) {
        restaurantSection.style.display =
            "none";
    }


    const activitySection =
        document.getElementById(
            "activityRecommendationsSection"
        );


    if (activitySection) {
        activitySection.style.display =
            "none";
    }
}


function createOldRecommendationsSection(
    hotels,
    restaurants,
    activities
) {

    return `

        <section class="itinerary-city-group">

            <div class="section-card-heading">

                <div>

                    <span class="section-small-label">
                        اقتراحات محفوظة سابقًا
                    </span>

                    <h2>
                        اقتراحات الرحلة
                    </h2>

                </div>

            </div>


            ${createCityRecommendationGroup(
        "فنادق مقترحة",
        "hotel",
        hotels,
        "hotel"
    )}


            ${createCityRecommendationGroup(
        "مطاعم مقترحة",
        "restaurant",
        restaurants,
        "restaurant"
    )}


            ${createCityRecommendationGroup(
        "أنشطة وتجارب إضافية",
        "local_activity",
        activities,
        "activity"
    )}

        </section>
    `;
}

/* =========================
   DAYS
========================= */

function renderItineraryDays(days) {

    const container =
        document.getElementById(
            "itineraryDays"
        );


    if (
        !Array.isArray(days) ||
        days.length === 0
    ) {

        container.innerHTML = `

            <div class="trip-placeholder">
                ما فيه أيام في البرنامج
            </div>
        `;

        return;
    }


    container.innerHTML =
        days.map(day => {

            const places =
                Array.isArray(
                    day.places
                )
                    ? day.places
                    : [];


            const placesHtml =
                places.length > 0

                    ? places
                        .map(
                            place =>
                                renderDayPlace(
                                    place
                                )
                        )
                        .join("")

                    : `

                        <div class="day-activity">

                            <span class="material-symbols-rounded">
                                hotel
                            </span>

                            <p>
                                وقت مرن للراحة أو الاستكشاف.
                            </p>

                        </div>
                      `;


            return `

                <article class="itinerary-day">

                    <div class="itinerary-day-header">

                        <div class="itinerary-day-number">

                            <div class="day-number-circle">
                                ${day.dayNumber || ""}
                            </div>

                            <div>

                                <h3>
                                    اليوم ${day.dayNumber || ""}
                                </h3>

                                <small>
                                    ${formatDate(day.date)}
                                </small>

                            </div>

                        </div>


                        ${
                day.city
                    ? `
                                    <span class="itinerary-city">
                                        ${escapeHtml(day.city)}
                                    </span>
                                  `
                    : ""
            }

                    </div>


                    <div class="day-activities">

                        ${placesHtml}

                    </div>

                </article>
            `;

        }).join("");
}


function renderDayPlace(place) {

    const icon =
        place.type === "restaurant"
            ? "restaurant"
            : "local_activity";


    const time =
        formatTime(
            place.suggestedTime
        );


    return `

        <div class="day-activity">

            <span class="material-symbols-rounded">
                ${icon}
            </span>


            <div>

                <strong>
                    ${escapeHtml(
        place.name || ""
    )}
                </strong>


                ${
        time
            ? `
                            <small>
                                ${time}
                            </small>
                          `
            : ""
    }


                ${
        place.reason
            ? `
                            <p>
                                ${escapeHtml(
                place.reason
            )}
                            </p>
                          `
            : ""
    }

            </div>

        </div>
    `;
}


/* =========================
   RECOMMENDATIONS
========================= */

function renderRecommendations(
    containerId,
    recommendations,
    type
) {

    const container =
        document.getElementById(
            containerId
        );


    if (!container) {
        return;
    }


    if (
        !Array.isArray(
            recommendations
        ) ||
        recommendations.length === 0
    ) {

        container.innerHTML = `

            <div class="trip-placeholder">
                ما فيه اقتراحات إضافية
            </div>
        `;

        return;
    }


    container.innerHTML =
        recommendations
            .map(
                item =>
                    createRecommendationCard(
                        item,
                        type
                    )
            )
            .join("");
}


function createRecommendationCard(
    item,
    type
) {

    const meta = [];


    if (
        Array.isArray(item.ratings) &&
        item.ratings.length > 0
    ) {

        const rating =
            item.ratings[0];


        if (
            rating.rating != null
        ) {

            meta.push(
                `⭐ ${rating.rating}${
                    rating.maxRating
                        ? `/${rating.maxRating}`
                        : ""
                }`
            );
        }
    }


    if (
        item.estimatedPrice
            ?.estimatedPricePerNight
        != null
    ) {

        meta.push(
            `تقريبًا ${
                formatNumber(
                    item.estimatedPrice
                        .estimatedPricePerNight
                )
            } ${
                item.estimatedPrice
                    .currency || ""
            } / ليلة`
        );
    }


    if (
        item.halalInfo
            ?.halalStatus
    ) {

        meta.push(
            formatHalalStatus(
                item.halalInfo
                    .halalStatus
            )
        );
    }


    const website =
        safeUrl(
            item.officialWebsite
        );


    const typeNames = {

        hotel:
            "فندق",

        restaurant:
            "مطعم",

        activity:
            "نشاط"
    };


    return `

        <article class="recommendation-card">

            <small>
                ${typeNames[type] || ""}
            </small>


            <h3>
                ${escapeHtml(
        item.name || ""
    )}
            </h3>


            ${
        item.reason
            ? `
                        <p>
                            ${escapeHtml(
                item.reason
            )}
                        </p>
                      `
            : ""
    }


            ${
        meta.length > 0
            ? `
                        <div class="recommendation-meta">

                            ${meta
                .map(
                    value =>
                        `<span>${escapeHtml(value)}</span>`
                )
                .join("")}

                        </div>
                      `
            : ""
    }


            ${
        website
            ? `
                        <a href="${website}"
                           target="_blank"
                           rel="noopener noreferrer">

                            الموقع الرسمي

                        </a>
                      `
            : ""
    }

        </article>
    `;
}


/* =========================
   ACTIVITY RECOMMENDATIONS
========================= */

function renderActivityRecommendations(
    activities
) {

    let section =
        document.getElementById(
            "activityRecommendationsSection"
        );


    if (!section) {

        section =
            document.createElement(
                "div"
            );


        section.id =
            "activityRecommendationsSection";


        section.className =
            "trip-section-card";


        section.innerHTML = `

            <div class="section-card-heading">

                <div>

                    <span class="section-small-label">
                        اقتراحات إضافية
                    </span>

                    <h2>
                        أنشطة وتجارب
                    </h2>

                </div>

                <span class="material-symbols-rounded heading-icon">
                    local_activity
                </span>

            </div>


            <div id="activityRecommendations"
                 class="recommendation-grid">
            </div>
        `;


        const actions =
            document.querySelector(
                ".itinerary-actions"
            );


        actions.parentNode.insertBefore(
            section,
            actions
        );
    }


    renderRecommendations(
        "activityRecommendations",
        activities,
        "activity"
    );
}


/* =========================
   ITINERARY STATES
========================= */

function showItineraryEmpty() {

    document.getElementById(
        "itineraryEmpty"
    ).style.display =
        "flex";


    document.getElementById(
        "itineraryLoading"
    ).style.display =
        "none";


    document.getElementById(
        "itineraryResult"
    ).style.display =
        "none";
}


function showItineraryLoading() {

    document.getElementById(
        "itineraryEmpty"
    ).style.display =
        "none";


    document.getElementById(
        "itineraryResult"
    ).style.display =
        "none";


    document.getElementById(
        "itineraryLoading"
    ).style.display =
        "flex";
}


function showItineraryResult() {

    document.getElementById(
        "itineraryEmpty"
    ).style.display =
        "none";


    document.getElementById(
        "itineraryLoading"
    ).style.display =
        "none";


    document.getElementById(
        "itineraryResult"
    ).style.display =
        "block";
}


function setItineraryAcceptedState() {

    const acceptButton =
        document.getElementById(
            "acceptItineraryButton"
        );


    const regenerateButton =
        document.getElementById(
            "regenerateItineraryButton"
        );


    acceptButton.disabled =
        true;


    acceptButton.innerHTML = `

        <span class="material-symbols-rounded">
            check_circle
        </span>

        البرنامج معتمد
    `;


    regenerateButton.style.display =
        "none";

    const cancelButton =
        document.getElementById(
            "cancelItineraryButton"
        );

    if (cancelButton) {
        cancelButton.style.display = "none";
    }
    const whatsappButton =
        document.getElementById(
            "sendTodayWhatsAppButton"
        );

    if (whatsappButton) {
        whatsappButton.style.display = "inline-flex";
    }
}


function resetItineraryButtons() {

    const acceptButton =
        document.getElementById(
            "acceptItineraryButton"
        );


    const regenerateButton =
        document.getElementById(
            "regenerateItineraryButton"
        );


    acceptButton.disabled =
        false;


    acceptButton.innerHTML = `

        <span class="material-symbols-rounded">
            check_circle
        </span>

        اعتمد البرنامج
    `;


    regenerateButton.style.display =
        "inline-flex";

    const cancelButton =
        document.getElementById(
            "cancelItineraryButton"
        );

    if (cancelButton) {
        cancelButton.style.display = "inline-flex";
    }

    const whatsappButton =
        document.getElementById(
            "sendTodayWhatsAppButton"
        );

    if (whatsappButton) {
        whatsappButton.style.display = "none";
    }
}

/* =========================
   TRIP PLACES ACCESS
========================= */

function syncTripPlacesAccess() {

    const locked =
        document.getElementById(
            "tripPlacesLocked"
        );

    const content =
        document.getElementById(
            "tripPlacesContent"
        );


    if (!locked || !content) {
        return;
    }


    const accepted =
        itineraryStatus === "accepted";


    locked.style.display =
        accepted
            ? "none"
            : "flex";


    content.style.display =
        accepted
            ? "block"
            : "none";
}


/* =========================
   LOAD ALL TRIP PLACES
========================= */

async function loadTripPlaces() {

    if (itineraryStatus !== "accepted") {
        return;
    }

    showTripPlacesLoading();

    try {

        const data =
            await apiRequest(
                `/api/v1/trip-place/get-by-trip/${tripId}`,
                "GET"
            );


        tripPlaces =
            Array.isArray(data)
                ? data
                : [];


        renderTripPlaces(
            tripPlaces
        );


    } catch (error) {

        renderTripPlaces([]);

        showMessage(
            error.message,
            "error"
        );
    }
}


/* =========================
   TODAY TRIP PLACES
========================= */

async function loadTodayTripPlaces() {

    showTripPlacesLoading();

    try {

        const data =
            await apiRequest(
                `/api/v1/trip-place/today/${tripId}`,
                "GET"
            );


        tripPlaces =
            Array.isArray(data)
                ? data
                : [];


        renderTripPlaces(
            tripPlaces
        );


    } catch (error) {

        renderTripPlaces([]);

        showMessage(
            error.message,
            "error"
        );
    }
}


/* =========================
   TRIP PLACES BY DATE
========================= */

async function loadTripPlacesByDate() {

    const date =
        document.getElementById(
            "tripPlaceFilterDate"
        )?.value;


    if (!date) {

        showMessage(
            "اختاري التاريخ أول",
            "error"
        );

        return;
    }


    showTripPlacesLoading();


    try {

        const data =
            await apiRequest(
                `/api/v1/trip-place/get-by-date/${tripId}/${date}`,
                "GET"
            );


        tripPlaces =
            Array.isArray(data)
                ? data
                : [];


        renderTripPlaces(
            tripPlaces
        );


    } catch (error) {

        renderTripPlaces([]);

        showMessage(
            error.message,
            "error"
        );
    }
}


/* =========================
   RENDER TRIP PLACES
========================= */

function renderTripPlaces(places) {

    const loading =
        document.getElementById(
            "tripPlacesLoading"
        );

    const empty =
        document.getElementById(
            "tripPlacesEmpty"
        );

    const list =
        document.getElementById(
            "tripPlacesList"
        );


    if (!loading || !empty || !list) {
        return;
    }


    loading.style.display =
        "none";


    if (
        !Array.isArray(places) ||
        places.length === 0
    ) {

        empty.style.display =
            "flex";

        list.innerHTML =
            "";

        return;
    }


    empty.style.display =
        "none";


    list.innerHTML =
        places
            .map(
                createTripPlaceCard
            )
            .join("");
}


/* =========================
   CREATE TRIP PLACE CARD
========================= */

function createTripPlaceCard(place) {

    const website =
        safeUrl(
            place.officialWebsite
        );


    return `

        <article class="trip-place-card">

            <div class="trip-place-card-main">

                <div class="trip-place-icon">

                    <span class="material-symbols-rounded">
                        ${getTripPlaceIcon(place.placeType)}
                    </span>

                </div>


                <div class="trip-place-info">

                    <small>
                        ${escapeHtml(
        formatTripPlaceType(
            place.placeType
        )
    )}
                    </small>


                    <h3>
                        ${escapeHtml(
        place.name || ""
    )}
                    </h3>


                    <div class="trip-place-meta">

                        <span>
                            <span class="material-symbols-rounded">
                                calendar_month
                            </span>

                            ${formatDate(
        place.scheduledAt
    )}
                        </span>


                        <span>
                            <span class="material-symbols-rounded">
                                location_city
                            </span>

                            ${escapeHtml(
        place.city || "—"
    )}
                        </span>

                    </div>


                    ${
        place.notes
            ? `
                                <p>
                                    ${escapeHtml(
                place.notes
            )}
                                </p>
                              `
            : ""
    }


                    ${
        website
            ? `
                                <a href="${website}"
                                   target="_blank"
                                   rel="noopener noreferrer">

                                    الموقع الرسمي

                                </a>
                              `
            : ""
    }

                </div>

            </div>


            <div class="trip-place-actions">

                <button type="button"
                        class="btn btn-light"
                        data-action="edit-trip-place"
                        data-id="${place.id}">

                    <span class="material-symbols-rounded">
                        edit
                    </span>

                    تعديل

                </button>


                <button type="button"
                        class="btn btn-light"
                        data-action="delete-trip-place"
                        data-id="${place.id}">

                    <span class="material-symbols-rounded">
                        delete
                    </span>

                    حذف

                </button>

            </div>

        </article>

    `;
}


/* =========================
   TRIP PLACE ACTIONS
========================= */

function handleTripPlaceAction(event) {

    const button =
        event.target.closest(
            "button[data-action]"
        );


    if (!button) {
        return;
    }


    const id =
        Number(
            button.dataset.id
        );


    const action =
        button.dataset.action;


    if (action === "edit-trip-place") {

        const place =
            tripPlaces.find(
                item =>
                    Number(item.id) === id
            );


        if (place) {

            openTripPlaceForm(
                place
            );
        }


        return;
    }


    if (action === "delete-trip-place") {

        deleteTripPlace(
            id
        );
    }
}


/* =========================
   OPEN ADD / EDIT FORM
========================= */

function openTripPlaceForm(
    place = null
) {

    if (
        itineraryStatus !== "accepted"
    ) {

        activateTab(
            "itinerary"
        );

        showMessage(
            "اعتمدي برنامج الرحلة أول",
            "error"
        );

        return;
    }


    const card =
        document.getElementById(
            "tripPlaceFormCard"
        );


    editingTripPlaceId =
        place?.id || null;


    document.getElementById(
        "tripPlaceFormTitle"
    ).textContent =

        editingTripPlaceId
            ? "تعديل المكان"
            : "أضف مكان جديد";


    document.getElementById(
        "tripPlaceName"
    ).value =
        place?.name || "";


    document.getElementById(
        "tripPlaceType"
    ).value =
        place?.placeType || "";


    document.getElementById(
        "tripPlaceDate"
    ).value =
        place?.scheduledAt ||
        travelRequest?.startDate ||
        "";


    document.getElementById(
        "tripPlaceCity"
    ).value =
        place?.city ||
        (
            travelRequest?.cityPlanMode ===
            "single_city"

                ? trip?.city || ""

                : ""
        );


    document.getElementById(
        "tripPlaceNotes"
    ).value =
        place?.notes || "";


    card.style.display =
        "block";


    card.scrollIntoView({
        behavior:
            "smooth",
        block:
            "start"
    });
}


/* =========================
   CLOSE FORM
========================= */

function closeTripPlaceForm() {

    editingTripPlaceId =
        null;


    const form =
        document.getElementById(
            "tripPlaceForm"
        );


    if (form) {
        form.reset();
    }


    const card =
        document.getElementById(
            "tripPlaceFormCard"
        );


    if (card) {
        card.style.display =
            "none";
    }
}


/* =========================
   SAVE TRIP PLACE
========================= */

async function saveTripPlace(event) {

    event.preventDefault();

    clearMessage();


    const name =
        document.getElementById(
            "tripPlaceName"
        ).value.trim();


    const placeType =
        document.getElementById(
            "tripPlaceType"
        ).value;


    const scheduledAt =
        document.getElementById(
            "tripPlaceDate"
        ).value;


    const city =
        document.getElementById(
            "tripPlaceCity"
        ).value.trim();


    const notes =
        document.getElementById(
            "tripPlaceNotes"
        ).value.trim();


    if (
        !name ||
        !placeType ||
        !scheduledAt ||
        !city
    ) {

        showMessage(
            "كملي بيانات المكان المطلوبة",
            "error"
        );

        return;
    }


    const body = {

        name:
        name,

        placeType:
        placeType,

        scheduledAt:
        scheduledAt,

        city:
        city,

        notes:
            notes || null
    };


    try {

        if (editingTripPlaceId) {

            await apiRequest(
                `/api/v1/trip-place/update/${editingTripPlaceId}`,
                "PUT",
                body
            );


            showMessage(
                "تم تعديل المكان بنجاح ✨",
                "success"
            );

        } else {

            await ensureCurrentItineraryId();


            await apiRequest(
                `/api/v1/trip-place/add/${currentItineraryId}`,
                "POST",
                body
            );


            showMessage(
                "تمت إضافة المكان لرحلتك ✨",
                "success"
            );
        }


        closeTripPlaceForm();

        await loadTripPlaces();


    } catch (error) {

        showMessage(
            error.message,
            "error"
        );
    }
}


/* =========================
   DELETE TRIP PLACE
========================= */

async function deleteTripPlace(id) {

    const confirmed =
        window.confirm(
            "متأكدة تبين تحذفين هذا المكان من الرحلة؟"
        );


    if (!confirmed) {
        return;
    }


    try {

        await apiRequest(
            `/api/v1/trip-place/delete/${id}`,
            "DELETE"
        );


        showMessage(
            "تم حذف المكان من الرحلة",
            "success"
        );


        await loadTripPlaces();


    } catch (error) {

        showMessage(
            error.message,
            "error"
        );
    }
}


/* =========================
   CURRENT ITINERARY ID
========================= */

async function ensureCurrentItineraryId() {

    if (currentItineraryId) {
        return;
    }


    const data =
        await apiRequest(
            `/api/v1/itinerary/get-by-trip/${tripId}`,
            "GET"
        );


    currentItineraryId =
        data?.id || null;


    if (!currentItineraryId) {

        throw new Error(
            "ما قدرنا نحدد البرنامج المعتمد"
        );
    }
}


/* =========================
   TRIP PLACE STATES
========================= */

function showTripPlacesLoading() {

    const loading =
        document.getElementById(
            "tripPlacesLoading"
        );

    const empty =
        document.getElementById(
            "tripPlacesEmpty"
        );

    const list =
        document.getElementById(
            "tripPlacesList"
        );


    if (loading) {

        loading.style.display =
            "flex";
    }


    if (empty) {

        empty.style.display =
            "none";
    }


    if (list) {

        list.innerHTML =
            "";
    }
}


/* =========================
   TRIP PLACE HELPERS
========================= */

function formatTripPlaceType(type) {

    const values = {

        activity:
            "نشاط",

        restaurant:
            "مطعم",

        cafe:
            "كافيه",

        hotel:
            "فندق",

        other:
            "مكان"
    };


    return values[type] ||
        type ||
        "مكان";
}


function getTripPlaceIcon(type) {

    const icons = {

        activity:
            "local_activity",

        restaurant:
            "restaurant",

        cafe:
            "local_cafe",

        hotel:
            "hotel",

        other:
            "location_on"
    };


    return icons[type] ||
        "location_on";
}
/* =========================
   BUDGET INPUTS
========================= */

function setupBudgetOriginFields() {

    const emptySection =
        document.getElementById(
            "budgetEmpty"
        );


    const generateButton =
        document.getElementById(
            "generateBudgetButton"
        );


    if (
        !emptySection ||
        !generateButton ||
        document.getElementById(
            "departureCountry"
        )
    ) {

        return;
    }


    const fields =
        document.createElement(
            "div"
        );


    fields.id =
        "budgetOriginFields";


    fields.style.cssText = `
        width: 100%;
        max-width: 620px;
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 12px;
        margin-bottom: 22px;
        text-align: right;
    `;


    fields.innerHTML = `

        <div class="form-group">

            <label for="departureCountry">
                دولة المغادرة
            </label>

            <input id="departureCountry"
                   type="text"
                   maxlength="50"
                   placeholder="مثال: Saudi Arabia">

        </div>


        <div class="form-group">

            <label for="departureCity">
                مدينة المغادرة
            </label>

            <input id="departureCity"
                   type="text"
                   maxlength="50"
                   placeholder="مثال: Riyadh">

        </div>


        <div class="form-group">

            <label for="budgetCurrency">
                العملة
            </label>

            <input id="budgetCurrency"
                   type="text"
                   maxlength="10"
                   value="SAR"
                   placeholder="SAR">

        </div>
    `;


    emptySection.insertBefore(
        fields,
        generateButton
    );
}


/* =========================
   LOAD BUDGET
========================= */

async function loadExistingBudget() {

    const data =
        await optionalApiRequest(
            `/api/v1/trip-budget-estimate/get-by-trip/${tripId}`,
            "GET"
        );


    if (!data) {

        showBudgetEmpty();

        return;
    }


    currentBudget =
        data;


    renderBudget(
        currentBudget
    );


    showBudgetResult();
}


/* =========================
   GENERATE BUDGET
========================= */

async function generateBudget() {

    clearMessage();


    const body =
        getBudgetRequestBody();


    if (!body) {
        return;
    }


    showBudgetLoading();


    try {

        const result =
            await apiRequest(
                `/api/v1/trip-budget-estimate/generate/${tripId}?lang=${lang}`
                ,
                "POST",
                body
            );


        currentBudget =
            result;


        if (
            !currentBudget ||
            currentBudget.totalEstimate == null
        ) {

            currentBudget =
                await apiRequest(
                    `/api/v1/trip-budget-estimate/get-by-trip/${tripId}`,
                    "GET"
                );
        }


        renderBudget(
            currentBudget
        );


        showBudgetResult();


    } catch (error) {

        showBudgetEmpty();


        showMessage(
            error.message,
            "error"
        );
    }
}


/* =========================
   REFRESH BUDGET
========================= */

async function refreshBudget() {

    clearMessage();


    const body =
        getBudgetRequestBody();


    if (!body) {
        return;
    }


    showBudgetLoading();


    try {

        const result =
            await apiRequest(
                `/api/v1/trip-budget-estimate/refresh/${tripId}?lang=${lang}`,
                "PUT",
                body
            );


        currentBudget =
            result;


        if (
            !currentBudget ||
            currentBudget.totalEstimate == null
        ) {

            currentBudget =
                await apiRequest(
                    `/api/v1/trip-budget-estimate/get-by-trip/${tripId}`,
                    "GET"
                );
        }


        renderBudget(
            currentBudget
        );


        showBudgetResult();


    } catch (error) {

        showBudgetResult();


        showMessage(
            error.message,
            "error"
        );
    }
}


/* =========================
   BUDGET BODY
========================= */

function getBudgetRequestBody() {

    const departureCountry =
        document.getElementById(
            "departureCountry"
        )?.value.trim();


    const departureCity =
        document.getElementById(
            "departureCity"
        )?.value.trim();


    const currency =
        document.getElementById(
            "budgetCurrency"
        )?.value.trim();


    if (
        !departureCountry ||
        !departureCity ||
        !currency
    ) {

        activateTab(
            "budget"
        );


        showMessage(
            "اكتب دولة المغادرة ومدينة المغادرة والعملة أول",
            "error"
        );


        return null;
    }


    return {

        departureCountry:
        departureCountry,

        departureCity:
        departureCity,

        currency:
            currency.toUpperCase()
    };
}


/* =========================
   RENDER BUDGET
========================= */

function renderBudget(data) {

    if (!data) {
        return;
    }


    const currency =
        data.currency || "SAR";


    document.getElementById(
        "budgetTotal"
    ).textContent =
        formatNumber(
            data.totalEstimate
        );


    document.getElementById(
        "budgetCurrency"
    );


    const currencyDisplay =
        document.getElementById(
            "budgetCurrencyDisplay"
        );

    if (currencyDisplay) {

        currencyDisplay.textContent =
            currency;
    }


    setMoneyValue(
        "flightEstimate",
        data.flightEstimate,
        currency
    );


    setMoneyValue(
        "accommodationEstimate",
        data.accommodationEstimate,
        currency
    );


    setMoneyValue(
        "foodEstimate",
        data.foodEstimate,
        currency
    );


    setMoneyValue(
        "transportationEstimate",
        data.transportationEstimate,
        currency
    );


    setMoneyValue(
        "activitiesEstimate",
        data.activitiesEstimate,
        currency
    );


    document.getElementById(
        "budgetSummary"
    ).textContent =
        data.summary || "";
}


function setMoneyValue(
    elementId,
    value,
    currency
) {

    document.getElementById(
        elementId
    ).textContent =
        value != null

            ? `${formatNumber(value)} ${currency}`

            : "—";
}


/* =========================
   BUDGET STATES
========================= */

function showBudgetEmpty() {

    document.getElementById(
        "budgetEmpty"
    ).style.display =
        "flex";


    document.getElementById(
        "budgetLoading"
    ).style.display =
        "none";


    document.getElementById(
        "budgetResult"
    ).style.display =
        "none";
}


function showBudgetLoading() {

    document.getElementById(
        "budgetEmpty"
    ).style.display =
        "none";


    document.getElementById(
        "budgetResult"
    ).style.display =
        "none";


    document.getElementById(
        "budgetLoading"
    ).style.display =
        "flex";
}


function showBudgetResult() {

    document.getElementById(
        "budgetEmpty"
    ).style.display =
        "none";


    document.getElementById(
        "budgetLoading"
    ).style.display =
        "none";


    document.getElementById(
        "budgetResult"
    ).style.display =
        "block";
}


/* =========================
   API
========================= */

async function apiRequest(
    url,
    method = "GET",
    body = null
) {

    const options = {

        method:
        method,

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


    let data =
        null;


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


async function optionalApiRequest(
    url,
    method = "GET"
) {

    try {

        return await apiRequest(
            url,
            method
        );

    } catch (_) {

        return null;
    }
}


/* =========================
   HELPERS
========================= */

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
            month: "short",
            year: "numeric"
        }
    );
}


function formatTime(time) {

    if (!time) {
        return "";
    }


    const parts =
        String(time).split(":");


    if (parts.length < 2) {
        return time;
    }


    return `${parts[0]}:${parts[1]}`;
}


function formatNumber(value) {

    if (
        value === null ||
        value === undefined
    ) {

        return "—";
    }


    return new Intl.NumberFormat(
        "ar-SA"
    ).format(value);
}


function formatTravelType(type) {

    const values = {

        solo:
            "فردية",

        couple:
            "شخصين",

        group:
            "مجموعة",

        family:
            "عائلية"
    };


    return values[type] ||
        type ||
        "—";
}


function formatHalalStatus(status) {

    const values = {

        FULLY_HALAL:
            "حلال مؤكد",

        PARTIAL_HALAL:
            "حلال جزئي",

        NOT_VERIFIED:
            "غير متحقق"
    };


    return values[status] ||
        status;
}


function safeUrl(value) {

    if (!value) {
        return null;
    }


    try {

        const url =
            new URL(value);


        if (
            url.protocol !== "http:" &&
            url.protocol !== "https:"
        ) {

            return null;
        }


        return url.href;


    } catch (_) {

        return null;
    }
}


function escapeHtml(value) {

    const div =
        document.createElement(
            "div"
        );


    div.textContent =
        value ?? "";


    return div.innerHTML;
}


/* =========================
   MESSAGE
========================= */

function showMessage(
    message,
    type
) {

    const element =
        document.getElementById(
            "tripMessage"
        );


    element.textContent =
        message;


    element.className =
        `trip-message ${type}`;


    element.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}


function clearMessage() {

    const element =
        document.getElementById(
            "tripMessage"
        );


    element.textContent =
        "";


    element.className =
        "trip-message";
}