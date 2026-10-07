// =====================================================
// SAHIL DARBAK
// TRAVEL TOOLS
// =====================================================


// =====================================================
// GET TRIP ID
// =====================================================

const urlParams =
    new URLSearchParams(
        window.location.search
    );


const tripId =
    urlParams.get("tripId");



// =====================================================
// CHECK TRIP ID
// =====================================================

if (!tripId) {

    console.error(
        "No tripId was found in the URL."
    );

}



// =====================================================
// DOM READY
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        setupPackingButton();

        setupTransportationButton();

        setupTransportationEmailButton();

    }
);



// =====================================================
// PACKING BUTTON
// =====================================================

function setupPackingButton() {

    const button =
        document.getElementById(
            "generatePackingButton"
        );


    if (!button) {

        return;

    }


    button.addEventListener(
        "click",
        generatePackingList
    );

}



// =====================================================
// TRANSPORTATION BUTTON
// =====================================================

function setupTransportationButton() {

    const button =
        document.getElementById(
            "generateTransportationButton"
        );


    if (!button) {

        return;

    }


    button.addEventListener(
        "click",
        generateTransportation
    );

}



// =====================================================
// EMAIL BUTTON
// =====================================================

function setupTransportationEmailButton() {

    const button =
        document.getElementById(
            "sendTransportationEmailButton"
        );


    if (!button) {

        return;

    }


    button.addEventListener(
        "click",
        sendTransportationEmail
    );

}



// =====================================================
// PACKING LIST
// =====================================================

async function generatePackingList() {

    if (!tripId) {

        showError(
            "لم يتم العثور على رقم الرحلة."
        );

        return;

    }


    const button =
        document.getElementById(
            "generatePackingButton"
        );


    const loading =
        document.getElementById(
            "packingLoading"
        );


    const results =
        document.getElementById(
            "packingResults"
        );


    try {

        button.disabled = true;


        button.innerHTML = `
            <span class="material-symbols-rounded">
                progress_activity
            </span>

            جاري التجهيز...
        `;


        results.classList.remove(
            "show"
        );


        loading.classList.add(
            "show"
        );


        loading.scrollIntoView({
            behavior: "smooth",
            block: "center"
        });


        const response =
            await fetch(
                `/api/v1/trip/generate-packing-list/${tripId}`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json",

                        "Accept":
                            "application/json"
                    }
                }
            );


        const data =
            await readResponse(
                response
            );


        if (!response.ok) {

            throw new Error(
                data?.message ||
                "تعذر إنشاء قائمة التجهيز."
            );

        }


        renderPackingList(
            data
        );


        loading.classList.remove(
            "show"
        );


        results.classList.add(
            "show"
        );


        results.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });


    } catch (error) {

        loading.classList.remove(
            "show"
        );


        showError(
            error.message
        );


    } finally {

        button.disabled = false;


        button.innerHTML = `
            <span class="material-symbols-rounded">
                auto_awesome
            </span>

            أنشئ قائمة التجهيز
        `;

    }

}



// =====================================================
// RENDER PACKING LIST
// =====================================================

function renderPackingList(
    data
) {

    const summary =
        document.getElementById(
            "packingSummary"
        );


    const grid =
        document.getElementById(
            "packingGrid"
        );


    const tips =
        document.getElementById(
            "packingTips"
        );


    const country =
        data?.country || "";


    const city =
        data?.city || "";


    const weather =
        data?.weatherSummary || "";


    summary.innerHTML = `

        <div class="packing-summary-card">

            <div>

                <span class="material-symbols-rounded">
                    luggage
                </span>

            </div>


            <div>

                <h2>
                    🎒 قائمة تجهيز رحلتك
                </h2>


                <p>

                    ${
        city
            ? `${escapeHtml(city)}, `
            : ""
    }

                    ${escapeHtml(country)}

                </p>


                ${
        weather
            ? `
                            <span class="packing-weather">

                                <span class="material-symbols-rounded">
                                    partly_cloudy_day
                                </span>

                                ${escapeHtml(weather)}

                            </span>
                        `
            : ""
    }

            </div>

        </div>

    `;


    const categories = [

        {
            title: "👕 الملابس",
            icon: "checkroom",
            items: data?.clothing
        },

        {
            title: "👟 الأحذية",
            icon: "steps",
            items: data?.shoes
        },

        {
            title: "🌦️ مستلزمات الطقس",
            icon: "umbrella",
            items: data?.weatherEssentials
        },

        {
            title: "🏕️ مستلزمات الأنشطة",
            icon: "hiking",
            items: data?.activityEssentials
        },

        {
            title: "🧳 مستلزمات السفر",
            icon: "travel",
            items: data?.travelEssentials
        },

        {
            title: "🩺 الصحة والعناية الشخصية",
            icon: "health_and_safety",
            items: data?.healthAndPersonal
        }

    ];


    grid.innerHTML = "";


    categories.forEach(
        category => {

            const items =
                normalizeItems(
                    category.items
                );


            if (
                items.length === 0
            ) {

                return;

            }


            const card =
                document.createElement(
                    "div"
                );


            card.className =
                "packing-category";


            card.innerHTML = `

                <div class="packing-category-header">

                    <span class="material-symbols-rounded">
                        ${category.icon}
                    </span>


                    <h3>
                        ${category.title}
                    </h3>

                </div>


                <div class="packing-items">

                    ${items
                .map(
                    item => `

                                <label class="packing-item">

                                    <input
                                        type="checkbox"
                                    >

                                    <span>
                                        ${escapeHtml(item)}
                                    </span>

                                </label>

                            `
                )
                .join("")}

                </div>

            `;


            grid.appendChild(
                card
            );

        }
    );


    const tipsList =
        normalizeItems(
            data?.tips
        );


    if (
        tipsList.length > 0
    ) {

        tips.innerHTML = `

            <div class="packing-tips-card">

                <div class="packing-category-header">

                    <span class="material-symbols-rounded">
                        lightbulb
                    </span>


                    <h3>
                        💡 نصائح لرحلتك
                    </h3>

                </div>


                <ul>

                    ${tipsList
            .map(
                tip => `
                                <li>
                                    ${escapeHtml(tip)}
                                </li>
                            `
            )
            .join("")}

                </ul>

            </div>

        `;

    } else {

        tips.innerHTML = "";

    }

}



