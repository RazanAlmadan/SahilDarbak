// =====================================================
// SahlDarbak - My invites (/invites)
// GET /travel-match/received/{userId}
// GET /travel-match/sent/{userId}
// GET /travel-match/pending-count/{userId}
// PUT /travel-match/update/{inviteId}/{userId}/{status}
// =====================================================

let currentUserId = null;
let activeTab = "received";


document.addEventListener("DOMContentLoaded", () => {

    currentUserId = requireLogin();

    if (!currentUserId) {
        return;
    }

    document.getElementById("tabReceived")
        .addEventListener("click", () => switchTab("received"));

    document.getElementById("tabSent")
        .addEventListener("click", () => switchTab("sent"));

    loadPendingCount();
    loadList();

});


function switchTab(tab) {

    activeTab = tab;

    document.getElementById("tabReceived")
        .classList.toggle("iv-tab-active", tab === "received");

    document.getElementById("tabSent")
        .classList.toggle("iv-tab-active", tab === "sent");

    loadList();

}


async function loadPendingCount() {

    const badge = document.getElementById("pendingBadge");

    try {

        const count = await immerseApi(
            `/travel-match/pending-count/${currentUserId}`
        );

        badge.textContent = count;
        badge.style.display = Number(count) > 0 ? "inline-block" : "none";

    } catch (_) {

        badge.style.display = "none";

    }

}


async function loadList() {

    const loader = document.getElementById("ivLoader");
    const empty = document.getElementById("ivEmpty");
    const list = document.getElementById("ivList");

    loader.style.display = "block";
    empty.style.display = "none";
    list.innerHTML = "";

    const path = activeTab === "received"
        ? `/travel-match/received/${currentUserId}`
        : `/travel-match/sent/${currentUserId}`;

    let invites = [];

    try {

        const data = await immerseApi(path);

        invites = Array.isArray(data) ? data : [];

    } catch (_) {

        // لو ما فيه دعوات الباك اند ممكن يرجع خطأ، نعرضها كقائمة فاضية
        invites = [];

    }

    loader.style.display = "none";

    if (invites.length === 0) {

        document.getElementById("ivEmptyText").textContent =
            t(activeTab === "received" ? "imIvEmptyReceived" : "imIvEmptySent");

        empty.style.display = "block";

        return;
    }

    invites.forEach(invite => {
        list.appendChild(createCard(invite));
    });

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


function statusKey(status) {

    const value = String(status || "").toLowerCase();

    if (value === "accepted") {
        return "imIvAccepted";
    }

    if (value === "rejected") {
        return "imIvRejected";
    }

    return "imIvPending";

}


function createCard(invite) {

    const isReceived = activeTab === "received";

    // في المستلمة الشخص هو المرسل، وفي المرسلة هو المستلم
    const person = isReceived ? invite.sender : invite.receiver;

    const status = String(invite.status || "").toLowerCase();

    const card = document.createElement("article");
    card.className = "im-card iv-card";

    // ---- الرأس ----
    const head = document.createElement("div");
    head.className = "iv-card-head";

    const personBox = document.createElement("div");
    personBox.className = "iv-person";

    const avatar = document.createElement("div");
    avatar.className = "iv-avatar";
    avatar.appendChild(createIcon("person"));

    const info = document.createElement("div");

    const name = document.createElement("div");
    name.className = "iv-name";
    name.textContent = person ? person.fullName : "";

    const meta = document.createElement("div");
    meta.className = "iv-meta";
    meta.textContent = [
        person && person.city,
        person && person.country,
        formatDate(invite.createdAt)
    ].filter(Boolean).join(" · ");

    info.append(name, meta);
    personBox.append(avatar, info);

    const badge = document.createElement("span");
    badge.className = "iv-status iv-status-" +
        (status === "accepted" || status === "rejected" ? status : "pending");
    badge.setAttribute("data-i18n", statusKey(status));
    badge.textContent = t(statusKey(status));

    head.append(personBox, badge);

    // ---- الرسالة ----
    const message = document.createElement("p");
    message.className = "iv-message";
    message.textContent = invite.message || "";

    card.append(head, message);

    // ---- أزرار القبول والرفض (للمستلمة المعلقة فقط) ----
    if (isReceived && status === "pending") {

        const actions = document.createElement("div");
        actions.className = "iv-actions";

        const acceptBtn = document.createElement("button");
        acceptBtn.type = "button";
        acceptBtn.className = "btn btn-primary";
        acceptBtn.append(createIcon("check"), createText("imIvAccept"));
        acceptBtn.addEventListener("click", () => {
            answerInvite(invite.inviteId, "accepted", acceptBtn);
        });

        const rejectBtn = document.createElement("button");
        rejectBtn.type = "button";
        rejectBtn.className = "btn btn-secondary";
        rejectBtn.append(createIcon("close"), createText("imIvReject"));
        rejectBtn.addEventListener("click", () => {
            answerInvite(invite.inviteId, "rejected", rejectBtn);
        });

        actions.append(acceptBtn, rejectBtn);
        card.appendChild(actions);

    }

    return card;

}


async function answerInvite(inviteId, status, button) {

    setBtnLoading(button, true);

    try {

        await immerseApi(
            `/travel-match/update/${inviteId}/${currentUserId}/${status}`,
            { method: "PUT" }
        );

        showSuccess(t(status === "accepted" ? "imIvAcceptedDone" : "imIvRejectedDone"));

        loadPendingCount();
        loadList();

    } catch (error) {

        showError(error.message);

        setBtnLoading(button, false);

    }

}


function formatDate(value) {

    if (!value) {
        return "";
    }

    const lang = document.documentElement.lang === "en" ? "en-GB" : "ar-SA";

    return new Date(value).toLocaleDateString(lang, {
        day: "numeric",
        month: "short",
        year: "numeric"
    });

}
