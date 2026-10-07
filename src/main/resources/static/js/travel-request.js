let currentStep = 1;

let travelRequestId = null;

let currentRequest = null;


const userId =
    localStorage.getItem("userId");


const foodOptions = [
    ["halal", "حلال"],
    ["local_food", "أكل محلي"],
    ["vegetarian", "نباتي"],
    ["seafood", "مأكولات بحرية"],
    ["other", "أخرى"]
];


const activityOptions = [
    ["museum", "متاحف"],
    ["hiking", "تسلق وطبيعة"],
    ["shopping", "تسوق"],
    ["beach", "بحر"],
    ["culture", "ثقافة وتاريخ"],
    ["entertainment", "ترفيه"],
    ["food", "تجارب أكل"],
    ["other", "أخرى"]
];


document.addEventListener(
    "DOMContentLoaded",
    async () => {

        if (!userId) {

            window.location.href =
                "/login";

            return;
        }


        initializeDates();

        renderFoodOptions();

        renderActivityOptions();

        setupTravelType();

        setupButtons();


        const params =
            new URLSearchParams(
                window.location.search
            );


        const requestId =
            params.get("requestId");


        if (requestId) {

            travelRequestId =
                Number(requestId);

            localStorage.setItem(
                "travelRequestId",
                travelRequestId
            );


            await loadExistingRequest(
                travelRequestId
            );

        }


        updateWizard();

    }
);



/* =============================
   SETUP
============================= */

function initializeDates() {

    const today =
        new Date()
            .toISOString()
            .split("T")[0];


    document.getElementById(
        "startDate"
    ).min = today;


    document.getElementById(
        "endDate"
    ).min = today;


    document.getElementById(
        "startDate"
    ).addEventListener(
        "change",
        event => {

            document.getElementById(
                "endDate"
            ).min =
                event.target.value;

        }
    );

}


function setupTravelType() {

    document
        .querySelectorAll(
            "input[name='travelType']"
        )
        .forEach(input => {

            input.addEventListener(
                "change",
                updateTravelTypeFields
            );

        });

}


function updateTravelTypeFields() {

    const type =
        getCheckedValue("travelType");


    const groupContainer =
        document.getElementById(
            "groupSizeContainer"
        );


    const familyContainer =
        document.getElementById(
            "familyDetailsContainer"
        );


    groupContainer.style.display =
        type === "group"
            ? "block"
            : "none";


    familyContainer.style.display =
        type === "family"
            ? "block"
            : "none";


    if (type === "family") {

        renderFamilyChildrenAges();

    }

}





/* =============================
   FOOD
============================= */
function renderFoodOptions() {

    const container =
        document.getElementById(
            "foodPreferences"
        );


    container.innerHTML =
        foodOptions
            .map(([value, label]) => `

                <div class="preference-item"
                     data-food="${value}">

                    <div class="preference-content">

                        <label class="preference-main">

                            <input type="checkbox"
                                   class="food-checkbox"
                                   value="${value}">

                            <strong>
                                ${label}
                            </strong>

                        </label>


                        ${
                value === "other"
                    ? `
                                    <input type="text"
                                           class="other-food-input"
                                           placeholder="اكتب تفضيلك..."
                                           style="display:none;">
                                  `
                    : ""
            }

                    </div>


                    <label class="required-label">

                        <input type="checkbox"
                               class="food-required">

                        شرط أساسي

                    </label>

                </div>

            `)
            .join("");


    const otherItem =
        document.querySelector(
            '[data-food="other"]'
        );


    const otherCheckbox =
        otherItem?.querySelector(
            ".food-checkbox"
        );


    const otherInput =
        otherItem?.querySelector(
            ".other-food-input"
        );


    if (otherCheckbox && otherInput) {

        otherCheckbox.addEventListener(
            "change",
            () => {

                otherInput.style.display =
                    otherCheckbox.checked
                        ? "block"
                        : "none";


                if (otherCheckbox.checked) {

                    otherInput.focus();

                } else {

                    otherInput.value = "";

                }

            }
        );

    }

}

/* =============================
   ACTIVITIES
============================= */