// =====================================================
// TRANSPORTATION
// =====================================================

async function generateTransportation() {

    if (!tripId) {

        showError(
            "لم يتم العثور على رقم الرحلة."
        );

        return;

    }


    const button =
        document.getElementById(
            "generateTransportationButton"
        );


    const loading =
        document.getElementById(
            "transportationLoading"
        );


    const results =
        document.getElementById(
            "transportationResults"
        );


    const emailSection =
        document.getElementById(
            "transportationEmailSection"
        );


    try {

        button.disabled = true;


        button.innerHTML = `
            <span class="material-symbols-rounded">
                progress_activity
            </span>

            جاري البحث...
        `;


        results.classList.remove(
            "show"
        );


        emailSection.classList.remove(
            "show"
        );


        loading.classList.add(
            "show"
        );


        loading.scrollIntoView({
            behavior: "smooth",
            block: "center"
        });


        const response =
            await fetch(
                `/api/v1/trip/generate-transportation/${tripId}`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json",

                        "Accept":
                            "application/json"
                    }
                }
            );


        const data =
            await readResponse(
                response
            );


        if (!response.ok) {

            throw new Error(
                data?.message ||
                "تعذر إنشاء اقتراحات التنقل."
            );

        }


        renderTransportation(
            data
        );


        loading.classList.remove(
            "show"
        );


        results.classList.add(
            "show"
        );


        /*
         * Only show the email button after
         * transportation has been generated.
         */

        emailSection.classList.add(
            "show"
        );


        results.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });


    } catch (error) {

        loading.classList.remove(
            "show"
        );


        showError(
            error.message
        );


    } finally {

        button.disabled = false;


        button.innerHTML = `
            <span class="material-symbols-rounded">
                route
            </span>

            اقترح طريقة التنقل
        `;

    }

}



// =====================================================
// RENDER TRANSPORTATION
// =====================================================

function renderTransportation(
    data
) {

    const container =
        document.getElementById(
            "transportationResultsContent"
        );


    container.innerHTML = "";


    /*
     * TransportationService responses can be
     * represented by the returned object.
     *
     * We first try the common collection names.
     */

    let suggestions = [];


    if (
        Array.isArray(data)
    ) {

        suggestions = data;

    } else if (
        Array.isArray(data?.suggestions)
    ) {

        suggestions =
            data.suggestions;

    } else if (
        Array.isArray(data?.routes)
    ) {

        suggestions =
            data.routes;

    } else if (
        Array.isArray(data?.transportation)
    ) {

        suggestions =
            data.transportation;

    } else if (
        data &&
        typeof data === "object"
    ) {

        suggestions = [
            data
        ];

    }


    if (
        suggestions.length === 0
    ) {

        container.innerHTML = `

            <div class="transportation-empty">

                <span class="material-symbols-rounded">
                    info
                </span>


                <h3>
                    لم نجد اقتراحات للتنقل
                </h3>


                <p>
                    حاول إنشاء الاقتراحات مرة أخرى.
                </p>

            </div>

        `;

        return;

    }


    suggestions.forEach(
        suggestion => {

            const method =
                suggestion?.recommendedMethod ||
                suggestion?.method ||
                "TRANSPORT";


            const from =
                suggestion?.from ||
                suggestion?.origin ||
                suggestion?.start ||
                "";


            const to =
                suggestion?.to ||
                suggestion?.destination ||
                suggestion?.end ||
                "";


            const description =
                suggestion?.description ||
                suggestion?.recommendation ||
                suggestion?.details ||
                "";


            const distance =
                suggestion?.distance ||
                "";


            const duration =
                suggestion?.duration ||
                "";


            const icon =
                getTransportationIcon(
                    method
                );


            const card =
                document.createElement(
                    "article"
                );


            card.className =
                "transportation-result-card";


            card.innerHTML = `

                <div class="transportation-result-header">

                    <div class="transportation-result-icon">

                        <span class="material-symbols-rounded">
                            ${icon}
                        </span>

                    </div>


                    <div>

                        <span class="transportation-method">

                            ${escapeHtml(
                formatTransportationMethod(
                    method
                )
            )}

                        </span>


                        ${
                from || to
                    ? `
                                    <h3>

                                        ${escapeHtml(from)}

                                        ${
                        from && to
                            ? " → "
                            : ""
                    }

                                        ${escapeHtml(to)}

                                    </h3>
                                `
                    : ""
            }

                    </div>

                </div>


                ${
                distance
                    ? `
                            <p>

                                <strong>
                                    المسافة:
                                </strong>

                                ${escapeHtml(distance)}

                            </p>
                        `
                    : ""
            }


                ${
                duration
                    ? `
                            <p>

                                <strong>
                                    الوقت:
                                </strong>

                                ${escapeHtml(duration)}

                            </p>
                        `
                    : ""
            }


                ${
                description
                    ? `
                            <p class="transportation-description">

                                ${escapeHtml(description)}

                            </p>
                        `
                    : ""
            }

            `;


            container.appendChild(
                card
            );

        }
    );

}



