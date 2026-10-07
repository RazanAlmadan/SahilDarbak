document.addEventListener("DOMContentLoaded", () => {

    const userId =
        localStorage.getItem("userId");


    if (!userId) {

        window.location.href = "/login";

        return;
    }


    setupNewTripButtons();

    loadTravelRequests(userId);

});


function setupNewTripButtons() {

    const buttons =
        document.querySelectorAll(
            "#newTripButton, [data-action='new-trip']"
        );


    buttons.forEach(button => {

        button.addEventListener("click", () => {

            localStorage.removeItem("travelRequestId");
            localStorage.removeItem("tripId");

            window.location.href =
                "/travel-request";

        });

    });

}


async function loadTravelRequests(userId) {

    const loading =
        document.getElementById("dashboardLoading");

    const errorBox =
        document.getElementById("dashboardError");

    const errorText =
        document.getElementById("dashboardErrorText");

    const emptyTrips =
        document.getElementById("emptyTrips");


    try {

        const response =
            await fetch(
                `/api/v1/travel-request/get-by-user/${userId}`
            );


        let data = null;


        try {

            data = await response.json();

        } catch (_) {

            data = null;

        }


        loading.style.display = "none";


        /*
         * لو ما عنده Travel Requests
         * سواء الباك رجع [] أو 404
         */

        const noTravelRequests =
            response.status === 404 ||
            (Array.isArray(data) && data.length === 0) ||
            data?.message
                ?.toLowerCase()
                .includes("no travel requests found");


        if (noTravelRequests) {

            loading.style.display = "none";

            errorBox.style.display = "none";

            emptyTrips.style.display = "flex";

            updateStatistics([]);

            return;
        }



        if (!response.ok) {

            throw new Error(
                data?.message ||
                "تعذر تحميل رحلاتك"
            );

        }


        const requests =
            Array.isArray(data)
                ? data
                : [];


        if (requests.length === 0) {

            emptyTrips.style.display = "flex";

            updateStatistics([]);

            return;
        }


        renderTrips(requests);

        updateStatistics(requests);


    } catch (error) {

        loading.style.display = "none";

        errorBox.style.display = "flex";

        errorText.textContent =
            error.message;

    }

}


function renderTrips(requests) {

    const grid =
        document.getElementById("tripsGrid");


    grid.innerHTML = "";


    requests.forEach(request => {

        grid.insertAdjacentHTML(
            "beforeend",
            createTripCard(request)
        );

    });


    setupTripButtons();

}


