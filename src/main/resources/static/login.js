"use strict";

document.addEventListener("DOMContentLoaded", async () => {
    document.getElementById("currentYear").textContent = new Date().getFullYear();

    const message = document.getElementById("formMessage");
    const parameters = new URLSearchParams(window.location.search);
    if (parameters.has("error")) {
        message.textContent = "The username or password was not recognized. Please try again.";
        message.classList.add("visible");
    } else if (parameters.has("logout")) {
        message.textContent = "You have been signed out securely.";
        message.classList.add("visible");
    } else if (parameters.has("expired")) {
        message.textContent = "Your session has expired. Sign in again to continue.";
        message.classList.add("visible");
    }

    try {
        const response = await fetch("/csrf", {
            credentials: "same-origin",
            headers: { Accept: "application/json" }
        });
        if (!response.ok) throw new Error("Unable to initialize secure sign-in.");
        const csrf = await response.json();
        document.getElementById("csrfToken").value = csrf.token;
        document.getElementById("signInButton").disabled = false;
    } catch (error) {
        message.textContent = "Secure sign-in could not be initialized. Reload the page and try again.";
        message.classList.add("visible");
    }
});
