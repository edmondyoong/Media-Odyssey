package com.mo.mediaodyssey.community.favorite;

import com.mo.mediaodyssey.auth.repository.UserRepository;
import com.mo.mediaodyssey.recommendation.InteractionRequest;
import com.mo.mediaodyssey.recommendation.UserInteraction;
import com.mo.mediaodyssey.recommendation.UserInteractionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller for the Community Favourites / Trending page (/community).
 *
 * Ranking sources:
 *   - UserInteraction table → Top 10 by score, Top 5 trending
 *   - Media table           → Top Rated, rating persistence
 *
 * Like and View interactions are now persisted into UserInteraction,
 * so they feed directly into the ranking system.
 *
 * Session attributes used for duplicate prevention:
 *   - "likedIds"  : Set<String> of "mediaType:mediaApiId" liked this session
 *   - "viewedIds" : Set<String> of "mediaType:mediaApiId" viewed this session
 *   - "ratedIds"  : Set<String> of "mediaType:mediaApiId" rated this session
 */
@Controller
@RequestMapping("/community")
public class CommunityController {

    private final MediaRankingService mediaRankingService;
    private final UserInteractionRepository userInteractionRepository;
    private final UserRepository userRepository;

    public CommunityController(MediaRankingService mediaRankingService,
                                UserInteractionRepository userInteractionRepository,
                                UserRepository userRepository) {
        this.mediaRankingService = mediaRankingService;
        this.userInteractionRepository = userInteractionRepository;
        this.userRepository = userRepository;
    }

    // ── Helper: get logged-in user ID ─────────────────────────────────────────

