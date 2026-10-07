document.addEventListener("DOMContentLoaded", () => {

    const form =
        document.getElementById("registerForm");

    const errorElement =
        document.getElementById("registerError");

    const registerButton =
        document.getElementById("registerButton");


    form.addEventListener("submit", async (event) => {

        event.preventDefault();

        errorElement.textContent = "";


        const email =
            document.getElementById("email")
                .value
                .trim();

        const phoneNumber =
            document.getElementById("phoneNumber")
                .value
                .trim();

        const password =
            document.getElementById("password")
                .value;


        registerButton.disabled = true;

        const originalText =
            registerButton.innerHTML;

        registerButton.innerHTML = `
            <span class="material-symbols-rounded loading-icon">
                progress_activity
            </span>
            جاري إنشاء الحساب...
        `;


        try {

            // =========================
            // REGISTER
            // =========================

            const registerResponse =
                await fetch("/api/v1/user/add", {

                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        email: email,
                        phoneNumber: phoneNumber,
                        password: password
                    })
                });


            const registerData =
                await registerResponse.json();


            if (!registerResponse.ok) {

                throw new Error(
                    registerData.message ||
                    "تعذر إنشاء الحساب"
                );
            }
            window.location.href = "/login";




        } catch (error) {

            errorElement.textContent =
                error.message;

            registerButton.disabled = false;
            registerButton.innerHTML = originalText;
        }

    });

});