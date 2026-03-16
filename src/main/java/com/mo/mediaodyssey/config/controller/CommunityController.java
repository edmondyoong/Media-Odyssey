package com.mo.mediaodyssey.config.controller;

import com.mo.mediaodyssey.entity.Media;
import com.mo.mediaodyssey.repository.MediaRepository;
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
 * Responsibilities:
 *   1. Load and display the Top 10 ranked media list
 *   2. Load the Fast-Rising Top 5 trending section
 *   3. Handle AJAX like requests (session-based duplicate prevention)
 *   4. Handle AJAX view count increments (session-based, once per item per session)
 *   5. Handle AJAX star rating submissions (session-based, once per item per session)
 *   6. Serve the Add Media form and save new media items
 *
 * Session attributes used:
 *   - "likedIds"  : Set<Long> of media IDs liked in this session
 *   - "viewedIds" : Set<Long> of media IDs viewed in this session
 *   - "ratedIds"  : Set<Long> of media IDs rated in this session
 */
@Controller
@RequestMapping("/community")
public class CommunityController {

    private final MediaRepository mediaRepository;

    @Autowired
    public CommunityController(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    // ── 1. Community Favourites Main Page ─────────────────────────────────────

    /**
     * Loads the Community Favourites page.
     *
     * Supports three query parameters:
     *   - sort     : "likes" | "views" | "rating" | null (default = Point System Top 10)
     *   - category : "Movie" | "Game" | "Song" | null (default = all categories)
     *
     * Always loads the Fast-Rising Top 5 trending section regardless of sort/category.
     */
    @GetMapping
    public String communityPage(Model model,
                                @RequestParam(required = false) String sort,
                                @RequestParam(required = false) String category) {

        List<Media> mediaList;

        if ("likes".equals(sort)) {
            // Sort all media by total likes — "Most Liked" view
            mediaList = mediaRepository.findAllByOrderByLikesDesc();
        } else if ("views".equals(sort)) {
            // Sort all media by total views — "Most Viewed" view
            mediaList = mediaRepository.findAllByOrderByViewsDesc();
        } else if ("rating".equals(sort)) {
            // Sort by average star rating — "Top Rated" view (only items with >= 1 rating)
            mediaList = mediaRepository.findTop10ByRating();
        } else if (category != null && !category.isBlank()) {
            // Filter Top 10 by category tab (Movie / Game / Song)
            mediaList = mediaRepository.findTop10ByScoreAndCategory(category);
        } else {
            // Default: Top 10 by Point System across all categories
            mediaList = mediaRepository.findTop10ByScore();
        }

        // Always load the Fast-Rising Top 5 for the trending section
        List<Media> trending = mediaRepository.findTop5Trending();

        model.addAttribute("mediaList",    mediaList);
        model.addAttribute("trending",     trending);
        model.addAttribute("currentSort",  sort);
        model.addAttribute("currentCat",   category);

        return "trending";
    }

    // ── 2. Add Media Form ─────────────────────────────────────────────────────

    /** Renders the Add Media form. Only title and category are required from the user. */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("media", new Media());
        return "add-media";
    }

    // ── 3. Save New Media ─────────────────────────────────────────────────────

    /**
     * Saves a new media item submitted from the Add Media form.
     * views, likes, likesLastWeek, ratingSum, ratingCount all default to 0 via @PrePersist.
     */
    @PostMapping("/add")
    public String saveMedia(@ModelAttribute Media media) {
        mediaRepository.save(media);
        return "redirect:/community";
    }

    // ── 4. Like Media (AJAX) ──────────────────────────────────────────────────

