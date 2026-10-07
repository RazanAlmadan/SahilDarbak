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



// =========================
// INITIALIZE
// =========================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        setupFooterYear();

        setupNavbarScrollEffect();

        setupActiveNavbar();

    }
);