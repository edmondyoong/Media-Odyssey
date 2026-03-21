package com.mo.mediaodyssey.community.favorite;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller for the Community Favourites page (/community).
 *
 * Ranking data comes from UserInteraction table (aggregated by MediaRankingService).
 * Likes and views recorded here are stored directly via UserInteraction.
 *
 * Session attributes used:
 *   - "likedIds"  : Set<Long> of media API IDs liked in this session
 *   - "viewedIds" : Set<Long> of media API IDs viewed in this session
 *   - "ratedIds"  : Set<Long> of media API IDs rated in this session
 */
@Controller
@RequestMapping("/community")
public class CommunityController {

    private final MediaRankingService mediaRankingService;

    @Autowired
    public CommunityController(MediaRankingService mediaRankingService) {
        this.mediaRankingService = mediaRankingService;
    }

    // ── 1. Community Favourites Main Page ─────────────────────────────────────

    /**
     * Loads the Community Favourites page.
     *
     * Supports one query parameter:
     *   - category : "MOVIE" | "GAME" | "SONG" | null (default = all)
     *
     * Ranking is based on UserInteraction data aggregated by MediaRankingService.
     * Always loads the Fast-Rising Top 5 trending section.
     */
    @GetMapping
    public String communityPage(Model model,
                                @RequestParam(required = false) String category) {

        List<RankedMediaResponse> mediaList;

        if (category != null && !category.isBlank()) {
            // Filter Top 10 by category tab (MOVIE / GAME / SONG)
            mediaList = mediaRankingService.getTop10ByMediaType(category);
        } else {
            // Default: Top 10 across all media types
            mediaList = mediaRankingService.getTop10();
        }

        // Fast-Rising Top 5: most liked in the past 7 days
        List<RankedMediaResponse> trending = mediaRankingService.getTop5Trending();

        model.addAttribute("mediaList", mediaList);
        model.addAttribute("trending",  trending);
        model.addAttribute("currentCat", category);

        return "boardsLayout/features/trending";
    }

    // ── 2. Like Media (AJAX) ──────────────────────────────────────────────────

    /**
     * Records a LIKE interaction for the specified media item.
     * Each browser session can only like a given item once.
     *
     * Note: Interaction is stored in UserInteraction table via
     * POST /api/recommendations/interactions on the landing page.
     * This endpoint handles the session-based duplicate prevention
     * and returns the updated like count for the Optimistic UI.
     *
     * Returns JSON:
     *   - liked   : true if accepted, false if already liked this session
     *   - mediaApiId : the media item ID
     */
    @PostMapping("/like/{mediaApiId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> likeMedia(@PathVariable String mediaApiId,
                                                          HttpSession session) {
        @SuppressWarnings("unchecked")
        Set<String> likedIdsRaw = (Set<String>) session.getAttribute("likedIds");
        if (likedIdsRaw == null) {
            likedIdsRaw = new HashSet<>();
            session.setAttribute("likedIds", likedIdsRaw);
        }
        final Set<String> likedIds = likedIdsRaw;

        if (likedIds.contains(mediaApiId)) {
            return ResponseEntity.ok(Map.<String, Object>of(
                    "liked",      false,
                    "mediaApiId", mediaApiId
            ));
        }

        likedIds.add(mediaApiId);
        session.setAttribute("likedIds", likedIds);

        return ResponseEntity.ok(Map.<String, Object>of(
                "liked",      true,
                "mediaApiId", mediaApiId
        ));
    }

    // ── 3. Increment View Count (AJAX) ────────────────────────────────────────

    /**
     * Records a VIEW interaction for the specified media item.
     * Called automatically by JavaScript on page load.
     * Each session can only increment the view count once per item.
     *
     * Returns JSON:
     *   - viewed     : true if accepted, false if already viewed this session
     *   - mediaApiId : the media item ID
     */
    @PostMapping("/view/{mediaApiId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incrementView(@PathVariable String mediaApiId,
                                                              HttpSession session) {
        @SuppressWarnings("unchecked")
        Set<String> viewedIdsRaw = (Set<String>) session.getAttribute("viewedIds");
        if (viewedIdsRaw == null) {
            viewedIdsRaw = new HashSet<>();
            session.setAttribute("viewedIds", viewedIdsRaw);
        }
        final Set<String> viewedIds = viewedIdsRaw;

        if (viewedIds.contains(mediaApiId)) {
            return ResponseEntity.ok(Map.<String, Object>of(
                    "viewed",     false,
                    "mediaApiId", mediaApiId
            ));
        }

        viewedIds.add(mediaApiId);
        session.setAttribute("viewedIds", viewedIds);

        return ResponseEntity.ok(Map.<String, Object>of(
                "viewed",     true,
                "mediaApiId", mediaApiId
        ));
    }

    // ── 4. Submit Star Rating (AJAX) ──────────────────────────────────────────

    /**
     * Records a star rating (1-5) for the specified media item.
     * Each session can only rate a given item once.
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
                                                          HttpSession session) {
        if (stars < 1 || stars > 5) {
            return ResponseEntity.badRequest().build();
        }

        @SuppressWarnings("unchecked")
        Set<String> ratedIdsRaw = (Set<String>) session.getAttribute("ratedIds");
        if (ratedIdsRaw == null) {
            ratedIdsRaw = new HashSet<>();
            session.setAttribute("ratedIds", ratedIdsRaw);
        }
        final Set<String> ratedIds = ratedIdsRaw;

        if (ratedIds.contains(mediaApiId)) {
            return ResponseEntity.ok(Map.<String, Object>of(
                    "rated",      false,
                    "mediaApiId", mediaApiId
            ));
        }

        ratedIds.add(mediaApiId);
        session.setAttribute("ratedIds", ratedIds);

        return ResponseEntity.ok(Map.<String, Object>of(
                "rated",      true,
                "mediaApiId", mediaApiId,
                "stars",      stars
        ));
    }
}