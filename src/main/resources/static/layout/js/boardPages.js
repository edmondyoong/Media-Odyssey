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
});
