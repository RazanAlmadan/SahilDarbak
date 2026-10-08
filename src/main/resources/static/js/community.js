/* =====================================================
   COMMUNITY PAGE
===================================================== */

const communityUserId =
    localStorage.getItem("userId");


let currentCommunityMode = "all";


document.addEventListener(
    "DOMContentLoaded",
    () => {

        initializeCommunity();

    }
);



/* =====================================================
   INITIALIZE
===================================================== */

function initializeCommunity() {

    setupCommunityEvents();

    setupContentCounter();

    loadAllPosts();

    translateCommunityPage();

    observeLanguageChanges();

}



/* =====================================================
   EVENTS
===================================================== */

function setupCommunityEvents() {

    const allPostsButton =
        document.getElementById("allPostsButton");


    const ratingButton =
        document.getElementById("ratingButton");


    const searchButton =
        document.getElementById("searchButton");


    const countrySearch =
        document.getElementById("countrySearch");


    const clearSearchButton =
        document.getElementById("clearSearchButton");


    const createPostButton =
        document.getElementById("createPostButton");


    const closeModalButton =
        document.getElementById("closeModalButton");


    const cancelPostButton =
        document.getElementById("cancelPostButton");


    const modalOverlay =
        document.getElementById("modalOverlay");


    const postForm =
        document.getElementById("communityPostForm");



    allPostsButton.addEventListener(
        "click",
        () => {

            setCommunityMode("all");

            loadAllPosts();

        }
    );



    ratingButton.addEventListener(
        "click",
        () => {

            setCommunityMode("rating");

            loadTopRatedPosts();

        }
    );



    searchButton.addEventListener(
        "click",
        searchByCountry
    );



    countrySearch.addEventListener(
        "keydown",
        event => {

            if (event.key === "Enter") {

                searchByCountry();

            }

        }
    );



    clearSearchButton.addEventListener(
        "click",
        () => {

            countrySearch.value = "";

            setCommunityMode("all");

            loadAllPosts();

        }
    );



    createPostButton.addEventListener(
        "click",
        openCreatePostModal
    );



    closeModalButton.addEventListener(
        "click",
        closeCreatePostModal
    );



    cancelPostButton.addEventListener(
        "click",
        closeCreatePostModal
    );



    modalOverlay.addEventListener(
        "click",
        closeCreatePostModal
    );



    postForm.addEventListener(
        "submit",
        submitCommunityPost
    );

}



/* =====================================================
   LOAD ALL POSTS
===================================================== */

async function loadAllPosts() {

    showPostsLoading();

    hideSearchState();


    try {

        const response =
            await fetch(
                "/api/v1/community/get"
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load community posts"
            );

        }


        const posts =
            await response.json();


        renderPosts(posts);

    }

    catch (error) {

        console.error(
            "Community posts error:",
            error
        );


        renderPosts([]);

    }

    finally {

        hidePostsLoading();

    }

}



/* =====================================================
   LOAD TOP RATED
===================================================== */

async function loadTopRatedPosts() {

    showPostsLoading();

    hideSearchState();


    try {

        const response =
            await fetch(
                "/api/v1/community/get/by/rating"
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load rated posts"
            );

        }


        const posts =
            await response.json();


        renderPosts(posts);

    }

    catch (error) {

        console.error(
            "Rated posts error:",
            error
        );


        renderPosts([]);

    }

    finally {

        hidePostsLoading();

    }

}



/* =====================================================
   SEARCH BY COUNTRY
===================================================== */

async function searchByCountry() {

    const input =
        document.getElementById(
            "countrySearch"
        );


    const country =
        input.value.trim();


    if (!country) {

        setCommunityMode("all");

        loadAllPosts();

        return;

    }


    showPostsLoading();


    try {

        const encodedCountry =
            encodeURIComponent(country);


        const response =
            await fetch(
                `/api/v1/community/country/${encodedCountry}`
            );


        if (!response.ok) {

            throw new Error(
                "No posts found"
            );

        }


        const posts =
            await response.json();


        showSearchState(country);

        renderPosts(posts);

    }

    catch (error) {

        console.error(
            "Country search error:",
            error
        );


        showSearchState(country);

        renderPosts([]);

    }

    finally {

        hidePostsLoading();

    }

}



