// =================== Board Display Page =================================================

document.addEventListener('DOMContentLoaded', function() {
    const postsBtn = document.getElementById('postsBtn');
    const mediaBtn = document.getElementById('mediaBtn');
    const postsContainer = document.getElementById('postsContainer');
    const mediaContainer = document.getElementById('mediaContainer');

    // Tab switching 
    if (postsBtn && mediaBtn) {
        postsBtn.addEventListener('click', function() {
            // Update button states
            postsBtn.classList.add('active');
            mediaBtn.classList.remove('active');
            
            // Show/hide containers
            postsContainer.style.display = 'block';
            mediaContainer.style.display = 'none';
        });

        mediaBtn.addEventListener('click', function() {
            // Update button states
            mediaBtn.classList.add('active');
            postsBtn.classList.remove('active');
            
            // Show/hide containers
            mediaContainer.style.display = 'block';
            postsContainer.style.display = 'none';
        });
    }

    const movieDisplay = document.querySelector('.movie-card-display');

    if (movieDisplay) {
        const boardId = window.location.pathname.split('/').pop(); // grabs ID from URL

        // Fetch movies from backend
        fetch(`/boards/display/${boardId}/movies`)
            .then(response => response.json())
            .then(movies => {
                movieDisplay.innerHTML = ''; // clear placeholder

                if (movies.length === 0) {
                    movieDisplay.innerHTML = '<p class="empty-message">No movies found</p>';
                    return;
                }

                movies.forEach(movie => {
                    const card = document.createElement('div');
                    card.classList.add('movie-card');

                    card.innerHTML = `
                        <a href="/mediaView/movie/${movie.id}">
                            <img src="https://image.tmdb.org/t/p/w500${movie.poster_path}" alt="${movie.title}">
                            <h4>${movie.title}</h4>
                        </a>
                    `;
                    movieDisplay.appendChild(card);
                });
            })
            .catch(err => {
                console.error('Error fetching movies:', err);
                movieDisplay.innerHTML = '<p class="empty-message">Failed to load movies.</p>';
            });
    }
});