    /**
     * Resolves the logged-in user's database ID from the Spring Security Authentication.
     * Returns null if the user is not authenticated.
     */
    private Long getUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByEmail(auth.getName())
                .map(u -> u.getId())
                .orElse(null);
    }

    // ── Helper: save interaction to UserInteraction table ─────────────────────

    /**
     * Persists a LIKE or VIEW interaction into the UserInteraction table.
     * This feeds directly into the Community Favourites ranking system.
     *
     * @param userId          logged-in user's ID
     * @param mediaApiId      external API ID of the media item
     * @param mediaType       "MOVIE", "GAME", or "SONG"
     * @param interactionType "LIKE" or "VIEW"
     */
    private void saveInteraction(Long userId, String mediaApiId,
                                  String mediaType, String interactionType) {
        if (userId == null) return; // skip if not logged in

        UserInteraction interaction = new UserInteraction();
        interaction.setUserId(userId);
        interaction.setMediaApiId(mediaApiId);
        interaction.setMediaType(mediaType);
        interaction.setInteractionType(interactionType);
        interaction.setTimestamp(LocalDateTime.now());
        interaction.setGenres(List.of()); // genres not needed for ranking
        userInteractionRepository.save(interaction);
    }

    // ── 1. Community Favourites Main Page ─────────────────────────────────────

    /**
     * Loads the Community Favourites page.
     *
     * Query parameters:
     *   - category : "MOVIE" | "GAME" | "SONG" | null (default = all)
     *   - sort     : "score" | "rating" (default = score)
     */
    @GetMapping
    public String communityPage(Model model,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false, defaultValue = "score") String sort) {

        String normalizedSort = (sort == null || sort.isBlank()) ? "score" : sort.toLowerCase();

        List<RankedMediaResponse> mediaList;

        if ("rating".equals(normalizedSort)) {
            mediaList = (category != null && !category.isBlank())
                    ? mediaRankingService.getTop10ByRatingAndMediaType(category)
                    : mediaRankingService.getTop10ByRating();
        } else {
            mediaList = (category != null && !category.isBlank())
                    ? mediaRankingService.getTop10ByMediaType(category)
                    : mediaRankingService.getTop10();
        }

        List<RankedMediaResponse> trending = mediaRankingService.getTop5Trending();

        model.addAttribute("mediaList",    mediaList);
        model.addAttribute("trending",     trending);
        model.addAttribute("currentCat",   category);
        model.addAttribute("currentSort",  normalizedSort);

        return "boardsLayout/features/trending";
    }

    // ── 2. Like Media (AJAX) ──────────────────────────────────────────────────

    /**
     * Records a LIKE interaction for the specified media item.
     *
     * - Session-based duplicate prevention (one like per item per session)
     * - If user is logged in, persists the interaction into UserInteraction table
     *   so it feeds into the Top 10 ranking
     *
     * Returns JSON:
     *   - liked      : true if accepted, false if already liked this session
     *   - mediaApiId : the media item ID
     */
    @PostMapping("/like/{mediaApiId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> likeMedia(@PathVariable String mediaApiId,
                                                          @RequestParam String mediaType,
                                                          HttpSession session,
                                                          Authentication auth) {
        String sessionKey = mediaType + ":" + mediaApiId;

        @SuppressWarnings("unchecked")
        Set<String> likedIdsRaw = (Set<String>) session.getAttribute("likedIds");
        if (likedIdsRaw == null) {
            likedIdsRaw = new HashSet<>();
            session.setAttribute("likedIds", likedIdsRaw);
        }
        final Set<String> likedIds = likedIdsRaw;

        if (likedIds.contains(sessionKey)) {
            return ResponseEntity.ok(Map.of(
                    "liked",      false,
                    "mediaApiId", mediaApiId
            ));
        }

        // Persist into UserInteraction so ranking updates
        saveInteraction(getUserId(auth), mediaApiId, mediaType, "LIKE");

        likedIds.add(sessionKey);
        session.setAttribute("likedIds", likedIds);

        return ResponseEntity.ok(Map.of(
                "liked",      true,
                "mediaApiId", mediaApiId
        ));
    }

    // ── 3. Increment View Count (AJAX) ────────────────────────────────────────

    /**
     * Records a VIEW interaction for the specified media item.
     * Called automatically by JavaScript when the page loads.
     *
     * - Session-based duplicate prevention (one view per item per session)
     * - If user is logged in, persists the interaction into UserInteraction table
     *
     * Returns JSON:
     *   - viewed     : true if accepted, false if already viewed this session
     *   - mediaApiId : the media item ID
     */
    @PostMapping("/view/{mediaApiId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incrementView(@PathVariable String mediaApiId,
                                                              @RequestParam String mediaType,
                                                              HttpSession session,
                                                              Authentication auth) {
        String sessionKey = mediaType + ":" + mediaApiId;

        @SuppressWarnings("unchecked")
        Set<String> viewedIdsRaw = (Set<String>) session.getAttribute("viewedIds");
        if (viewedIdsRaw == null) {
            viewedIdsRaw = new HashSet<>();
            session.setAttribute("viewedIds", viewedIdsRaw);
        }
        final Set<String> viewedIds = viewedIdsRaw;

        if (viewedIds.contains(sessionKey)) {
            return ResponseEntity.ok(Map.of(
                    "viewed",     false,
                    "mediaApiId", mediaApiId
            ));
        }

        // Persist into UserInteraction so ranking updates
        saveInteraction(getUserId(auth), mediaApiId, mediaType, "VIEW");

        viewedIds.add(sessionKey);
        session.setAttribute("viewedIds", viewedIds);

        return ResponseEntity.ok(Map.of(
                "viewed",     true,
                "mediaApiId", mediaApiId
        ));
    }

    // ── 4. Submit Star Rating (AJAX) ──────────────────────────────────────────

    /**
     * Records a star rating (1–5) for the specified media item.
     *
     * - Session-based duplicate prevention (one rating per item per session)
     * - Persists ratingSum and ratingCount into the Media table
     *
     * Returns JSON:
     *   - rated      : true if accepted, false if already rated this session
     *   - mediaApiId : the media item ID
     *   - stars      : the submitted rating value
     */
    @PostMapping("/rate/{mediaApiId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rateMedia(@PathVariable String mediaApiId,
                                                          @RequestParam int stars,
                                                          @RequestParam String mediaType,
                                                          HttpSession session) {
        if (stars < 1 || stars > 5) {
            return ResponseEntity.badRequest().body(Map.of(
                    "rated",   false,
                    "message", "stars must be between 1 and 5"
            ));
        }

        String sessionKey = mediaType + ":" + mediaApiId;

        @SuppressWarnings("unchecked")
        Set<String> ratedIdsRaw = (Set<String>) session.getAttribute("ratedIds");
        if (ratedIdsRaw == null) {
            ratedIdsRaw = new HashSet<>();
            session.setAttribute("ratedIds", ratedIdsRaw);
        }
        final Set<String> ratedIds = ratedIdsRaw;

        if (ratedIds.contains(sessionKey)) {
            return ResponseEntity.ok(Map.of(
                    "rated",      false,
                    "mediaApiId", mediaApiId,
                    "mediaType",  mediaType
            ));
        }

        // Persist rating into Media table
        mediaRankingService.addRating(mediaApiId, mediaType, stars);

        ratedIds.add(sessionKey);
        session.setAttribute("ratedIds", ratedIds);

        return ResponseEntity.ok(Map.of(
                "rated",      true,
                "mediaApiId", mediaApiId,
                "mediaType",  mediaType,
                "stars",      stars
        ));
    }
}