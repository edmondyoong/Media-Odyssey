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
/* =========================================================
   RECOMMENDATIONS
   ========================================================= */

let currentMediaType = "MOVIE";

// Fetch recommendations for the selected media type
async function loadRecommendations(mediaType) {
    const statusEl = document.getElementById("recStatus");
    const cardsEl  = document.getElementById("recCards");

    statusEl.textContent = "Loading… wait time is 30 seconds max. Thank you for your patience!";
    cardsEl.innerHTML    = "";

    try {
        const res = await fetch(`/api/recommendations?mediaType=${mediaType}`);

        if (res.status === 401 || res.status === 403) {
            statusEl.textContent = "Please log in to see recommendations.";
            return;
        }

        const data = await res.json();

        if (!data || data.length === 0) {
            statusEl.textContent =
                "No recommendations yet — browse some media first so we can learn your taste!";
            return;
        }

        statusEl.textContent = "";
        renderCards(data);

    } catch (err) {
        statusEl.textContent = "Could not load recommendations.";
        console.error(err);
    }
}

// Build a card for each recommendation
function renderCards(items) {
    const cardsEl = document.getElementById("recCards");

    items.forEach(item => {
        const card = document.createElement("div");
        card.className = "rec-card";
        card.dataset.mediaApiId = item.mediaApiId;
        card.dataset.mediaType  = item.mediaType;
        card.dataset.genre      = item.genre;

        const img = item.imageUrl && item.imageUrl !== "null"
            ? `<img class="rec-img" src="${item.imageUrl}" alt="${item.title}" onerror="this.style.display='none'">`
            : `<div class="rec-img rec-img-placeholder">No Image</div>`;

        card.innerHTML = `
            ${img}
            <div class="rec-info">
                <p class="rec-title">${item.title}</p>
                <p class="rec-meta">${item.genre} · ${item.mediaType === 'SONG' ? item.artist : 'score: ' + item.score.toFixed(1)}</p>
            </div>
            <div class="rec-actions">
                <button class="rec-btn view-btn"  onclick="recordInteraction(this, 'VIEW')">👁 View</button>
                <button class="rec-btn like-btn"  onclick="recordInteraction(this, 'LIKE')">♡ Like</button>
            </div>
        `;

        cardsEl.appendChild(card);
    });
}

// re add buttons when we have interactions working




// POST a VIEW or LIKE interaction to the backend
async function recordInteraction(btn, interactionType) {
    const card       = btn.closest(".rec-card");
    const mediaApiId = card.dataset.mediaApiId;
    const mediaType  = card.dataset.mediaType;
    const genre      = card.dataset.genre;

    try {
        const res = await fetch("/api/recommendations/interactions", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                mediaApiId,
                interactionType,
                mediaType,
                genres: [genre]
            })
        });

        if (res.ok) {
            if (interactionType === "LIKE") {
                btn.textContent = "❤ Liked!";
                btn.classList.add("liked");
                btn.disabled = true;
            } else {
                btn.textContent = "✓ Viewed";
                btn.classList.add("viewed");
                btn.disabled = true;
            }
        } else {
            console.warn("Interaction failed:", res.status);
        }
    } catch (err) {
        console.error("Interaction error:", err);
    }
}

// Tab switching
document.querySelectorAll(".rec-tab").forEach(tab => {
    tab.addEventListener("click", () => {
        document.querySelectorAll(".rec-tab").forEach(t => t.classList.remove("active"));
        tab.classList.add("active");
        currentMediaType = tab.dataset.type;
        loadRecommendations(currentMediaType);
    });
});

// Load on page open
loadRecommendations(currentMediaType);