function createTripCard(request) {

    const trip =
        request.trip;


    let title;

    let statusClass;

    let statusText;

    let buttonText;

    let buttonIcon;

    let action;


    if (trip) {

        if (
            request.cityPlanMode === "multi_city_ai" ||
            request.cityPlanMode === "multi_city_manual"
        ) {

            title =
                trip.country || "رحلتي";

            statusClass =
                "status-trip";

            statusText =
                "جاهزة لترتيب المدن";

            buttonText =
                request.cityPlanMode === "multi_city_ai"
                    ? "رتب لي المدن ✨"
                    : "أضف مدن رحلتك";

            buttonIcon =
                request.cityPlanMode === "multi_city_ai"
                    ? "auto_awesome"
                    : "location_city";

            action =
                "city-plan";

        } else {

            title =
                trip.country && trip.city
                    ? `${trip.country} - ${trip.city}`
                    : trip.country || "رحلتي";

            statusClass =
                "status-trip";

            statusText =
                "قيد التخطيط";

            buttonText =
                "كمل رحلتك";

            buttonIcon =
                "route";

            action =
                "trip";
        }

    } else if (
        request.status &&
        request.status.toLowerCase() === "open"
    ) {

        title =
            `خطة سفر #${request.id}`;

        statusClass =
            "status-open";

        statusText =
            "جاهزة لاختيار الوجهة";

        buttonText =
            "بانتظار اختيار الوجهة";

        buttonIcon =
            "public";

        action =
            "waiting-destination";

    } else {

        title =
            `خطة سفر #${request.id}`;

        statusClass =
            "status-draft";

        statusText =
            "تحتاج تكملة";

        buttonText =
            "كمل تفاصيل رحلتك";

        buttonIcon =
            "edit_note";

        action =
            "request";
    }


    return `

        <article class="trip-card">


            <div class="trip-card-header">

                <div class="trip-card-icon">

                    <span class="material-symbols-rounded">
                        ${trip ? "flight_takeoff" : "travel_explore"}
                    </span>

                </div>


                <span class="trip-status ${statusClass}">
                    ${statusText}
                </span>

            </div>


            <h3>
                ${escapeHtml(title)}
            </h3>


            <div class="trip-info">


                <div class="trip-info-row">

                    <span class="material-symbols-rounded">
                        calendar_month
                    </span>

                    <span>
                        ${formatDate(request.startDate)}
                        -
                        ${formatDate(request.endDate)}
                    </span>

                </div>


                <div class="trip-info-row">

                    <span class="material-symbols-rounded">
                        payments
                    </span>

                    <span>
                        ${formatBudget(request.budget)}
                    </span>

                </div>


                <div class="trip-info-row">

                    <span class="material-symbols-rounded">
                        group
                    </span>

                    <span>
                        ${formatTravelType(request.travelType)}
                    </span>

                </div>

            </div>


            <button type="button"
                    class="btn btn-primary trip-action-button"

                    data-action="${action}"

                    data-request-id="${request.id}"

                    data-trip-id="${trip?.id || ""}">

                <span class="material-symbols-rounded">
                    ${buttonIcon}
                </span>

                ${buttonText}

            </button>


        </article>

    `;

}


function setupTripButtons() {

    const buttons =
        document.querySelectorAll(
            ".trip-action-button"
        );


    buttons.forEach(button => {

        button.addEventListener("click", () => {

            const requestId =
                button.dataset.requestId;

            const tripId =
                button.dataset.tripId;

            const action =
                button.dataset.action;


            localStorage.setItem(
                "travelRequestId",
                requestId
            );


            if (action === "request") {

                window.location.href =
                    `/travel-request?requestId=${requestId}`;

                return;

            }

            if (action === "waiting-destination") {

                window.location.href =
                    `/trip-start?requestId=${requestId}`;

                return;
            }


            if (action === "city-plan") {

                localStorage.setItem(
                    "tripId",
                    tripId
                );

                window.location.href =
                    `/city-plan?tripId=${tripId}`;

                return;
            }





            if (action === "trip") {

                localStorage.setItem(
                    "tripId",
                    tripId
                );

                window.location.href =
                    `/trip?tripId=${tripId}`;

            }

        });

    });

}


function updateStatistics(requests) {

    const total =
        document.getElementById("totalRequests");

    const drafts =
        document.getElementById("draftRequests");

    const planned =
        document.getElementById("plannedTrips");


    total.textContent =
        requests.length;


    drafts.textContent =
        requests.filter(request =>
            !request.trip &&
            (
                !request.status ||
                request.status.toLowerCase() === "draft"
            )
        ).length;


    planned.textContent =
        requests.filter(request =>
            request.trip != null
        ).length;

}


function formatDate(date) {

    if (!date) {
        return "-";
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


function formatBudget(budget) {

    if (budget == null) {
        return "-";
    }


    return `${new Intl.NumberFormat(
        "ar-SA"
    ).format(budget)} ر.س`;

}


function formatTravelType(type) {

    const types = {

        solo: "فردية",

        couple: "شخصين",

        group: "مجموعة",

        family: "عائلية"

    };


    return types[type] || type || "-";

}


function escapeHtml(value) {

    const div =
        document.createElement("div");

    div.textContent =
        value ?? "";

    return div.innerHTML;

}