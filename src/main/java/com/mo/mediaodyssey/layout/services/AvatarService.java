package com.mo.mediaodyssey.layout.services;

import org.springframework.stereotype.Service;

@Service
public class AvatarService {

    /*
    ** This service will serves as calling the DiceBear API, which is an API for generating avatar pictures.
    *
    *** Logic: Use a unique feature from the user as the seed for calling DiceBear API. Extra features like
    *  rotate, flip, radius is for the uniqueness of avatar and the size to display avatar. 
    *  Seed in DiceBear API ensures that no matter how many times we call this API, it will give the exact same
    *  image for the same seed. 
    * 
    ** => Ensures the same default avatar for each user.
    */

    public String avatarGenerate (Long seed) {
        int hash = seed.hashCode(); 

        int rotate = Math.abs(hash%360);
        boolean flip =hash % 2 == 0; 

        return "https://api.dicebear.com/9.x/glass/svg?seed=" + seed
                                                + "&rotate=" + rotate
                                                + "&flip=" + flip
                                                + "&radius=50" ;
    }
    
}
