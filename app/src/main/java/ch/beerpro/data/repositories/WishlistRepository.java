package ch.beerpro.data.repositories;

import android.util.Pair;
import androidx.lifecycle.LiveData;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.Entity;
import ch.beerpro.domain.models.Fridge;
import ch.beerpro.domain.models.Wish;
import ch.beerpro.domain.utils.FirestoreQueryLiveData;
import ch.beerpro.domain.utils.FirestoreQueryLiveDataArray;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static androidx.lifecycle.Transformations.map;
import static androidx.lifecycle.Transformations.switchMap;
import static ch.beerpro.domain.utils.LiveDataExtensions.combineLatest;

public class WishlistRepository {


    private static LiveData<List<Wish>> getWishesByUser(String userId) {
        return new FirestoreQueryLiveDataArray<>(FirebaseFirestore.getInstance().collection(Wish.COLLECTION)
                .orderBy(Wish.FIELD_ADDED_AT, Query.Direction.DESCENDING).whereEqualTo(Wish.FIELD_USER_ID, userId),
                Wish.class);
    }

    private static LiveData<Wish> getUserWishListFor(Pair<String, Beer> input) {
        String userId = input.first;
        Beer beer = input.second;
        DocumentReference document = FirebaseFirestore.getInstance().collection(Wish.COLLECTION)
                .document(Wish.generateId(userId, beer.getId()));
        return new FirestoreQueryLiveData<>(document, Wish.class);
    }

    public Task<Void> toggleUserWishlistItem(String userId, String itemId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String wishId = Wish.generateId(userId, itemId);

        DocumentReference wishEntryQuery = db.collection(Wish.COLLECTION).document(wishId);

        return wishEntryQuery.get().continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                return wishEntryQuery.delete();
            } else if (task.isSuccessful()) {
                return wishEntryQuery.set(new Wish(userId, itemId, new Date()));
            } else {
                throw task.getException();
            }
        });
    }

    public LiveData<List<Triple<Wish, Beer,Fridge>>> getMyWishlistWithBeers(LiveData<String> currentUserId,
                                                                   LiveData<List<Beer>> allBeers,
                                                                   LiveData<List<Fridge>> fridges) {
        return map(combineLatest(getMyWishlist(currentUserId), map(allBeers, Entity::entitiesById), map(fridges, Entity::entitiesById)), input -> {
            List<Wish> wishes = input.getLeft();
            HashMap<String, Beer> beersById = input.getMiddle();
            HashMap<String, Fridge> fridgeById = input.getRight();

            ArrayList<Triple<Wish, Beer, Fridge>> result = new ArrayList<>();
            for (Wish wish : wishes) {
                String beerId = wish.getBeerId();
                Beer beer = beersById.get(beerId);
                Fridge fridge = fridgeById.get(beerId);
                result.add(Triple.of(wish, beer, fridge));
            }
            return result;
        });
    }

    public LiveData<List<Wish>> getMyWishlist(LiveData<String> currentUserId) {
        return switchMap(currentUserId, WishlistRepository::getWishesByUser);
    }


    public LiveData<Wish> getMyWishForBeer(LiveData<String> currentUserId, LiveData<Beer> beer) {


        return switchMap(combineLatest(currentUserId, beer), WishlistRepository::getUserWishListFor);
    }


}
