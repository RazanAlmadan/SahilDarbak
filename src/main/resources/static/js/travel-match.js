// =====================================================
// SahlDarbak - Travelers around you (/travel-match)
// GET  /travel-presence/nearest/{userId}
// GET  /travel-match/sent/{userId}        (نعرف منهم دعيناهم)
// POST /travel-match/add/{senderId}/{receiverId}?message=
// =====================================================

let currentUserId = null;
let invitedIds = new Set();
let selectedTraveler = null;
let selectedButton = null;


document.addEventListener("DOMContentLoaded", () => {

    currentUserId = requireLogin();

    if (!currentUserId) {
        return;
    }

    setupModal();
    loadTravelers();

});


async function loadTravelers() {

    const loader = document.getElementById("tmLoader");
    const empty = document.getElementById("tmEmpty");
    const grid = document.getElementById("tmGrid");

    try {

        const [travelers, sent] = await Promise.all([
            immerseApi(`/travel-presence/nearest/${currentUserId}`),
            loadSentInvites()
        ]);

        // نخزن أرقام اللي دعيناهم
        invitedIds = new Set(
            sent
                .filter(invite => invite.receiver)
                .map(invite => invite.receiver.id)
        );

        loader.style.display = "none";

        if (!Array.isArray(travelers) || travelers.length === 0) {
            empty.style.display = "block";
            return;
        }

        travelers.forEach(traveler => {
            grid.appendChild(createCard(traveler));
        });

    } catch (error) {

        loader.style.display = "none";

        showError(error.message);

    }

}


// لو ما عنده دعوات يرجع الباك اند خطأ أو فاضي، نتعامل معها كقائمة فاضية
async function loadSentInvites() {

    try {

        const data = await immerseApi(`/travel-match/sent/${currentUserId}`);

        return Array.isArray(data) ? data : [];

    } catch (_) {

        return [];

    }

}


function createIcon(name) {

    const icon = document.createElement("span");
    icon.className = "material-symbols-rounded";
    icon.textContent = name;

    return icon;

}


function createText(key) {

    const span = document.createElement("span");
    span.setAttribute("data-i18n", key);
    span.textContent = t(key);

    return span;

}


function createCard(traveler) {

    const card = document.createElement("article");
    card.className = "im-card tm-card";

    // ---- الرأس: صورة + اسم + عمر وجنس ----
    const head = document.createElement("div");
    head.className = "tm-card-head";

    const avatar = document.createElement("div");
    avatar.className = "tm-avatar";
    avatar.appendChild(createIcon("person"));

    const info = document.createElement("div");

    const name = document.createElement("div");
    name.className = "tm-name";
    name.textContent = traveler.fullName;

    const meta = document.createElement("div");
    meta.className = "tm-meta";

    const genderKey = traveler.gender === "female" ? "imFemale" : "imMale";
    meta.append(createText(genderKey));

    if (traveler.age != null) {
        meta.append(" · " + traveler.age);
    }

    info.append(name, meta);
    head.append(avatar, info);

    // ---- من وين ----
    const origin = document.createElement("div");
    origin.className = "tm-origin";

    const originText = document.createElement("span");
    originText.textContent =
        [traveler.homeCity, traveler.homeCountry]
            .filter(Boolean)
            .join("، ");

    origin.append(createIcon("home"), originText);

    // ---- النبذة ----
    const bio = document.createElement("p");
    bio.className = "tm-bio";
    bio.textContent = traveler.bio || "";

    // ---- المسافة ----
    const distance = document.createElement("div");
    distance.className = "tm-distance";

    const km = Number(traveler.distanceKm || 0).toFixed(1);
    distance.append(km + " ", createText("imTmKm"));

    // ---- الزر ----
    const button = document.createElement("button");
    button.type = "button";
    button.className = "btn btn-primary";

    if (invitedIds.has(traveler.userId)) {

        markInvited(button);

    } else {

        button.append(createIcon("send"), createText("imTmInvite"));

        button.addEventListener("click", () => {
            openModal(traveler, button);
        });

    }

    card.append(head, origin, bio, distance, button);

    return card;

}


function markInvited(button) {

    button.disabled = true;
    button.className = "btn btn-secondary";
    button.innerHTML = "";
    button.append(createIcon("check"), createText("imTmInvited"));

}


// ---------- النافذة ----------

function setupModal() {

    const message = document.getElementById("tmMessage");

    message.addEventListener("input", () => {
        document.getElementById("tmMsgCount").textContent =
            message.value.length;
    });

    document.getElementById("tmCancelBtn")
        .addEventListener("click", closeModal);

    document.getElementById("tmModal")
        .addEventListener("click", event => {
            if (event.target.id === "tmModal") {
                closeModal();
            }
        });

    document.getElementById("tmSendBtn")
        .addEventListener("click", sendInvite);

}


function openModal(traveler, button) {

    selectedTraveler = traveler;
    selectedButton = button;

    document.getElementById("tmModalName").textContent = traveler.fullName;
    document.getElementById("tmMessage").value = "";
    document.getElementById("tmMsgCount").textContent = "0";
    document.getElementById("tmModal").style.display = "flex";
    document.getElementById("tmMessage").focus();

}


function closeModal() {

    document.getElementById("tmModal").style.display = "none";

    selectedTraveler = null;
    selectedButton = null;

}


async function sendInvite() {

    const text = document.getElementById("tmMessage").value.trim();
    const sendButton = document.getElementById("tmSendBtn");

    if (!text) {
        showError(t("imTmMsgRequired"));
        return;
    }

    setBtnLoading(sendButton, true);

    try {

        await immerseApi(
            `/travel-match/add/${currentUserId}/${selectedTraveler.userId}` +
            `?message=${encodeURIComponent(text)}`,
            { method: "POST" }
        );

        invitedIds.add(selectedTraveler.userId);

        markInvited(selectedButton);

        showSuccess(t("imTmSent"));

        closeModal();

    } catch (error) {

        showError(error.message);

    }

    setBtnLoading(sendButton, false);

}
