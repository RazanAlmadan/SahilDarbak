// =====================================================
// SahlDarbak - Profile page
// GET /profile/get/{userId}  -> يعبي النموذج لو فيه بروفايل
// POST /profile/add          -> أول مرة
// PUT  /profile/update       -> تعديل
// =====================================================

let profileExists = false;


document.addEventListener("DOMContentLoaded", () => {

    const userId = requireLogin();

    if (!userId) {
        return;
    }

    // أصغر تاريخ ميلاد مسموح = اليوم (الباك اند يتأكد إنه في الماضي)
    document.getElementById("dateOfBirth").max =
        new Date().toISOString().split("T")[0];

    setupBioCounter();
    setupForm(userId);
    loadProfile(userId);

});


async function loadProfile(userId) {

    try {

        const profile = await immerseApi(`/profile/get/${userId}`);

        fillForm(profile);

        setMode(true);

    } catch (error) {

        // مافيه بروفايل = وضع الإنشاء، أي خطأ ثاني نعرضه
        if (!isProfileMissingError(error.message)) {
            showError(error.message);
        }

        setMode(false);

    } finally {

        document.getElementById("profileLoader").style.display = "none";
        document.getElementById("profileForm").style.display = "block";

    }

}


function fillForm(profile) {

    document.getElementById("fullName").value = profile.fullName || "";
    document.getElementById("dateOfBirth").value = profile.dateOfBirth || "";
    document.getElementById("gender").value = profile.gender || "";
    document.getElementById("country").value = profile.country || "";
    document.getElementById("city").value = profile.city || "";
    document.getElementById("bio").value = profile.bio || "";

    updateBioCounter();

}


// يغير عنوان الصفحة ونص الزر حسب إنشاء / تعديل
function setMode(exists) {

    profileExists = exists;

    document.getElementById("profileTitle").dataset.i18n =
        exists ? "imProfileEditTitle" : "imProfileCreateTitle";

    document.getElementById("saveBtnText").dataset.i18n =
        exists ? "imSaveEdit" : "imSaveCreate";

    // نعيد تطبيق الترجمة عشان النص الجديد يظهر
    changeLanguage(document.documentElement.lang);

}


function setupBioCounter() {

    document.getElementById("bio")
        .addEventListener("input", updateBioCounter);

}


function updateBioCounter() {

    document.getElementById("bioCount").textContent =
        document.getElementById("bio").value.length;

}


function setupForm(userId) {

    const form = document.getElementById("profileForm");
    const saveButton = document.getElementById("saveBtn");

    form.addEventListener("submit", async event => {

        event.preventDefault();

        const body = {
            userId: userId,
            fullName: document.getElementById("fullName").value.trim(),
            dateOfBirth: document.getElementById("dateOfBirth").value,
            gender: document.getElementById("gender").value,
            country: document.getElementById("country").value.trim(),
            city: document.getElementById("city").value.trim(),
            bio: document.getElementById("bio").value.trim()
        };

        if (
            !body.fullName ||
            !body.dateOfBirth ||
            !body.gender ||
            !body.country ||
            !body.city
        ) {
            showError(t("imFillRequired"));
            return;
        }

        setBtnLoading(saveButton, true);

        try {

            await immerseApi(
                profileExists ? "/profile/update" : "/profile/add",
                {
                    method: profileExists ? "PUT" : "POST",
                    body: JSON.stringify(body)
                }
            );

            if (profileExists) {

                showSuccess(t("imProfileUpdated"));

                setBtnLoading(saveButton, false);

            } else {

                showSuccess(t("imProfileSaved"));

                // أول مرة: نكمل لتسجيل الوصول
                setTimeout(() => {
                    window.location.href = "/checkin";
                }, 900);

            }

        } catch (error) {

            showError(error.message);

            setBtnLoading(saveButton, false);

        }

    });

}
