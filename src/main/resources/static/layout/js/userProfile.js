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
const avatarSelectionBox = document.getElementById("avatarSelectionBox");
// const storedAvatarsDisplay = document.getElementById("storedAvatarsDisplay");

// The box that displays the avatar options will be toggled (open + closed) 
// when users click on the main avatar display.
/* Toggle when clicking avatar */
avatarDisplay.addEventListener("click", (e) => {
    e.stopPropagation(); // prevent bubbling to document
    avatarSelectionBox.classList.toggle("hidden");
});

/* Prevent clicks INSIDE box from closing it */
avatarSelectionBox.addEventListener("click", (e) => {
    e.stopPropagation();
});

/* Close when clicking anywhere else */
document.addEventListener("click", () => {
    avatarSelectionBox.classList.add("hidden");
});

// Response when user chose which avatar to use. 
// This will return a json body of String ("default" or "custom") to the backend, 
// which will be used to update the database.
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

/* Avatar Uploading (user upload a custom avatar to the server and update the display) */
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
    .then( data => {
        const customAvatarURL = data.customAvatarURL;

        console.log("Avatar uploaded successfully:", data);

        // Update the main avatar display on html immediately after successful upload. 
        avatarDisplay.src = customAvatarURL;

        // find the already existing custom avatar option and update it.
        // Logic: if exists, replace. Otherwise, show. This is by UI design choice.
        let customAvatarOption = document.querySelector(".avatar-option[data-avatar-type='custom']");
        if (customAvatarOption) { 
            customAvatarOption.src = customAvatarURL;
        } else {
            // Create only if there has not been a custom avatar uploaded before. (most likely new users)
            const customAvatarOption = document.createElement("img"); 
            customAvatarOption.src = customAvatarURL;
            customAvatarOption.alt = "Custom Avatar";
            customAvatarOption.classList.add("avatar-option");
            customAvatarOption.dataset.avatarType = "custom";

            // add click behavior
            customAvatarOption.addEventListener("click", () => {
                avatarDisplay.src = customAvatarURL;

                fetch("/user/profile/avatar/selectedType", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ selectedAvatarType: "custom" })
                });
            });
            document.getElementById("storedAvatarsDisplay").prepend(customAvatarOption);
        }
    })
    .then(data => {
        console.log("Avatar uploaded successfully:", data);
    })
    .catch(error => {
        console.error("From uploading avatar js:", error);
    });
});