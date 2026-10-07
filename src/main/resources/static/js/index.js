// =====================================================
// SahlDarbak - Home Page JavaScript
// =====================================================


// =========================
// PAGE ROUTES
// =========================

const routes = {

    travelRequest: "/travel-request",

    destinationRecommendation: "/destination-recommendation",

    myTrips: "/trips",

    travelMatch: "/travel-match",

    community: "/community",

    login: "/login"

};



// =====================================================
// NAVIGATION
// =====================================================

function navigateTo(path) {

    if (!path) {
        return;
    }

    window.location.href = path;

}



// =====================================================
// FETCH HELPER
// =====================================================
// حالياً موجود هنا.
// إذا احتاجه الفريق لاحقاً ننقله إلى base.js.

async function apiRequest(url, options = {}) {

    const defaultOptions = {

        method: "GET",

        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json"
        }

    };


    const finalOptions = {

        ...defaultOptions,

        ...options,

        headers: {
            ...defaultOptions.headers,
            ...(options.headers || {})
        }

    };


    try {

        const response =
            await fetch(url, finalOptions);


        if (!response.ok) {

            let errorMessage =
                "Something went wrong";


            try {

                const errorData =
                    await response.json();


                if (errorData.message) {

                    errorMessage =
                        errorData.message;

                }

            } catch (error) {

                console.error(
                    "Could not parse error response",
                    error
                );

            }


            throw new Error(errorMessage);

        }


        if (response.status === 204) {

            return null;

        }


        return await response.json();


    } catch (error) {

        console.error(
            "API Request Error:",
            error
        );


        throw error;

    }

}



// =====================================================
// BUTTON LOADING
// =====================================================

function setButtonLoading(
    button,
    isLoading,
    loadingText = "جاري التحميل..."
) {

    if (!button) {
        return;
    }


    if (isLoading) {

        button.dataset.originalText =
            button.innerHTML;


        button.disabled = true;


        button.innerHTML = `
            <span class="material-symbols-rounded loading-icon">
                progress_activity
            </span>

            <span>
                ${loadingText}
            </span>
        `;

    } else {

        button.disabled = false;


        if (button.dataset.originalText) {

            button.innerHTML =
                button.dataset.originalText;

        }

    }

}



// =====================================================
// HOME BUTTONS
// =====================================================

function setupHomeButtons() {


    // Start Trip

    const startTripButtons =
        document.querySelectorAll(
            "[data-action='start-trip']"
        );


    startTripButtons.forEach(button => {

        button.addEventListener(
            "click",
            event => {

                event.preventDefault();

                navigateTo(
                    routes.travelRequest
                );

            }
        );

    });



    // Destination Recommendation

    const recommendationButton =
        document.querySelector(
            "[data-action='destination-recommendation']"
        );


    if (recommendationButton) {

        recommendationButton.addEventListener(
            "click",
            event => {

                event.preventDefault();

                navigateTo(
                    routes.destinationRecommendation
                );

            }
        );

    }



    // TravelMatch

    const travelMatchButton =
        document.querySelector(
            "[data-action='travel-match']"
        );


    if (travelMatchButton) {

        travelMatchButton.addEventListener(
            "click",
            event => {

                event.preventDefault();

                navigateTo(
                    routes.travelMatch
                );

            }
        );

    }

}



// =====================================================
// SMOOTH SCROLL
// =====================================================

function setupSmoothScroll() {

    const internalLinks =
        document.querySelectorAll(
            "a[href^='#']"
        );


    internalLinks.forEach(link => {

        link.addEventListener(
            "click",
            event => {

                const targetId =
                    link.getAttribute("href");


                if (
                    !targetId ||
                    targetId === "#"
                ) {

                    return;

                }


                const target =
                    document.querySelector(
                        targetId
                    );


                if (!target) {

                    return;

                }


                event.preventDefault();


                target.scrollIntoView({

                    behavior: "smooth",

                    block: "start"

                });

            }
        );

    });

}



// =====================================================
// HOME DATA
// =====================================================

async function loadHomeData() {

    try {

        /*
        لاحقاً لو احتجنا بيانات حقيقية للـHome:

        const posts =
            await apiRequest(
                "/api/v1/post/get-latest"
            );

        displayPosts(posts);
        */

    } catch (error) {

        console.error(
            "Failed to load home data:",
            error
        );

    }

}



// =====================================================
// INITIALIZE HOME PAGE
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        setupHomeButtons();

        setupSmoothScroll();

        loadHomeData();

    }
);