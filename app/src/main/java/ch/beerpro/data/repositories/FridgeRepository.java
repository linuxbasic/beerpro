package ch.beerpro.data.repositories;

import android.util.Log;
import android.util.Pair;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.lifecycle.LiveData;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.Entity;
import ch.beerpro.domain.models.Fridge;
import ch.beerpro.domain.utils.FirestoreQueryLiveData;
import ch.beerpro.domain.utils.FirestoreQueryLiveDataArray;

import static androidx.lifecycle.Transformations.map;
import static androidx.lifecycle.Transformations.switchMap;
import static ch.beerpro.domain.utils.LiveDataExtensions.combineLatest;

public class FridgeRepository {


    private static LiveData<List<Fridge>> getFridgesByUser(String userId) {
        return new FirestoreQueryLiveDataArray<>(
                FirebaseFirestore.getInstance()
                    .collection(Fridge.COLLECTION)
                    .orderBy(Fridge.FIELD_ADDED_AT, Query.Direction.DESCENDING)
                    .whereEqualTo(Fridge.FIELD_USER_ID, userId)
                , Fridge.class);
    }

    public LiveData<List<Fridge>> getFridges(LiveData<String> currentUserId) {
        return switchMap(currentUserId, FridgeRepository::getFridgesByUser);
    }

    private static LiveData<Fridge> getFridgeFor(Pair<String, String> input) {
        String userId = input.first;
        String beerId = input.second;
        String fridgeId = Fridge.generateId(userId, beerId);
        Log.wtf("linus", fridgeId);
        DocumentReference document = FirebaseFirestore.getInstance().collection(Fridge.COLLECTION)
                .document(fridgeId);
        return new FirestoreQueryLiveData<>(document, Fridge.class);
    }

    public LiveData<Fridge> getFridgeFor(LiveData<String> currentUserId, LiveData<String> beerId){
        return switchMap(combineLatest(currentUserId, beerId), FridgeRepository::getFridgeFor);
    }


}