function renderActivityOptions() {

    const container =
        document.getElementById(
            "activityPreferences"
        );


    container.innerHTML =
        activityOptions
            .map(([value, label]) => `

                <div class="preference-item"
                     data-activity="${value}">

                    <label class="preference-main">

                        <input type="checkbox"
                               class="activity-checkbox"
                               value="${value}">

                        <strong>
                            ${label}
                        </strong>

                    </label>


                    <div class="preference-options">

                        <span>
                            الأهمية
                        </span>

                        <select class="priority-select">

                            <option value="1">
                                1
                            </option>

                            <option value="2">
                                2
                            </option>

                            <option value="3"
                                    selected>
                                3
                            </option>

                            <option value="4">
                                4
                            </option>

                            <option value="5">
                                5
                            </option>

                        </select>

                    </div>

                </div>

            `)
            .join("");

}



/* =============================
   RESTRICTIONS
============================= */

function addRestrictionRow(
    restriction = null
) {

    const container =
        document.getElementById(
            "restrictionRows"
        );


    const row =
        document.createElement("div");


    row.className =
        "restriction-row";


    row.innerHTML = `

        <select class="restriction-type">

            <option value="allergy">
                حساسية
            </option>

            <option value="accessibility">
                سهولة الوصول
            </option>

            <option value="dietary">
                قيد غذائي
            </option>

            <option value="medical">
                احتياج صحي
            </option>

            <option value="other">
                أخرى
            </option>

        </select>


        <input type="text"
               class="restriction-description"
               placeholder="اكتب التفاصيل">


        <label class="required-label">

            <input type="checkbox"
                   class="restriction-required">

            شرط أساسي

        </label>


        <button type="button"
                class="remove-row-button">

            <span class="material-symbols-rounded">
                delete
            </span>

        </button>

    `;


    if (restriction) {

        row.querySelector(
            ".restriction-type"
        ).value =
            restriction.restrictionType;


        row.querySelector(
            ".restriction-description"
        ).value =
            restriction.description;


        row.querySelector(
            ".restriction-required"
        ).checked =
            restriction.isRequired === true;

    }


    row.querySelector(
        ".remove-row-button"
    ).addEventListener(
        "click",
        () => row.remove()
    );


    container.appendChild(row);

}




/* =============================
   BUTTONS
============================= */

function setupButtons() {

    document.getElementById(
        "nextButton"
    ).addEventListener(
        "click",
        nextStep
    );


    document.getElementById(
        "previousButton"
    ).addEventListener(
        "click",
        previousStep
    );


    document.getElementById(
        "addRestrictionButton"
    ).addEventListener(
        "click",
        () => addRestrictionRow()
    );





    document.getElementById(
        "submitRequestButton"
    ).addEventListener(
        "click",
        submitTravelRequest
    );

}



/* =============================
   NEXT
============================= */

async function nextStep() {

    clearMessage();


    try {

        await saveCurrentStep();


        currentStep++;


        if (currentStep === 6) {

            renderReview();

        }


        updateWizard();


        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });


    } catch (error) {

        showMessage(
            error.message,
            "error"
        );

    }

}



/* =============================
   PREVIOUS
============================= */

function previousStep() {

    clearMessage();


    currentStep--;


    if (currentStep < 1) {
        currentStep = 1;
    }


    updateWizard();

}



/* =============================
   SAVE STEP
============================= */

async function saveCurrentStep() {

    switch (currentStep) {

        case 1:
            await saveMainRequest();
            break;

        case 2:
            await saveGeneralPreference();
            break;

        case 3:
            await saveFoodPreferences();
            break;

        case 4:
            await saveActivityPreferences();
            break;

        case 5:
            await saveRestrictions();
            break;


    }

}



/* =============================
   MAIN REQUEST
============================= */

