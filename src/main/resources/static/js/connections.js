// =====================================================
// SahlDarbak - My connections + chat (/connections)
// GET  /travel-match/accepted/{userId}
// POST /blocked-user/add/{blockerId}/{blockedId}
// WS   /chat?userId=ID    (نرسل  toUserId:text ، ونستقبل  fromUserId: text)
// =====================================================

let currentUserId = null;
let socket = null;

const chats = {};          // رسائل كل شخص: { userId: [ {kind, text} ] }
const unread = {};         // عدد الرسائل غير المقروءة
const unreadBadges = {};   // عنصر الشارة لكل بطاقة
let openChatUserId = null;


document.addEventListener("DOMContentLoaded", () => {

    currentUserId = requireLogin();

    if (!currentUserId) {
        return;
    }

    setupChatWindow();
    connectSocket();
    loadConnections();

});


// =====================================================
// الاتصالات
// =====================================================

// الباك اند قد يرجّع البروفايل مباشرة أو داخل sender / receiver،
// فنأخذ "الطرف الثاني" (اللي مو أنا) بشكل مرن.
function getOtherPerson(item) {

    if (item.fullName) {
        return item;
    }

    const candidates = [item.sender, item.receiver, item.profile, item.traveler]
        .filter(Boolean);

    return candidates.find(p => p.id !== currentUserId) || candidates[0] || null;

}


function getPersonId(person) {

    return person.id != null ? person.id : person.userId;

}


