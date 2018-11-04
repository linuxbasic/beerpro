package ch.beerpro.presentation.profile.mybeerrefrigerated;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.*;
import java.util.List;
import java.util.Date;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ch.beerpro.data.repositories.CurrentUser;
import ch.beerpro.data.repositories.FridgeRepository;
import ch.beerpro.domain.models.Fridge;
import ch.beerpro.presentation.utils.EntityClassSnapshotParser;

public class FridgeViewModel extends ViewModel implements CurrentUser {

    private static final String TAG = "FridgeViewModel";

    private final LiveData<List<Fridge>> fridges;
    private final FridgeRepository fridgeRepository;
    private final MutableLiveData<String> currentUserId;
    private EntityClassSnapshotParser<Fridge> parser = new EntityClassSnapshotParser<>(Fridge.class);

    public FridgeViewModel() {
        fridgeRepository = new FridgeRepository();
        currentUserId = new MutableLiveData<>();
        fridges = fridgeRepository.getFridges(currentUserId);
        currentUserId.setValue(getCurrentUser().getUid());
    }

    public LiveData<List<Fridge>> getFridges() {
        return fridges;
    }

    public Task<Fridge> saveFridge(String beerId, Integer amount){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        String id = Fridge.generateId(user.getUid(), beerId);
        Fridge newFridge = new Fridge(user.getUid(), beerId, new Date(), amount);
        Log.i(TAG, "Adding new rating: " + newFridge.toString());
        return FirebaseFirestore.getInstance().collection(Fridge.COLLECTION).document(id).set(newFridge).continueWithTask(task -> {
            if (task.isSuccessful()) {
                return FirebaseFirestore.getInstance().collection(Fridge.COLLECTION).document(id).get();
            } else {
                throw task.getException();
            }
        }).continueWithTask(task -> {

            if (task.isSuccessful()) {
                return Tasks.forResult(parser.parseSnapshot(task.getResult()));
            } else {
                throw task.getException();
            }
        });
    }
}