async function saveMainRequest() {

    const startDate =
        document.getElementById(
            "startDate"
        ).value;


    const endDate =
        document.getElementById(
            "endDate"
        ).value;


    const budget =
        Number(
            document.getElementById(
                "budget"
            ).value
        );


    const travelType =
        getCheckedValue(
            "travelType"
        );


    const cityPlanMode =
        getCheckedValue(
            "cityPlanMode"
        );


    if (
        !startDate ||
        !endDate ||
        !budget ||
        !travelType ||
        !cityPlanMode
    ) {

        throw new Error(
            "كمل بيانات الرحلة أول"
        );

    }


    if (
        new Date(endDate) <=
        new Date(startDate)
    ) {

        throw new Error(
            "تاريخ نهاية الرحلة لازم يكون بعد تاريخ البداية"
        );

    }


    const body = {

        startDate:
        startDate,

        endDate:
        endDate,

        budget:
        budget,

        travelType:
        travelType,

        cityPlanMode:
        cityPlanMode,

        groupSize:
            travelType === "group"
                ? Number(
                    document.getElementById(
                        "groupSize"
                    ).value
                )
                : null,

        adultsCount:
            travelType === "family"
                ? Number(
                    document.getElementById(
                        "adultsCount"
                    ).value
                )
                : null

    };


    if (
        travelType === "group" &&
        !body.groupSize
    ) {

        throw new Error(
            "حدد عدد أفراد المجموعة"
        );

    }


    if (travelType === "family") {

        if (!body.adultsCount) {

            throw new Error(
                "حدد عدد البالغين"
            );
        }


        const children =
            [...document.querySelectorAll(
                ".family-child-age"
            )];


        if (children.length === 0) {

            throw new Error(
                "أضف طفل واحد على الأقل للرحلة العائلية"
            );
        }


        for (const input of children) {

            const age =
                Number(input.value);


            if (
                input.value === "" ||
                age < 0 ||
                age > 17
            ) {

                throw new Error(
                    "تأكد من إدخال أعمار الأطفال من 0 إلى 17"
                );
            }

        }

    }

    if (!travelRequestId) {

        await apiRequest(
            `/api/v1/travel-request/add/${userId}`,
            "POST",
            body
        );


        /*
         * الـadd عندنا يرجع ApiResponse فقط،
         * لذلك نجيب طلبات المستخدم ونأخذ أحدث ID.
         */
        const requests =
            await apiRequest(
                `/api/v1/travel-request/get-by-user/${userId}`,
                "GET"
            );


        if (
            !Array.isArray(requests) ||
            requests.length === 0
        ) {

            throw new Error(
                "تم إنشاء الطلب لكن تعذر تحديد رقمه"
            );

        }


        const newest =
            [...requests]
                .sort(
                    (a, b) =>
                        b.id - a.id
                )[0];


        travelRequestId =
            newest.id;


        localStorage.setItem(
            "travelRequestId",
            travelRequestId
        );


        history.replaceState(
            {},
            "",
            `/travel-request?requestId=${travelRequestId}`
        );


    } else {

        await apiRequest(
            `/api/v1/travel-request/update/${travelRequestId}`,
            "PUT",
            body
        );

    }


    await refreshCurrentRequest();

    await saveFamilyChildren();

}



/* =============================
   GENERAL PREFERENCE
============================= */

async function saveGeneralPreference() {

    ensureRequestExists();


    const weather =
        getCheckedValue("weather");

    const environment =
        getCheckedValue("environment");

    const crowdPreference =
        getCheckedValue(
            "crowdPreference"
        );

    const tripPace =
        getCheckedValue("tripPace");


    if (
        !weather ||
        !environment ||
        !crowdPreference ||
        !tripPace
    ) {

        throw new Error(
            "جاوب على جميع تفضيلات الرحلة"
        );

    }


    const body = {

        weather:
        weather,

        environment:
        environment,

        crowdPreference:
        crowdPreference,

        tripPace:
        tripPace

    };


    const existing =
        currentRequest
            ?.generalPreference;


    if (existing?.id) {

        await apiRequest(
            `/api/v1/general-preference/update/${existing.id}`,
            "PUT",
            body
        );

    } else {

        await apiRequest(
            `/api/v1/general-preference/add/${travelRequestId}`,
            "POST",
            body
        );

    }


    await refreshCurrentRequest();

}



/* =============================
   FOOD
============================= */

