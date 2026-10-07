document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("loginForm");
    const errorElement = document.getElementById("loginError");
    const loginButton = document.getElementById("loginButton");

    form.addEventListener("submit", async (event) => {

        event.preventDefault();

        errorElement.textContent = "";

        const email =
            document.getElementById("email").value.trim();

        const password =
            document.getElementById("password").value;

        loginButton.disabled = true;

        const originalText = loginButton.innerHTML;

        loginButton.innerHTML = `
            <span class="material-symbols-rounded loading-icon">
                progress_activity
            </span>
            جاري الدخول...
        `;

        try {

            const response = await fetch("/api/v1/user/login", {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    email: email,
                    password: password
                })
            });


            const data = await response.json();


            if (!response.ok) {

                throw new Error(
                    data.message || "تعذر تسجيل الدخول"
                );
            }


            localStorage.setItem(
                "userId",
                data.userId
            );

            localStorage.setItem(
                "userEmail",
                data.email
            );


            window.location.href = "/dashboard";


        } catch (error) {

            errorElement.textContent =
                error.message;

            loginButton.disabled = false;
            loginButton.innerHTML = originalText;
        }

    });

});