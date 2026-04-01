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
        }); 
    });
});