// =====================================================
// SEND TRANSPORTATION EMAIL
// =====================================================

async function sendTransportationEmail() {

    if (!tripId) {

        showError(
            "لم يتم العثور على رقم الرحلة."
        );

        return;

    }


    const button =
        document.getElementById(
            "sendTransportationEmailButton"
        );


    try {

        button.disabled = true;


        button.innerHTML = `
            <span class="material-symbols-rounded">
                progress_activity
            </span>

            جاري الإرسال...
        `;


        const response =
            await fetch(
                `/api/v1/trip/send-transportation-email/${tripId}`,
                {
                    method: "POST",

                    headers: {
                        "Accept":
                            "application/json"
                    }
                }
            );


        const data =
            await readResponse(
                response
            );


        if (!response.ok) {

            throw new Error(
                data?.message ||
                "تعذر إرسال اقتراحات التنقل إلى بريدك."
            );

        }


        button.innerHTML = `
            <span class="material-symbols-rounded">
                check_circle
            </span>

            تم إرسال الاقتراحات إلى بريدك ✓
        `;


    } catch (error) {

        showError(
            error.message
        );


        button.disabled = false;


        button.innerHTML = `
            <span class="material-symbols-rounded">
                mail
            </span>

            أرسل الاقتراحات إلى بريدي
        `;

    }

}



// =====================================================
// RESPONSE READER
// =====================================================

async function readResponse(
    response
) {

    const contentType =
        response.headers.get(
            "content-type"
        ) || "";


    if (
        contentType.includes(
            "application/json"
        )
    ) {

        return await response.json();

    }


    const text =
        await response.text();


    if (!text) {

        return null;

    }


    return {
        message: text
    };

}



// =====================================================
// TRANSPORTATION ICON
// =====================================================

function getTransportationIcon(
    method
) {

    const normalized =
        String(method)
            .toUpperCase();


    const icons = {

        WALK:
            "directions_walk",

        PUBLIC_TRANSPORT:
            "directions_transit",

        CAR:
            "directions_car",

        TAXI:
            "local_taxi",

        BICYCLE:
            "directions_bike"

    };


    return (
        icons[normalized] ||
        "route"
    );

}



// =====================================================
// TRANSPORTATION METHOD NAME
// =====================================================

function formatTransportationMethod(
    method
) {

    const normalized =
        String(method)
            .toUpperCase();


    const names = {

        WALK:
            "المشي",

        PUBLIC_TRANSPORT:
            "النقل العام",

        CAR:
            "السيارة",

        TAXI:
            "تاكسي",

        BICYCLE:
            "الدراجة"

    };


    return (
        names[normalized] ||
        method
    );

}



// =====================================================
// PACKING ITEMS NORMALIZER
// =====================================================

function normalizeItems(
    value
) {

    if (
        Array.isArray(value)
    ) {

        return value
            .map(
                item =>
                    String(item)
            )
            .filter(
                item =>
                    item.trim() !== ""
            );

    }


    if (
        typeof value === "string"
    ) {

        return value
            .split(/\r?\n/)
            .map(
                item =>
                    item
                        .replace(
                            /^[-•*]\s*/,
                            ""
                        )
                        .trim()
            )
            .filter(
                item =>
                    item !== ""
            );

    }


    return [];

}



// =====================================================
// ESCAPE HTML
// =====================================================

function escapeHtml(
    value
) {

    return String(value)

        .replaceAll(
            "&",
            "&amp;"
        )

        .replaceAll(
            "<",
            "&lt;"
        )

        .replaceAll(
            ">",
            "&gt;"
        )

        .replaceAll(
            '"',
            "&quot;"
        )

        .replaceAll(
            "'",
            "&#039;"
        );

}



// =====================================================
// ERROR
// =====================================================

function showError(
    message
) {

    console.error(
        message
    );


    alert(
        message ||
        "حدث خطأ. حاول مرة أخرى."
    );

}