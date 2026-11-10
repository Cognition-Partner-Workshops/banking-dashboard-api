/*
 * Adds a dark/light theme toggle button to the Swagger UI topbar (next to
 * "Explore") and remembers the choice in localStorage. The dark theme itself is
 * the stylesheet injected with id "swagger-dark-theme"; toggling simply enables
 * or disables that stylesheet.
 */
(function () {
  "use strict";

  var STORAGE_KEY = "swagger-ui-theme";
  var toggleButton = null;

  function darkLink() {
    return document.getElementById("swagger-dark-theme");
  }

  function currentTheme() {
    return localStorage.getItem(STORAGE_KEY) || "dark";
  }

  function updateButton(theme) {
    if (!toggleButton) {
      return;
    }
    if (theme === "dark") {
      toggleButton.textContent = "\u2600 Light";
      toggleButton.title = "Switch to light theme";
    } else {
      toggleButton.textContent = "\u263D Dark";
      toggleButton.title = "Switch to dark theme";
    }
  }

  function applyTheme(theme) {
    var link = darkLink();
    if (link) {
      link.disabled = theme !== "dark";
    }
    localStorage.setItem(STORAGE_KEY, theme);
    updateButton(theme);
  }

  function createButton() {
    var btn = document.createElement("button");
    btn.id = "theme-toggle";
    btn.type = "button";
    btn.style.cssText = [
      "margin-left:10px",
      "padding:0 18px",
      "height:40px",
      "border-radius:4px",
      "border:2px solid #ffffff",
      "background:transparent",
      "color:#ffffff",
      "font-family:sans-serif",
      "font-size:14px",
      "font-weight:700",
      "cursor:pointer",
      "white-space:nowrap"
    ].join(";");
    btn.addEventListener("click", function () {
      applyTheme(currentTheme() === "dark" ? "light" : "dark");
    });
    return btn;
  }

  // Apply the saved theme as soon as the stylesheet element is available.
  var themeTimer = setInterval(function () {
    if (darkLink()) {
      applyTheme(currentTheme());
      clearInterval(themeTimer);
    }
  }, 50);

  // Swagger UI renders the topbar asynchronously; poll until it exists.
  var insertTimer = setInterval(function () {
    var wrapper = document.querySelector(".topbar .download-url-wrapper");
    if (wrapper && !document.getElementById("theme-toggle")) {
      toggleButton = createButton();
      wrapper.appendChild(toggleButton);
      updateButton(currentTheme());
      clearInterval(insertTimer);
    }
  }, 100);

  setTimeout(function () {
    clearInterval(insertTimer);
  }, 15000);
})();
