/* === SIDEBAR LOGIC ============
** Open and close sidebar + Logo will change to when sidebar is toggled.
===================================
*/

const logoToggle = document.getElementById("logo");
const sideBar = document.getElementById("sideBar"); 
const content = document.getElementById("mainContent"); 
const formContainer = document.querySelector(".form-container");
const boardContainer = document.querySelector(".board-container");

const openLogo = "/layout/images/LogoSidebar/openLogo.svg";
const closeLogo = "/layout/images/LogoSidebar/closeLogo.svg"; 

logoToggle.addEventListener("click", ()=>{
    sideBar.classList.toggle("collapsed");

    if(sideBar.classList.contains("collapsed")) {
        logoToggle.src = closeLogo;

        if(mainContent) mainContent.classList.add("sidebar-collapsed");
        if(formContainer) formContainer.classList.add("sidebar-collaped");
        if(boardContainer) boardContainer.classList.add("sidebar-collapsed");
    } else {
        logoToggle.src = openLogo; 

        if(mainContent) mainContent.classList.remove("sidebar-collapsed");
        if(formContainer) formContainer.classList.remove("sidebar-collaped");
        if(boardContainer) boardContainer.classList.remove("sidebar-collapsed");
    }
}); 

/* === HEADER LOGIC ============
** avatar shows box of profile + settings
===================================
*/

const avatar = document.getElementById("avatarBox"); 
const settingsBox = document.getElementById("settingsBox");

// opens the settings box when avatar is clicked on
avatar.addEventListener("click", function(e){
    e.stopPropagation(); 
    settingsBox.classList.toggle("hidden"); 
});

// settings box can be closed when users click anywhere on the page (exception the box itself)
document.addEventListener("click", function() {
    settingsBox.classList.add("hidden");
});

// prevents the box from closing when users clicked inside the box
settingsBox.addEventListener("click", function(e) {
    e.stopPropagation();
    settingsBox.classList.remove("hidden");
});