async function saveFoodPreferences() {

    ensureRequestExists();


    const old =
        currentRequest
            ?.foodPreferences || [];


    for (const preference of old) {

        await apiRequest(
            `/api/v1/food-preference/delete/${preference.id}`,
            "DELETE"
        );

    }


    const selected =
        document.querySelectorAll(
            ".food-checkbox:checked"
        );


    for (const checkbox of selected) {

        const item =
            checkbox.closest(
                ".preference-item"
            );


        let foodType =
            checkbox.value;


        if (foodType === "other") {

            const customValue =
                item.querySelector(
                    ".other-food-input"
                ).value.trim();


            if (!customValue) {

                throw new Error(
                    "اكتب تفضيل الأكل في خيار أخرى"
                );

            }


            foodType =
                customValue;

        }


        await apiRequest(
            `/api/v1/food-preference/add/${travelRequestId}`,
            "POST",
            {
                foodType:
                foodType,

                isRequired:
                item.querySelector(
                    ".food-required"
                ).checked
            }
        );

    }


    await refreshCurrentRequest();

}



/* =============================
   ACTIVITIES
============================= */

async function saveActivityPreferences() {

    ensureRequestExists();


    const old =
        currentRequest
            ?.activityPreferences || [];


    for (const preference of old) {

        await apiRequest(
            `/api/v1/activity-preference/delete/${preference.id}`,
            "DELETE"
        );

    }


    const selected =
        document.querySelectorAll(
            ".activity-checkbox:checked"
        );


    for (const checkbox of selected) {

        const item =
            checkbox.closest(
                ".preference-item"
            );


        await apiRequest(
            `/api/v1/activity-preference/add/${travelRequestId}`,
            "POST",
            {
                activityType:
                checkbox.value,

                priority:
                    Number(
                        item.querySelector(
                            ".priority-select"
                        ).value
                    )
            }
        );

    }


    await refreshCurrentRequest();

}



/* =============================
   RESTRICTIONS
============================= */

async function saveRestrictions() {

    ensureRequestExists();


    const old =
        currentRequest
            ?.travelRestrictions || [];


    for (const restriction of old) {

        await apiRequest(
            `/api/v1/travel-restriction/delete/${restriction.id}`,
            "DELETE"
        );

    }


    const rows =
        document.querySelectorAll(
            ".restriction-row"
        );


    for (const row of rows) {

        const description =
            row.querySelector(
                ".restriction-description"
            ).value.trim();


        if (!description) {
            continue;
        }


        await apiRequest(
            `/api/v1/travel-restriction/add/${travelRequestId}`,
            "POST",
            {
                restrictionType:
                row.querySelector(
                    ".restriction-type"
                ).value,

                description:
                description,

                isRequired:
                row.querySelector(
                    ".restriction-required"
                ).checked
            }
        );

    }


    await refreshCurrentRequest();

}



/* =============================
   CHILDREN
============================= */

async function saveFamilyChildren() {

    ensureRequestExists();


    const oldChildren =
        currentRequest
            ?.children || [];


    for (const child of oldChildren) {

        await apiRequest(
            `/api/v1/child/delete/${child.id}`,
            "DELETE"
        );

    }


    const travelType =
        getCheckedValue(
            "travelType"
        );


    if (travelType !== "family") {

        await refreshCurrentRequest();
        return;

    }


    const inputs =
        [...document.querySelectorAll(
            ".family-child-age"
        )];


    if (inputs.length === 0) {

        throw new Error(
            "أضف طفل واحد على الأقل للرحلة العائلية"
        );

    }


    for (const input of inputs) {

        if (input.value === "") {

            throw new Error(
                "أدخل عمر كل طفل"
            );

        }


        const age =
            Number(input.value);


        if (age < 0 || age > 17) {

            throw new Error(
                "عمر الطفل لازم يكون من 0 إلى 17"
            );

        }


        await apiRequest(
            `/api/v1/child/add/${travelRequestId}`,
            "POST",
            {
                age: age
            }
        );

    }


    await refreshCurrentRequest();

}



/* =============================
   SUBMIT
============================= */

