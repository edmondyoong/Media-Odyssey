/*===============================================================================================
    * This JS file is responsible for handling: 
    * 1. Avatar selection (between default and custom)
    * 2. Avatar uploading (uploading a custom avatar to the server and update the display)
    * 
    * Notice: Please put this JS file at the end of userProfile.html 
    * in order for functions to work properly.
==============================================================================================*/

/* Avatar Selection (user choose to use either default or custom avatar) */
const avatarDisplay = document.querySelector(".avatar-display");
const avatarOptions  = document.querySelectorAll(".avatar-option"); 
const avatarSelectionBox = document.getElementById("avatarsBox");

avatarDisplay.addEventListener("click", () => {
    avatarSelectionBox.classList.toggle("hidden");
});

avatarOptions.forEach(option => {
    option.addEventListener("click", () => {
        const selectedSrc = option.getAttribute("src");
        const selectedAvatarType = option.dataset.avatarType; 

        //testing from the console
        console.log("Selected Avatar Type:", selectedAvatarType);

        avatarDisplay.src = selectedSrc;

        //Send the selected avatar type to backend as JSON
        fetch("/user/profile/avatar/selectedType", {
            method: "POST", 
            headers: {
                "Content-Type": "application/json"
            }, 
            body: JSON.stringify({ selectedAvatarType: selectedAvatarType })
        }).then(response => {
            if (!response.ok) {
                throw new Error("Failed to update. Try again later.");
            }
        }).catch(error => {
            console.error("From updating avatar type js:", error);
        });
    });
});

/* Avatar Uploading (user upload a custom avatar to the server and update the display without reloading page) */
const avatarFileInput = document.getElementById("fileUpload");

avatarFileInput.addEventListener("change", () => {
    const file = avatarFileInput.files[0];

    //prevent empty request if errors happen
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    fetch("/user/profile/avatar/upload", {
        method: "POST",
        body: formData

    })
    .then(res => res.json())
    .then(data => {
        document.querySelector(".avatar-display.custom").src = data.customAvatarURL;
        console.log("Avatar uploaded successfully:", data);
    }).catch(error => {
        console.error("From uploading avatar js:", error);
    });
});