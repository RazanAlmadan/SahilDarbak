// =====================================================
// SahlDarbak - Shared JavaScript
// Navbar + Footer + shared UI
// =====================================================


// =========================
// FOOTER YEAR
// =========================

function setupFooterYear() {

    const yearElement =
        document.getElementById("currentYear");


    if (!yearElement) {
        return;
    }


    yearElement.textContent =
        new Date().getFullYear();

}



// =========================
// NAVBAR SCROLL
// =========================

function setupNavbarScrollEffect() {

    const navbar =
        document.querySelector(".navbar");


    if (!navbar) {
        return;
    }


    function updateNavbar() {

        if (window.scrollY > 40) {

            navbar.classList.add(
                "navbar-scrolled"
            );

        } else {

            navbar.classList.remove(
                "navbar-scrolled"
            );

        }

    }


    updateNavbar();


    window.addEventListener(
        "scroll",
        updateNavbar
    );

}



// =========================
// ACTIVE NAVBAR LINK
// =========================

function setupActiveNavbar() {

    const links =
        document.querySelectorAll(
            ".navbar-links a"
        );


    links.forEach(link => {

        link.addEventListener(
            "click",
            () => {

                links.forEach(item => {

                    item.classList.remove(
                        "active"
                    );

                });


                link.classList.add(
                    "active"
                );

            }
        );

    });

}

function setupLogout() {

    const logoutButton =
        document.getElementById("logoutBtn");

    if (!logoutButton) return;

    logoutButton.addEventListener("click", () => {

        localStorage.removeItem("userId");
        localStorage.removeItem("userEmail");

        window.location.href = "/login";
    });
}



function setupNavbarAuthState() {

    const userId =
        localStorage.getItem("userId");

    const guestNavbar =
        document.getElementById("guestNavbar");

    const userNavbar =
        document.getElementById("userNavbar");


    if (!guestNavbar || !userNavbar) {
        return;
    }


    if (userId) {

        guestNavbar.style.display = "none";
        userNavbar.style.display = "flex";

    } else {

        guestNavbar.style.display = "flex";
        userNavbar.style.display = "none";

    }
}
// =========================
// INITIALIZE
// =========================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        setupFooterYear();

        setupNavbarScrollEffect();

        setupActiveNavbar();
        setupLogout();
        setupNavbarAuthState();



    }
);