async function submitTravelRequest() {

    clearMessage();


    try {

        ensureRequestExists();



        const button =
            document.getElementById(
                "submitRequestButton"
            );


        button.disabled =
            true;


        await apiRequest(
            `/api/v1/travel-request/submit-travel-request/${travelRequestId}`,
            "PUT"
        );


        showMessage(
            "تم اعتماد طلب الرحلة بنجاح ✨",
            "success"
        );


        localStorage.setItem(
            "travelRequestId",
            travelRequestId
        );


        setTimeout(
            () => {

                /*
                 * هنا تنتهي مرحلتنا.
                 * اختيار الدولة مربوط لاحقًا
                 * مع Frontend العضوة الثانية.
                 */
                window.location.href =
                    "/dashboard";

            },
            900
        );


    } catch (error) {

        document.getElementById(
            "submitRequestButton"
        ).disabled =
            false;


        showMessage(
            error.message,
            "error"
        );

    }

}



/* =============================
   LOAD EXISTING
============================= */

async function loadExistingRequest(id) {

    try {

        currentRequest =
            await apiRequest(
                `/api/v1/travel-request/get-by-id/${id}`,
                "GET"
            );


        populateRequest(
            currentRequest
        );


    } catch (error) {

        showMessage(
            error.message,
            "error"
        );

    }

}


function populateRequest(request) {

    document.getElementById(
        "startDate"
    ).value =
        request.startDate || "";


    document.getElementById(
        "endDate"
    ).value =
        request.endDate || "";


    document.getElementById(
        "budget"
    ).value =
        request.budget ?? "";


    setRadio(
        "travelType",
        request.travelType
    );


    setRadio(
        "cityPlanMode",
        request.cityPlanMode
    );


    document.getElementById(
        "groupSize"
    ).value =
        request.groupSize ?? "";


    document.getElementById(
        "adultsCount"
    ).value =
        request.adultsCount ?? "";


    updateTravelTypeFields();


    const general =
        request.generalPreference;


    if (general) {

        setRadio(
            "weather",
            general.weather
        );

        setRadio(
            "environment",
            general.environment
        );

        setRadio(
            "crowdPreference",
            general.crowdPreference
        );

        setRadio(
            "tripPace",
            general.tripPace
        );

    }


    const standardFoodTypes =
        foodOptions
            .filter(
                option =>
                    option[0] !== "other"
            )
            .map(
                option =>
                    option[0]
            );


    request.foodPreferences
        ?.forEach(food => {

            let item;


            if (
                standardFoodTypes.includes(
                    food.foodType
                )
            ) {

                item =
                    document.querySelector(
                        `[data-food="${food.foodType}"]`
                    );

            } else {

                item =
                    document.querySelector(
                        '[data-food="other"]'
                    );


                const otherInput =
                    item?.querySelector(
                        ".other-food-input"
                    );


                if (otherInput) {

                    otherInput.value =
                        food.foodType;

                    otherInput.style.display =
                        "block";

                }

            }


            if (!item) return;


            item.querySelector(
                ".food-checkbox"
            ).checked =
                true;


            item.querySelector(
                ".food-required"
            ).checked =
                food.isRequired === true;

        });


    request.activityPreferences
        ?.forEach(activity => {

            const item =
                document.querySelector(
                    `[data-activity="${activity.activityType}"]`
                );


            if (!item) return;


            item.querySelector(
                ".activity-checkbox"
            ).checked =
                true;


            item.querySelector(
                ".priority-select"
            ).value =
                activity.priority;

        });


    document.getElementById(
        "restrictionRows"
    ).innerHTML =
        "";


    request.travelRestrictions
        ?.forEach(
            restriction =>
                addRestrictionRow(
                    restriction
                )
        );


    const children =
        request.children || [];


    const childrenCountInput =
        document.getElementById(
            "childrenCount"
        );


    if (childrenCountInput) {

        childrenCountInput.value =
            children.length > 0
                ? children.length
                : 1;


        renderFamilyChildrenAges();


        const ageInputs =
            document.querySelectorAll(
                ".family-child-age"
            );


        children.forEach((child, index) => {

            if (ageInputs[index]) {
                ageInputs[index].value = child.age;
            }

        });

    }

}



/* =============================
   REVIEW
============================= */

