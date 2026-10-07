// =====================================================
// SahlDarbak - Blocked users (/blocked)
// GET    /blocked-user/get-blocked/{blockerId}
// DELETE /blocked-user/unblock/{blockerId}/{blockedId}
// DELETE /demo/reset
// =====================================================

let currentUserId = null;


document.addEventListener("DOMContentLoaded", () => {

    currentUserId = requireLogin();

    if (!currentUserId) {
        return;
    }

    loadBlocked();
    setupResetDemo();

});


async function loadBlocked() {

    const loader = document.getElementById("blLoader");
    const empty = document.getElementById("blEmpty");
    const list = document.getElementById("blList");

    list.innerHTML = "";
    empty.style.display = "none";
    loader.style.display = "block";

    let items = [];

    try {

        const data = await immerseApi(`/blocked-user/get-blocked/${currentUserId}`);

        items = Array.isArray(data) ? data : [];

    } catch (_) {

        // لو ما فيه محظورين الباك اند ممكن يرجع خطأ، نعرضها كقائمة فاضية
        items = [];

    }

    loader.style.display = "none";

    if (items.length === 0) {
        empty.style.display = "block";
        return;
    }

    items.forEach(item => {
        if (item.profile) {
            list.appendChild(createCard(item.profile));
        }
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


function createCard(profile) {

    const card = document.createElement("article");
    card.className = "im-card bl-card";

    // ---- الاسم ----
    const person = document.createElement("div");
    person.className = "bl-person";

    const avatar = document.createElement("div");
    avatar.className = "bl-avatar";
    avatar.appendChild(createIcon("block"));

    const info = document.createElement("div");

    const name = document.createElement("div");
    name.className = "bl-name";
    name.textContent = profile.fullName;

    const meta = document.createElement("div");
    meta.className = "bl-meta";
    meta.textContent = [profile.city, profile.country].filter(Boolean).join("، ");

    info.append(name, meta);
    person.append(avatar, info);

    // ---- زر فك الحظر ----
    const button = document.createElement("button");
    button.type = "button";
    button.className = "btn btn-primary";
    button.append(createIcon("lock_open"), createText("imBlUnblock"));

    button.addEventListener("click", () => {
        unblock(profile.id, card, button);
    });

    card.append(person, button);

    return card;

}


async function unblock(blockedId, card, button) {

    setBtnLoading(button, true);

    try {

        await immerseApi(
            `/blocked-user/unblock/${currentUserId}/${blockedId}`,
            { method: "DELETE" }
        );

        showSuccess(t("imBlUnblockDone"));

        card.remove();

        if (document.getElementById("blList").children.length === 0) {
            document.getElementById("blEmpty").style.display = "block";
        }

    } catch (error) {

        showError(error.message);

        setBtnLoading(button, false);

    }

}


// ---------- إعادة ضبط الديمو ----------

function setupResetDemo() {

    const button = document.getElementById("resetDemoBtn");

    button.addEventListener("click", async () => {

        if (!confirm(t("imResetConfirm"))) {
            return;
        }

        setBtnLoading(button, true);

        try {

            await immerseApi("/demo/reset", { method: "DELETE" });

            showSuccess(t("imResetDone"));

            loadBlocked();

        } catch (error) {

            showError(error.message);

        }

        setBtnLoading(button, false);

    });

}