/* =====================================================
   RENDER POSTS
===================================================== */

function renderPosts(posts) {

    const grid =
        document.getElementById(
            "postsGrid"
        );


    const emptyState =
        document.getElementById(
            "emptyState"
        );


    grid.innerHTML = "";


    if (!Array.isArray(posts) || posts.length === 0) {

        emptyState.style.display =
            "block";

        return;

    }


    emptyState.style.display =
        "none";


    posts.forEach(
        (post, index) => {

            grid.appendChild(
                createPostCard(
                    post,
                    index
                )
            );

        }
    );

}



/* =====================================================
   CREATE POST CARD
===================================================== */

function createPostCard(
    post,
    index
) {

    const article =
        document.createElement("article");


    article.className =
        "community-post-card";


    const rating =
        Number(post.rating) || 0;


    const stars =
        "★".repeat(rating) +
        "☆".repeat(5 - rating);


    article.innerHTML = `

        <div class="post-location">

            <div class="post-location-main">

                <span class="material-symbols-rounded">
                    public
                </span>

                <span>
                    ${escapeHtml(post.country || "")}
                </span>

            </div>


            <div class="post-rating">

                <span>
                    ${stars}
                </span>

                <span>
                    ${rating}/5
                </span>

            </div>

        </div>


        <h3>
            ${escapeHtml(post.title || "")}
        </h3>


        <p class="post-content">
            ${escapeHtml(post.content || "")}
        </p>


        <div class="post-footer">

            <div class="post-city">

                <span class="material-symbols-rounded">
                    location_on
                </span>

                <span>
                    ${escapeHtml(post.city || "")}
                </span>

            </div>


            <span class="post-number">
                #${index + 1}
            </span>

        </div>

    `;


    return article;

}



/* =====================================================
   CREATE POST
===================================================== */

async function submitCommunityPost(
    event
) {

    event.preventDefault();


    const title =
        document
            .getElementById("postTitle")
            .value
            .trim();


    const content =
        document
            .getElementById("postContent")
            .value
            .trim();


    const country =
        document
            .getElementById("postCountry")
            .value
            .trim();


    const city =
        document
            .getElementById("postCity")
            .value
            .trim();


    const rating =
        Number(
            document
                .getElementById("postRating")
                .value
        );


    const errorBox =
        document.getElementById(
            "postError"
        );


    const successBox =
        document.getElementById(
            "postSuccess"
        );


    errorBox.style.display =
        "none";

    successBox.style.display =
        "none";



    if (!communityUserId) {

        showPostError(
            getCommunityText(
                "communityLoginRequired"
            )
        );

        return;

    }



    if (
        !title ||
        !content ||
        !country ||
        !city ||
        !rating
    ) {

        showPostError(
            getCommunityText(
                "communityFillFields"
            )
        );

        return;

    }



    const submitButton =
        document.getElementById(
            "submitPostButton"
        );


    submitButton.disabled =
        true;


    try {

        const response =
            await fetch(
                `/api/v1/community/post/${communityUserId}`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({

                        title:
                        title,

                        content:
                        content,

                        country:
                        country,

                        city:
                        city,

                        rating:
                        rating

                    })
                }
            );


        const data =
            await response.json()
                .catch(() => null);



        if (!response.ok) {

            const message =
                data?.message ||
                data?.error ||
                getCommunityText(
                    "communityPostFailed"
                );


            throw new Error(
                message
            );

        }



        successBox.style.display =
            "flex";


        document
            .getElementById(
                "communityPostForm"
            )
            .reset();


        document
            .getElementById(
                "contentCount"
            )
            .textContent = "0";


        setTimeout(
            () => {

                closeCreatePostModal();

                loadAllPosts();

            },
            1200
        );

    }

    catch (error) {

        console.error(
            "Create post error:",
            error
        );


        showPostError(
            error.message ||
            getCommunityText(
                "communityPostFailed"
            )
        );

    }

    finally {

        submitButton.disabled =
            false;

    }

}



