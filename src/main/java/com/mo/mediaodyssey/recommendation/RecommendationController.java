package com.mo.mediaodyssey.recommendation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    // gets user ID from the logged-in session via Spring Security
    // TODO: replace with real user lookup once teammate wires up login
    // e.g. return userRepository.findByUsername(auth.getName()).getId();
    private Long getUserIdFromSession(Authentication auth) {
        return Long.parseLong(auth.getName());
    }

    // POST /api/recommendations/interactions
    // frontend calls this when a user views or likes something
    // body: { "mediaApiId": "...", "interactionType": "VIEW", "mediaType": "MOVIE", "genres": ["Action"] }
    @PostMapping("/interactions")
    public ResponseEntity<Void> recordInteraction(@RequestBody InteractionRequest request,
                                                   Authentication auth) {
        Long userId = getUserIdFromSession(auth);
        recommendationService.recordInteraction(userId, request);
        return ResponseEntity.ok().build();
    }

    // GET /api/recommendations?mediaType=MOVIE
    // returns a list of recommended media for the logged-in user
    @GetMapping
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(@RequestParam String mediaType,
                                                                            Authentication auth) {
        Long userId = getUserIdFromSession(auth);
        List<RecommendationResponse> recommendations = recommendationService.getRecommendations(userId, mediaType);
        return ResponseEntity.ok(recommendations);
    }

    // POST /api/recommendations/admin/ban?mediaApiId=123&mediaType=MOVIE
    // admin bans a media item from appearing in recommendations
    @PostMapping("/admin/ban")
    public ResponseEntity<Void> banMedia(@RequestParam String mediaApiId,
                                          @RequestParam String mediaType) {
        recommendationService.banMedia(mediaApiId, mediaType);
        return ResponseEntity.ok().build();
    }

    // DELETE /api/recommendations/admin/ban?mediaApiId=123
    // admin unbans a media item
    @DeleteMapping("/admin/ban")
    public ResponseEntity<Void> unbanMedia(@RequestParam String mediaApiId) {
        recommendationService.unbanMedia(mediaApiId);
        return ResponseEntity.ok().build();
    }
}