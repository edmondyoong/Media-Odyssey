/* Functions to fold and unfold side bar + the logo changes everytime actions are made */
const logoToggle = document.getElementById("logo");
const sideBar = document.getElementById("sideBar"); 

const openLogo = "/images/openLogo.svg";
const closeLogo = "/images/closeLogo.svg"; 

logoToggle.addEventListener("click", ()=>{
    sideBar.classList.toggle("collapsed");

    if(sideBar.classList.contains("collapsed")) {
        logoToggle.src = closeLogo;
    } else {
        logoToggle.src = openLogo; 
    }
}); 