async function loadConnections() {

    const loader = document.getElementById("cnLoader");
    const empty = document.getElementById("cnEmpty");
    const grid = document.getElementById("cnGrid");

    let items = [];

    try {

        const data = await immerseApi(`/travel-match/accepted/${currentUserId}`);

        items = Array.isArray(data) ? data : [];

    } catch (_) {

        items = [];

    }

    loader.style.display = "none";

    const cards = items
        .map(item => ({ item, person: getOtherPerson(item) }))
        .filter(entry => entry.person);

    if (cards.length === 0) {
        empty.style.display = "block";
        return;
    }

    cards.forEach(entry => {
        grid.appendChild(createCard(entry.person));
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


function createButton(icon, key, extraClass) {

    const button = document.createElement("button");
    button.type = "button";
    button.className = "btn " + extraClass;
    button.append(createIcon(icon), createText(key));

    return button;

}


function createCard(person) {

    const personId = getPersonId(person);

    const card = document.createElement("article");
    card.className = "im-card cn-card";

    // ---- الاسم ----
    const personBox = document.createElement("div");
    personBox.className = "cn-person";

    const avatar = document.createElement("div");
    avatar.className = "cn-avatar";
    avatar.appendChild(createIcon("person"));

    const info = document.createElement("div");

    const name = document.createElement("div");
    name.className = "cn-name";
    name.textContent = person.fullName;

    const meta = document.createElement("div");
    meta.className = "cn-meta";
    meta.textContent = [person.city, person.country].filter(Boolean).join("، ");

    info.append(name, meta);
    personBox.append(avatar, info);

    // ---- النبذة ----
    const bio = document.createElement("p");
    bio.className = "cn-bio";
    bio.textContent = person.bio || "";

    // ---- الأزرار ----
    const actions = document.createElement("div");
    actions.className = "cn-actions";

    const chatBtn = createButton("chat", "imCnChat", "btn-primary");

    const badge = document.createElement("span");
    badge.className = "cn-unread";
    badge.style.display = "none";
    chatBtn.appendChild(badge);
    unreadBadges[personId] = badge;

    chatBtn.addEventListener("click", () => {
        openChat(personId, person.fullName);
    });

    const blockBtn = createButton("block", "imCnBlock", "btn-secondary");
    blockBtn.addEventListener("click", () => {
        blockPerson(personId, card, blockBtn);
    });

    actions.append(chatBtn, blockBtn);

    card.append(personBox, bio, actions);

    return card;

}


async function blockPerson(personId, card, button) {

    if (!confirm(t("imCnBlockConfirm"))) {
        return;
    }

    setBtnLoading(button, true);

    try {

        await immerseApi(
            `/blocked-user/add/${currentUserId}/${personId}`,
            { method: "POST" }
        );

        showSuccess(t("imCnBlockedDone"));

        if (openChatUserId === personId) {
            closeChat();
        }

        card.remove();

        if (document.getElementById("cnGrid").children.length === 0) {
            document.getElementById("cnEmpty").style.display = "block";
        }

    } catch (error) {

        showError(error.message);

        setBtnLoading(button, false);

    }

}


// =====================================================
// الشات (WebSocket)
// =====================================================

function connectSocket() {

    const protocol = location.protocol === "https:" ? "wss" : "ws";

    socket = new WebSocket(
        `${protocol}://${location.host}/chat?userId=${currentUserId}`
    );

    socket.addEventListener("message", event => {
        handleIncoming(String(event.data));
    });

}


function ensureSocket() {

    return new Promise((resolve, reject) => {

        if (socket && socket.readyState === WebSocket.OPEN) {
            resolve();
            return;
        }

        if (!socket || socket.readyState === WebSocket.CLOSED
            || socket.readyState === WebSocket.CLOSING) {
            connectSocket();
        }

        socket.addEventListener("open", () => resolve(), { once: true });
        socket.addEventListener("error", () => reject(new Error(t("imCnOffline"))), { once: true });

    });

}


// الشكل: "fromUserId: text"  أو  "error: ..."
function handleIncoming(raw) {

    const index = raw.indexOf(":");

    if (index === -1) {
        return;
    }

    const head = raw.substring(0, index).trim();
    const body = raw.substring(index + 1).trim();

    if (head.toLowerCase() === "error") {

        showError(body);

        if (openChatUserId != null) {
            addMessage(openChatUserId, "error", t("imCnSendFailed") + " " + body);
        }

        return;
    }

    const fromId = parseInt(head, 10);

    if (Number.isNaN(fromId)) {
        return;
    }

    addMessage(fromId, "them", body);

    if (openChatUserId !== fromId) {

        unread[fromId] = (unread[fromId] || 0) + 1;

        const badge = unreadBadges[fromId];

        if (badge) {
            badge.textContent = unread[fromId];
            badge.style.display = "inline-block";
        }

    }

}


function addMessage(personId, kind, text) {

    if (!chats[personId]) {
        chats[personId] = [];
    }

    chats[personId].push({ kind, text });

    if (openChatUserId === personId) {
        renderMessage({ kind, text });
    }

}


function renderMessage(message) {

    const box = document.getElementById("cnMessages");

    const bubble = document.createElement("div");
    bubble.className = "cn-msg cn-msg-" + message.kind;
    bubble.textContent = message.text;

    box.appendChild(bubble);
    box.scrollTop = box.scrollHeight;

}


function setupChatWindow() {

    document.getElementById("cnChatClose")
        .addEventListener("click", closeChat);

    document.getElementById("cnChat")
        .addEventListener("click", event => {
            if (event.target.id === "cnChat") {
                closeChat();
            }
        });

    document.getElementById("cnChatForm")
        .addEventListener("submit", async event => {

            event.preventDefault();

            const input = document.getElementById("cnChatInput");
            const text = input.value.trim();

            if (!text || openChatUserId == null) {
                return;
            }

            try {

                await ensureSocket();

                socket.send(`${openChatUserId}:${text}`);

                addMessage(openChatUserId, "me", text);

                input.value = "";

            } catch (error) {

                showError(error.message);

            }

        });

}


function openChat(personId, fullName) {

    openChatUserId = personId;

    unread[personId] = 0;

    if (unreadBadges[personId]) {
        unreadBadges[personId].style.display = "none";
    }

    document.getElementById("cnChatName").textContent = fullName;

    const input = document.getElementById("cnChatInput");
    input.placeholder = t("imCnTypeMsg");
    input.value = "";

    const box = document.getElementById("cnMessages");
    box.innerHTML = "";

    (chats[personId] || []).forEach(renderMessage);

    document.getElementById("cnChat").style.display = "flex";

    input.focus();

}


function closeChat() {

    document.getElementById("cnChat").style.display = "none";

    openChatUserId = null;

}