package com.mo.mediaodyssey.recommendation;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RecommendationService {

    private final UserInteractionRepository userInteractionRepository;

    public RecommendationService(UserInteractionRepository userInteractionRepository) {
        this.userInteractionRepository = userInteractionRepository;
    }

    public List<RecommendationResponse> getRecommendations(Long userId, String mediaType) {
    // logic here
    List<UserInteraction> allUserInteractions = userInteractionRepository.findByUserId(userId);
    for (UserInteraction interaction : allUserInteractions) {
        System.out.println("User Interaction: " + interaction.getInteractionType() + " on " + interaction.getMediaApiId());
    }

    return null;
}
}
