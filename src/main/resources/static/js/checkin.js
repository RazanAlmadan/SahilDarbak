// =====================================================
// SahlDarbak - Check-in page
// POST /travel-presence/add  (أول مرة)
// PUT  /travel-presence/update (لو عنده تسجيل وصول قديم)
// =====================================================

document.addEventListener("DOMContentLoaded", () => {

    const userId = requireLogin();

    if (!userId) {
        return;
    }

    const gpsButton = document.getElementById("gpsBtn");
    const manualButton = document.getElementById("manualBtn");


    gpsButton.addEventListener("click", () => {

        if (!navigator.geolocation) {
            showError(t("imGeoUnavailable"));
            return;
        }

        setBtnLoading(gpsButton, true);

        navigator.geolocation.getCurrentPosition(

            position => {
                checkIn(
                    userId,
                    position.coords.latitude,
                    position.coords.longitude,
                    gpsButton
                );
            },

            error => {
                setBtnLoading(gpsButton, false);

                if (error.code === error.PERMISSION_DENIED) {
                    showError(t("imGeoDenied"));
                } else if (error.code === error.TIMEOUT) {
                    showError(t("imGeoTimeout"));
                } else {
                    showError(t("imGeoUnavailable"));
                }
            },

            { enableHighAccuracy: true, timeout: 15000 }
        );
    });


    manualButton.addEventListener("click", () => {

        const lat = parseFloat(document.getElementById("latInput").value);
        const lon = parseFloat(document.getElementById("lonInput").value);

        const valid =
            !Number.isNaN(lat) && !Number.isNaN(lon) &&
            lat >= -90 && lat <= 90 &&
            lon >= -180 && lon <= 180;

        if (!valid) {
            showError(t("imBadCoords"));
            return;
        }

        checkIn(userId, lat, lon, manualButton);
    });

});


async function checkIn(userId, latitude, longitude, button) {

    const body = JSON.stringify({ userId, latitude, longitude });

    setBtnLoading(button, true);

    try {

        try {

            await immerseApi("/travel-presence/add", {
                method: "POST",
                body
            });

        } catch (error) {

            // لو عنده تسجيل وصول سابق نحدّثه بدل ما نفشل
            if (/already|exist/i.test(error.message)) {

                await immerseApi("/travel-presence/update", {
                    method: "PUT",
                    body
                });

            } else {

                throw error;

            }

        }

        showSuccess(t("imCheckedIn"));

        setTimeout(() => {
            window.location.href = "/city";
        }, 900);

    } catch (error) {

        setBtnLoading(button, false);

        // لو الباك اند يطلب بروفايل نوديه يعبيه أول
        if (/profile/i.test(error.message)) {

            showError(t("imNeedProfile"));

            setTimeout(() => {
                window.location.href = "/profile";
            }, 1800);

            return;
        }

        showError(error.message);

    }

}
