// =====================================================
// SahlDarbak - Immerse (Flow 3) shared helpers
// يُحمَّل بعد language.js و base.js في كل صفحات Flow 3
// =====================================================

// ---------- current user ----------
// المستخدم الحالي يُحفظ في localStorage وقت تسجيل الدخول (login.js)
// لما يجي نظام Auth حقيقي نغيّر هذي الدالة فقط.
function getCurrentUserId() {
    const value = parseInt(localStorage.getItem("userId"), 10);
    return Number.isNaN(value) ? null : value;
}

function requireLogin() {
    const userId = getCurrentUserId();
    if (!userId) {
        window.location.href = "/login";
        return null;
    }
    return userId;
}

// ---------- translations ----------
function t(key) {
    const lang = document.documentElement.lang || "ar";
    if (typeof translations !== "undefined" && translations[lang] && translations[lang][key]) {
        return translations[lang][key];
    }
    return key;
}

// ---------- fetch wrapper ----------
// يضيف /api/v1 ويقرأ JSON أو نص، ويرمي رسالة الباك اند عند الخطأ
async function immerseApi(path, options = {}) {
    const response = await fetch("/api/v1" + path, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    });

    const text = await response.text();

    let data = text;
    try {
        data = JSON.parse(text);
    } catch (_) {
        // رد نصي عادي، نخليه كما هو
    }

    if (!response.ok) {
        const message =
            (data && data.message) ||
            (typeof data === "string" && data) ||
            t("imGenericError");
        throw new Error(message);
    }

    return data;
}

// ---------- toasts ----------
function showToast(message, type) {
    let box = document.getElementById("imToasts");
    if (!box) {
        box = document.createElement("div");
        box.id = "imToasts";
        box.className = "im-toasts";
        document.body.appendChild(box);
    }

    const toast = document.createElement("div");
    toast.className = "im-toast im-toast-" + type;
    toast.textContent = message;
    box.appendChild(toast);

    setTimeout(() => toast.remove(), 4000);
}

function showError(message) {
    showToast(message, "error");
}

function showSuccess(message) {
    showToast(message, "success");
}

// ---------- button loading ----------
function setBtnLoading(button, isLoading) {
    if (!button) return;

    if (isLoading) {
        button.dataset.original = button.innerHTML;
        button.disabled = true;
        button.innerHTML =
            '<span class="material-symbols-rounded loading-icon">progress_activity</span>';
    } else {
        button.disabled = false;
        if (button.dataset.original) {
            button.innerHTML = button.dataset.original;
        }
    }
}

// الباك اند يرجع "profile not found" لو المستخدم ما أنشأ بروفايل
function isProfileMissingError(message) {
    return /profile not found/i.test(message || "");
}

// الباك اند يرجع رسالة فيها كلمة "check in" لو المستخدم ما سجّل وصول
function isNotCheckedInError(message) {
    return /check/i.test(message || "");
}
