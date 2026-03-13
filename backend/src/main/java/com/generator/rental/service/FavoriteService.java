package com.generator.rental.service;

import com.generator.rental.entity.Favorite;
import java.util.List;

public interface FavoriteService {
    List<Favorite> getFavorites(String userId);
    boolean toggleFavorite(String userId, Long generatorId);
}
