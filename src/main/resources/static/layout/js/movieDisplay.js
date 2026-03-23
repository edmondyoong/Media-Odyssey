/* ===========================================
          Star Rating System Functions:
=============================================*/
const stars = document.querySelectorAll('.star');
let currentRating = 0;

stars.forEach((star, index) => {
  star.addEventListener('click', () => {
    currentRating = index + 1;
    updateStars();
  });

  star.addEventListener('mouseenter', () => {
    highlightStars(index + 1);
  });
});

document.getElementById('star-rating').addEventListener('mouseleave', () => {
  highlightStars(currentRating);
});

function highlightStars(count) {
  stars.forEach((star, index) => {
    star.classList.toggle('active', index < count);
  });
}

function updateStars() {
  highlightStars(currentRating);
}

// Like Button Toggle
/*
const likeBtn = document.getElementById('like-btn');
likeBtn.addEventListener('click', () => {
  likeBtn.classList.toggle('active');
});

// Watched Button Toggle
const watchedBtn = document.getElementById('watched-btn');
const watchedIcon = document.getElementById('watched-icon');
const watchedSvg = document.getElementById('watched-svg');

// Hide SVG if custom image is provided
if (watchedIcon.src && watchedIcon.src !== window.location.href) {
  watchedSvg.style.display = 'none';
} else {
  watchedIcon.style.display = 'none';
}

watchedBtn.addEventListener('click', () => {
  watchedBtn.classList.toggle('watched-active');
});
*/

/*======================================================== 
                  ADD MOVIE INTO BOARD JS
                      DROP DOWN MENU
==========================================================*/
// Dropdown Toggle
const dropdownBtn = document.getElementById('dropdown-btn');
const dropdownMenu = document.getElementById('dropdown-menu');

dropdownBtn.addEventListener('click', () => {
  dropdownBtn.classList.toggle('open');
  dropdownMenu.classList.toggle('show');
});

// Close dropdown when clicking outside
document.addEventListener('click', (e) => {
  if (!dropdownBtn.contains(e.target) && !dropdownMenu.contains(e.target)) {
    dropdownBtn.classList.remove('open');
    dropdownMenu.classList.remove('show');
  }
});

// Handle adding media
document.addEventListener("DOMContentLoaded", function(){
  const movieId = document.getElementById("movieApiId").value;
  const dropdownItems = document.querySelectorAll(".dropdown-item");

  dropdownItems.forEach(button => {
    button.addEventListener("click", function() {
      const boardId = this.dataset.board_id;

      fetch(`api/boards/${boardId}/media?mediaApiId=${movieId}`, {
        method : "POST", 
        headers: {"Content-Type": "application/x-www-form-urlencoded"}
      })
      .then(response => {
        if(response.ok()) {
          alert("Movie is added"); 
        } else { alert("Movie is already added in this board or error occured."); }
      })
      .catch (error => {
        console.error("Error:", error );
        aler ("Server error. Please log in again.");
      });
    });
  });
});

// Dropdown item click handler
document.querySelectorAll('.dropdown-item').forEach(item => {
  item.addEventListener('click', () => {
    const text = item.textContent.trim();
    alert(`Added to "${text}"!`);
    dropdownBtn.classList.remove('open');
    dropdownMenu.classList.remove('show');
  });
});