    /**
     * Increments the like count by 1 for the specified media item.
     * Each browser session can only like a given item once.
     *
     * Session mechanism:
     *   - A Set<Long> called "likedIds" is stored in HttpSession.
     *   - If the media ID is already in the set, the request is rejected.
     *   - On success, the ID is added to the set to prevent future duplicates.
     *
     * Returns JSON:
     *   - liked      : true if the like was accepted, false if already liked this session
     *   - likes      : updated like count (server-confirmed)
     *   - totalScore : recalculated Point System score
     */
    @PostMapping("/like/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> likeMedia(@PathVariable Long id,
                                                          HttpSession session) {
        // Retrieve or initialise the liked IDs set for this session
        @SuppressWarnings("unchecked")
        Set<Long> likedIdsRaw = (Set<Long>) session.getAttribute("likedIds");
        if (likedIdsRaw == null) {
            likedIdsRaw = new HashSet<>();
            session.setAttribute("likedIds", likedIdsRaw);
        }
        final Set<Long> likedIds = likedIdsRaw;

        // Reject duplicate likes within the same session
        if (likedIds.contains(id)) {
            return mediaRepository.findById(id)
                    .map(m -> ResponseEntity.ok(Map.<String, Object>of(
                            "liked",      false,
                            "likes",      m.getLikes(),
                            "totalScore", m.getTotalScore()
                    )))
                    .orElse(ResponseEntity.notFound().build());
        }

        return mediaRepository.findById(id)
                .map(m -> {
                    m.setLikes(m.getLikes() + 1);
                    mediaRepository.save(m);

                    // Record this like so it cannot be repeated in this session
                    likedIds.add(id);
                    session.setAttribute("likedIds", likedIds);

                    return ResponseEntity.ok(Map.<String, Object>of(
                            "liked",      true,
                            "likes",      m.getLikes(),
                            "totalScore", m.getTotalScore()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── 5. Increment View Count (AJAX) ────────────────────────────────────────

    /**
     * Increments the view count by 1 for the specified media item.
     * Called automatically by JavaScript on page load for each visible card.
     * Each browser session can only increment the view count for a given item once,
     * preventing count inflation on page refresh.
     *
     * Returns JSON:
     *   - views      : updated view count (server-confirmed)
     *   - totalScore : recalculated Point System score
     */
    @PostMapping("/view/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incrementView(@PathVariable Long id,
                                                              HttpSession session) {
        // Retrieve or initialise the viewed IDs set for this session
        @SuppressWarnings("unchecked")
        Set<Long> viewedIdsRaw = (Set<Long>) session.getAttribute("viewedIds");
        if (viewedIdsRaw == null) {
            viewedIdsRaw = new HashSet<>();
            session.setAttribute("viewedIds", viewedIdsRaw);
        }
        final Set<Long> viewedIds = viewedIdsRaw;

        // Skip if already counted for this session — return current values
        if (viewedIds.contains(id)) {
            return mediaRepository.findById(id)
                    .map(m -> ResponseEntity.ok(Map.<String, Object>of(
                            "views",      m.getViews(),
                            "totalScore", m.getTotalScore()
                    )))
                    .orElse(ResponseEntity.notFound().build());
        }

        return mediaRepository.findById(id)
                .map(m -> {
                    m.setViews(m.getViews() + 1);
                    mediaRepository.save(m);

                    // Record this view so it cannot be repeated in this session
                    viewedIds.add(id);
                    session.setAttribute("viewedIds", viewedIds);

                    return ResponseEntity.ok(Map.<String, Object>of(
                            "views",      m.getViews(),
                            "totalScore", m.getTotalScore()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── 6. Submit Star Rating (AJAX) ──────────────────────────────────────────

    /**
     * Submits a star rating (1–5) for the specified media item.
     * Each browser session can only rate a given item once.
     *
     * The rating is stored as a running sum (ratingSum) and count (ratingCount),
     * allowing the average to be computed without storing individual ratings.
     *
     * Returns JSON:
     *   - rated         : true if the rating was accepted, false if already rated
     *   - averageRating : updated average rating (server-confirmed, 1 decimal place)
     *   - ratingCount   : total number of ratings submitted
     */
    @PostMapping("/rate/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rateMedia(@PathVariable Long id,
                                                          @RequestParam int stars,
                                                          HttpSession session) {
        // Validate star value — must be between 1 and 5 inclusive
        if (stars < 1 || stars > 5) {
            return ResponseEntity.badRequest().build();
        }

        // Retrieve or initialise the rated IDs set for this session
        @SuppressWarnings("unchecked")
        Set<Long> ratedIdsRaw = (Set<Long>) session.getAttribute("ratedIds");
        if (ratedIdsRaw == null) {
            ratedIdsRaw = new HashSet<>();
            session.setAttribute("ratedIds", ratedIdsRaw);
        }
        final Set<Long> ratedIds = ratedIdsRaw;

        // Reject duplicate ratings within the same session
        if (ratedIds.contains(id)) {
            return mediaRepository.findById(id)
                    .map(m -> ResponseEntity.ok(Map.<String, Object>of(
                            "rated",         false,
                            "averageRating", m.getAverageRating(),
                            "ratingCount",   m.getRatingCount()
                    )))
                    .orElse(ResponseEntity.notFound().build());
        }

        return mediaRepository.findById(id)
                .map(m -> {
                    m.setRatingSum(m.getRatingSum() + stars);
                    m.setRatingCount(m.getRatingCount() + 1);
                    mediaRepository.save(m);

                    // Record this rating so it cannot be repeated in this session
                    ratedIds.add(id);
                    session.setAttribute("ratedIds", ratedIds);

                    return ResponseEntity.ok(Map.<String, Object>of(
                            "rated",         true,
                            "averageRating", m.getAverageRating(),
                            "ratingCount",   m.getRatingCount()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}