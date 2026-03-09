package com.mo.mediaodyssey.recommendation;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class RecommendationService {

    private final UserInteractionRepository userInteractionRepository;

    public RecommendationService(UserInteractionRepository userInteractionRepository) {
        this.userInteractionRepository = userInteractionRepository;
    }

    private String userFavoriteGenre(Long userId, String mediaType) {
    List<UserInteraction> allUserInteractions = userInteractionRepository.findByUserId(userId);

    Map<String, Integer> genreScores = new HashMap<>();

    for (UserInteraction interaction : allUserInteractions) {
        if (interaction.getMediaType().equals(mediaType)) {

            int points = 0;
            switch (interaction.getInteractionType()) {
                case "VIEW": points = 1; break;
                case "LIKE": points = 10; break;
                // TODO: add RATING case when ratings are implemented
                default: points = 0; break;
            }

            for (String genre : interaction.getGenres()) {
                // if genre already exists in map, add points to its current score; if not, start it at 0 then add points
                genreScores.put(genre, genreScores.getOrDefault(genre, 0) + points);
            }
        }
    }

    // find genre with highest score
    String favoriteGenre = null;
    int highestScore = 0;
    for (Map.Entry<String, Integer> entry : genreScores.entrySet()) {
        if (entry.getValue() > highestScore) {
            highestScore = entry.getValue();
            favoriteGenre = entry.getKey();
        }
    }

    return favoriteGenre;
    }

    public List<RecommendationResponse> getRecommendations(Long userId, String mediaType) {
    // logic here
    // userFavoriteGenre(userId, mediaType) find best media within genre and recommend to user

    return null;
}
}
