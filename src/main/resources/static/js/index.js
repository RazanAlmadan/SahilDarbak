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

        const posts =
            await apiRequest(
                "/api/v1/community/get"
            );


        displayCommunityPosts(
            posts
        );


    } catch (error) {

        console.error(
            "Failed to load community posts:",
            error
        );

    }

}



// =====================================================
// DISPLAY COMMUNITY POSTS ON HOME
// =====================================================

function displayCommunityPosts(posts) {

    const postsGrid =
        document.querySelector(
            ".community-section .posts-grid"
        );


    if (!postsGrid) {

        console.error(
            "Community posts grid was not found."
        );

        return;

    }


    /*
     * Remove the fake/demo posts.
     */

    postsGrid.innerHTML = "";


    if (
        !Array.isArray(posts) ||
        posts.length === 0
    ) {

        console.log(
            "No community posts found."
        );

        return;

    }


    /*
     * Home shows the first 3 real posts.
     */

    posts
        .slice(0, 3)
        .forEach(
            post => {

                const card =
                    document.createElement(
                        "article"
                    );


                card.className =
                    "post-card";


                const rating =
                    Number(post.rating) || 0;


                const stars =
                    "★".repeat(rating) +
                    "☆".repeat(5 - rating);


                card.innerHTML = `

                    <div class="post-user">

                        <div class="post-avatar">

                            <span class="material-symbols-rounded">
                                public
                            </span>

                        </div>


                        <div>

                            <strong>
                                ${escapeHomeHtml(
                    post.title ||
                    "Travel Experience"
                )}
                            </strong>


                            <small>

                                ${escapeHomeHtml(
                    post.country || ""
                )}

                                ${
                    post.city
                        ? ` · ${escapeHomeHtml(post.city)}`
                        : ""
                }

                            </small>

                        </div>

                    </div>


                    <p>
                        ${escapeHomeHtml(
                    post.content || ""
                )}
                    </p>


                    <span class="post-like">

                        ★ ${rating}/5

                        <span class="home-post-stars">
                            ${stars}
                        </span>

                    </span>

                `;


                postsGrid.appendChild(
                    card
                );

            }
        );

}



// =====================================================
// SAFE HTML
// =====================================================

function escapeHomeHtml(value) {

    return String(value)

        .replaceAll(
            "&",
            "&amp;"
        )

        .replaceAll(
            "<",
            "&lt;"
        )

        .replaceAll(
            ">",
            "&gt;"
        )

        .replaceAll(
            '"',
            "&quot;"
        )

        .replaceAll(
            "'",
            "&#039;"
        );

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