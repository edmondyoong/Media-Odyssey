package com.mo.mediaodyssey.layout.services;

import org.springframework.stereotype.Service;

@Service
public class AvatarService {

    public static String avatarGenerate(Long seed) {
        int hash = seed.hashCode();

        int rotate = Math.abs(hash % 360);
        boolean flip = hash % 2 == 0;

        return "https://api.dicebear.com/9.x/glass/svg?seed=" + seed
                + "&rotate=" + rotate
                + "&flip=" + flip
                + "&radius=50";
    }

}