/* =====================================================
   MODAL
===================================================== */

function openCreatePostModal() {

    const modal =
        document.getElementById(
            "createPostModal"
        );


    modal.classList.add("open");

    modal.setAttribute(
        "aria-hidden",
        "false"
    );


    document.body.style.overflow =
        "hidden";

}



function closeCreatePostModal() {

    const modal =
        document.getElementById(
            "createPostModal"
        );


    modal.classList.remove("open");

    modal.setAttribute(
        "aria-hidden",
        "true"
    );


    document.body.style.overflow =
        "";



    document
        .getElementById(
            "postError"
        )
        .style.display = "none";


    document
        .getElementById(
            "postSuccess"
        )
        .style.display = "none";

}



/* =====================================================
   CONTENT COUNTER
===================================================== */

function setupContentCounter() {

    const content =
        document.getElementById(
            "postContent"
        );


    const counter =
        document.getElementById(
            "contentCount"
        );


    content.addEventListener(
        "input",
        () => {

            counter.textContent =
                content.value.length;

        }
    );

}



/* =====================================================
   UI STATES
===================================================== */

function showPostsLoading() {

    document
        .getElementById(
            "postsLoading"
        )
        .style.display = "flex";


    document
        .getElementById(
            "postsGrid"
        )
        .style.display = "none";


    document
        .getElementById(
            "emptyState"
        )
        .style.display = "none";

}



function hidePostsLoading() {

    document
        .getElementById(
            "postsLoading"
        )
        .style.display = "none";


    document
        .getElementById(
            "postsGrid"
        )
        .style.display = "grid";

}



function showSearchState(
    country
) {

    const state =
        document.getElementById(
            "searchState"
        );


    document
        .getElementById(
            "searchStateText"
        )
        .textContent =
        `${getCommunityText("communitySearchResult")} ${country}`;


    state.style.display =
        "flex";

}



function hideSearchState() {

    document
        .getElementById(
            "searchState"
        )
        .style.display =
        "none";

}



function setCommunityMode(
    mode
) {

    currentCommunityMode =
        mode;


    document
        .getElementById(
            "allPostsButton"
        )
        .classList.toggle(
        "active",
        mode === "all"
    );


    document
        .getElementById(
            "ratingButton"
        )
        .classList.toggle(
        "active",
        mode === "rating"
    );

}



/* =====================================================
   ERRORS
===================================================== */

function showPostError(
    message
) {

    const errorBox =
        document.getElementById(
            "postError"
        );


    errorBox.textContent =
        message;


    errorBox.style.display =
        "flex";

}



/* =====================================================
   SAFE HTML
===================================================== */

function escapeHtml(
    value
) {

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");

}



/* =====================================================
   PAGE TRANSLATIONS
===================================================== */

