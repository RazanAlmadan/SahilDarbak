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
            () => {

                showMessage(
                    "اقتراح الوجهة بالذكاء الاصطناعي سيتم ربطه قريبًا ✨",
                    "info"
                );

            }
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