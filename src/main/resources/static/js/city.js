// =====================================================
// SahlDarbak - City home page
// GET    /travel-presence/nearby-count/{userId}
// GET    /assistant/city-guide/{userId}   (نص عربي، 4 أقسام)
// DELETE /travel-presence/check-out/{userId}
// =====================================================

const GUIDE_ICONS = [
    "lightbulb",
    "diversity_3",
    "health_and_safety",
    "call"
];


document.addEventListener("DOMContentLoaded", () => {

    const userId = requireLogin();

    if (!userId) {
        return;
    }

    loadNearbyCount(userId);
    loadGuide(userId);
    setupCheckout(userId);

});


// لو الباك اند قال "سجّل وصولك أول" نوديه لصفحة تسجيل الوصول
function handleCityError(error) {

    if (isNotCheckedInError(error.message)) {
        window.location.href = "/checkin";
        return;
    }

    showError(error.message);

}


async function loadNearbyCount(userId) {

    try {

        const count = await immerseApi(
            `/travel-presence/nearby-count/${userId}`
        );

        document.getElementById("nearbyCount").textContent = count;

    } catch (error) {

        handleCityError(error);

    }

}


async function loadGuide(userId) {

    const loader = document.getElementById("guideLoader");
    const grid = document.getElementById("guideGrid");

    try {

        const text = await immerseApi(
            `/assistant/city-guide/${userId}`
        );

        loader.style.display = "none";

        renderGuide(grid, String(text));

    } catch (error) {

        loader.style.display = "none";

        handleCityError(error);

    }

}


// يقسم النص إلى أقسام: سطر بدون "-" = عنوان، سطر يبدأ بـ "-" = نقطة
function parseGuide(text) {

    const sections = [];
    let current = null;

    text.split("\n").forEach(raw => {

        const line = raw.trim();

        if (!line) {
            return;
        }

        if (line.startsWith("-") || line.startsWith("•")) {

            if (!current) {
                current = { title: "", items: [] };
                sections.push(current);
            }

            current.items.push(line.replace(/^[-•]\s*/, ""));

        } else {

            current = {
                title: line.replace(/[:：]$/, ""),
                items: []
            };

            sections.push(current);

        }

    });

    return sections;

}


function renderGuide(grid, text) {

    const sections = parseGuide(text);

    grid.innerHTML = "";

    // لو الشكل غير متوقع نعرض النص كما هو
    if (sections.length === 0) {

        const card = document.createElement("div");
        card.className = "im-card";
        card.style.whiteSpace = "pre-line";
        card.textContent = text;
        grid.appendChild(card);

        return;
    }

    sections.forEach((section, index) => {

        const card = document.createElement("article");
        card.className = "im-card im-guide-card";

        const heading = document.createElement("h3");

        const icon = document.createElement("span");
        icon.className = "material-symbols-rounded";
        icon.textContent = GUIDE_ICONS[index] || "info";

        const title = document.createElement("span");
        title.textContent = section.title;

        heading.append(icon, title);
        card.appendChild(heading);

        const list = document.createElement("ul");

        section.items.forEach(item => {
            const li = document.createElement("li");
            li.textContent = item;
            list.appendChild(li);
        });

        card.appendChild(list);
        grid.appendChild(card);

    });

}


function setupCheckout(userId) {

    const button = document.getElementById("checkoutBtn");

    button.addEventListener("click", async () => {

        setBtnLoading(button, true);

        try {

            await immerseApi(
                `/travel-presence/check-out/${userId}`,
                { method: "DELETE" }
            );

            showSuccess(t("imCheckedOut"));

            setTimeout(() => {
                window.location.href = "/checkin";
            }, 800);

        } catch (error) {

            showError(error.message);

            setBtnLoading(button, false);

        }

    });

}