const communityTranslations = {

    ar: {

        communityPageTag:
            "مجتمع المسافرين",

        communityPageTitle:
            "خذها من ناس راحوا قبلك 🌍",

        communityPageDescription:
            "تجارب حقيقية من مسافرين تساعدك تعرف وش ينتظرك قبل رحلتك.",

        communitySearchPlaceholder:
            "ابحث باسم الدولة...",

        search:
            "بحث",

        communityAllPosts:
            "كل التجارب",

        communityTopRated:
            "الأعلى تقييماً",

        communityCreatePost:
            "أضف تجربتك",

        communityClearSearch:
            "إلغاء البحث",

        communityLoading:
            "نحضر لك تجارب المسافرين...",

        communityEmptyTitle:
            "ما لقينا تجارب هنا",

        communityEmptyText:
            "جرّب دولة ثانية أو كن أول شخص يشارك تجربته.",

        communityShareExperience:
            "شارك تجربتك",

        communityCreateTitle:
            "خل المسافرين يستفيدون من تجربتك ✨",

        communityPostTitle:
            "عنوان التجربة",

        communityTitlePlaceholder:
            "مثلاً: أفضل وقت لزيارة كابادوكيا",

        communityCountry:
            "الدولة",

        communityCountryPlaceholder:
            "مثلاً: تركيا",

        communityCity:
            "المدينة",

        communityCityPlaceholder:
            "مثلاً: كابادوكيا",

        communityRating:
            "تقييم التجربة",

        communityChooseRating:
            "اختر التقييم",

        communityContent:
            "تجربتك",

        communityContentPlaceholder:
            "وش الشيء اللي تتمنى أحد قاله لك قبل ما تسافر؟",

        communityPostSuccess:
            "تمت إضافة تجربتك بنجاح 🎉",

        communityPublish:
            "نشر التجربة",

        cancel:
            "إلغاء",

        communityLoginRequired:
            "لازم تسجل دخول قبل ما تنشر تجربة.",

        communityFillFields:
            "فضلاً عبّ كل الحقول.",

        communityPostFailed:
            "ما قدرنا ننشر التجربة. حاول مرة ثانية.",

        communitySearchResult:
            "نتائج البحث عن:",

    },


    en: {

        communityPageTag:
            "Travel Community",

        communityPageTitle:
            "Learn from travelers who have been there 🌍",

        communityPageDescription:
            "Real travel experiences from people who can help you know what to expect before your trip.",

        communitySearchPlaceholder:
            "Search by country...",

        search:
            "Search",

        communityAllPosts:
            "All experiences",

        communityTopRated:
            "Top rated",

        communityCreatePost:
            "Share your experience",

        communityClearSearch:
            "Clear search",

        communityLoading:
            "Bringing you travelers' experiences...",

        communityEmptyTitle:
            "No experiences found",

        communityEmptyText:
            "Try another country or be the first to share an experience.",

        communityShareExperience:
            "Share your experience",

        communityCreateTitle:
            "Help other travelers with your experience ✨",

        communityPostTitle:
            "Experience title",

        communityTitlePlaceholder:
            "Example: Best time to visit Cappadocia",

        communityCountry:
            "Country",

        communityCountryPlaceholder:
            "Example: Turkey",

        communityCity:
            "City",

        communityCityPlaceholder:
            "Example: Cappadocia",

        communityRating:
            "Experience rating",

        communityChooseRating:
            "Choose a rating",

        communityContent:
            "Your experience",

        communityContentPlaceholder:
            "What do you wish someone had told you before your trip?",

        communityPostSuccess:
            "Your experience was shared successfully 🎉",

        communityPublish:
            "Publish experience",

        cancel:
            "Cancel",

        communityLoginRequired:
            "Please log in before sharing an experience.",

        communityFillFields:
            "Please fill in all fields.",

        communityPostFailed:
            "We couldn't publish your experience. Please try again.",

        communitySearchResult:
            "Search results for:",

    }

};



function getCommunityText(
    key
) {

    const language =
        document.documentElement.lang === "en"
            ? "en"
            : "ar";


    return (
        communityTranslations[language]?.[key]
        ||
        communityTranslations.ar[key]
        ||
        key
    );

}



function translateCommunityPage() {

    const language =
        document.documentElement.lang === "en"
            ? "en"
            : "ar";


    const dictionary =
        communityTranslations[language];


    document
        .querySelectorAll("[data-i18n]")
        .forEach(element => {

            const key =
                element.dataset.i18n;


            if (dictionary[key]) {

                element.textContent =
                    dictionary[key];

            }

        });


    document
        .querySelectorAll(
            "[data-i18n-placeholder]"
        )
        .forEach(element => {

            const key =
                element.dataset.i18nPlaceholder;


            if (dictionary[key]) {

                element.placeholder =
                    dictionary[key];

            }

        });

}



/* =====================================================
   LANGUAGE CHANGE SUPPORT
===================================================== */

function observeLanguageChanges() {

    const html =
        document.documentElement;


    const observer =
        new MutationObserver(
            mutations => {

                mutations.forEach(
                    mutation => {

                        if (
                            mutation.attributeName === "lang"
                        ) {

                            translateCommunityPage();

                        }

                    }
                );

            }
        );


    observer.observe(
        html,
        {
            attributes: true,
            attributeFilter: ["lang"]
        }
    );

}