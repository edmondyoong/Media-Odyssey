const avatarDisplayBox = document.querySelectorAll(".avatar-display");
const avatarSelectionBox = document.getElementById("avatarsBox");

avatarDisplayBox.forEach( box => {
    box.addEventListener("click", () => {
        avatarSelectionBox.classList.toggle("hidden");
    });
});