function renderReview() {

    const type =
        getCheckedValue(
            "travelType"
        );


    const mode =
        getCheckedValue(
            "cityPlanMode"
        );


    const typeNames = {

        solo:
            "رحلة فردية",

        couple:
            "شخصين",

        group:
            "مجموعة",

        family:
            "عائلة"

    };


    const modeNames = {

        single_city:
            "مدينة واحدة",

        multi_city_manual:
            "عدة مدن - أختارها بنفسي",

        multi_city_ai:
            "عدة مدن - اقتراح ذكي"

    };


    document.getElementById(
        "reviewContent"
    ).innerHTML = `

        <div class="review-card">

            <small>
                التواريخ
            </small>

            <strong>
                ${document.getElementById("startDate").value}
                -
                ${document.getElementById("endDate").value}
            </strong>

        </div>


        <div class="review-card">

            <small>
                الميزانية
            </small>

            <strong>
                ${document.getElementById("budget").value}
                ر.س
            </strong>

        </div>


        <div class="review-card">

            <small>
                نوع الرحلة
            </small>

            <strong>
                ${typeNames[type] || "-"}
            </strong>

        </div>


        <div class="review-card">

            <small>
                تخطيط المدن
            </small>

            <strong>
                ${modeNames[mode] || "-"}
            </strong>

        </div>

    `;

}



/* =============================
   WIZARD UI
============================= */

function updateWizard() {

    document
        .querySelectorAll(
            ".wizard-step"
        )
        .forEach(step => {

            step.classList.toggle(
                "active",
                Number(
                    step.dataset.step
                ) === currentStep
            );

        });


    document
        .querySelectorAll(
            ".step-indicator"
        )
        .forEach(indicator => {

            const number =
                Number(
                    indicator.dataset.indicator
                );


            indicator.classList.toggle(
                "active",
                number === currentStep
            );


            indicator.classList.toggle(
                "completed",
                number < currentStep
            );

        });


    document.getElementById(
        "previousButton"
    ).style.visibility =
        currentStep === 1
            ? "hidden"
            : "visible";


    document.getElementById(
        "nextButton"
    ).style.display =
        currentStep === 6
            ? "none"
            : "inline-flex";


    document.getElementById(
        "submitRequestButton"
    ).style.display =
        currentStep === 6
            ? "inline-flex"
            : "none";



}



/* =============================
   API
============================= */

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


    let data = null;


    const text =
        await response.text();


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
            "صار خطأ أثناء حفظ البيانات"
        );

    }


    return data;

}


async function refreshCurrentRequest() {

    if (!travelRequestId) {
        return;
    }


    currentRequest =
        await apiRequest(
            `/api/v1/travel-request/get-by-id/${travelRequestId}`,
            "GET"
        );

}



/* =============================
   HELPERS
============================= */

function ensureRequestExists() {

    if (!travelRequestId) {

        throw new Error(
            "احفظ بيانات الرحلة أول"
        );

    }

}


function getCheckedValue(name) {

    return document.querySelector(
        `input[name="${name}"]:checked`
    )?.value || null;

}


function setRadio(
    name,
    value
) {

    if (!value) return;


    const input =
        document.querySelector(
            `input[name="${name}"][value="${value}"]`
        );


    if (input) {

        input.checked =
            true;

    }

}


function showMessage(
    message,
    type
) {

    const element =
        document.getElementById(
            "requestMessage"
        );


    element.textContent =
        message;


    element.className =
        `request-message ${type}`;

}


function clearMessage() {

    const element =
        document.getElementById(
            "requestMessage"
        );


    element.textContent =
        "";


    element.className =
        "request-message";

}

document.getElementById(
    "childrenCount"
)?.addEventListener(
    "input",
    renderFamilyChildrenAges
);


function renderFamilyChildrenAges() {

    const container =
        document.getElementById(
            "familyChildrenAges"
        );


    const count =
        Number(
            document.getElementById(
                "childrenCount"
            ).value
        );


    if (!container) return;


    const oldValues =
        [...container.querySelectorAll(
            ".family-child-age"
        )]
            .map(input =>
                input.value
            );


    container.innerHTML = "";


    for (
        let i = 0;
        i < count;
        i++
    ) {

        const wrapper =
            document.createElement("div");


        wrapper.className =
            "form-group";


        wrapper.innerHTML = `

            <label>
                عمر الطفل ${i + 1}
            </label>

            <input type="number"
                   class="family-child-age"
                   min="0"
                   max="17"
                   placeholder="العمر"
                   value="${oldValues[i] || ""}">

        `;


        container.appendChild(
            wrapper
        );

    }

}
