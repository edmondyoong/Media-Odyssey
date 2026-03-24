/* === SIDEBAR LOGIC ============
** Open and close sidebar + Logo will change to when sidebar is toggled.
===================================
*/

const logoToggle = document.getElementById("logo");
const sideBar = document.getElementById("sideBar"); 
const content = document.getElementById("mainContent"); 
const formContainer = document.querySelector(".form-container");
const boardContainer = document.querySelector(".board-container");

const openLogo = "/layout/images/openLogo.svg";
const closeLogo = "/layout/images/closeLogo.svg"; 

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