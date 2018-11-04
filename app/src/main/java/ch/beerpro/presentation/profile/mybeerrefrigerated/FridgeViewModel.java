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
import ch.beerpro.data.repositories.BeersRepository;
import ch.beerpro.data.repositories.CurrentUser;
import ch.beerpro.data.repositories.FridgeRepository;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.Fridge;
import ch.beerpro.presentation.utils.EntityClassSnapshotParser;

import static androidx.lifecycle.Transformations.switchMap;
import static ch.beerpro.domain.utils.LiveDataExtensions.combineLatest;

public class FridgeViewModel extends ViewModel implements CurrentUser {
    private static final String TAG = "FridgeViewModel";

    private final MutableLiveData<String> currentUserId;
    private final MutableLiveData<String> beerId;

    private final FridgeRepository fridgeRepository;
    private final LiveData<Beer> beer;
    private final LiveData<Fridge> fridge;
    private EntityClassSnapshotParser<Fridge> parser = new EntityClassSnapshotParser<>(Fridge.class);

    public FridgeViewModel() {
        BeersRepository beersRepository = new BeersRepository();
        fridgeRepository = new FridgeRepository();

        beerId =  new MutableLiveData<>();
        currentUserId = new MutableLiveData<>();
        currentUserId.setValue(getCurrentUser().getUid());
        beer = beersRepository.getBeer(beerId);
        fridge = fridgeRepository.getFridgeFor(currentUserId, beerId);
    }

    public void setBeerId(String beerId){
        this.beerId.setValue(beerId);
    }

    public LiveData<Fridge> getFridge(){
        return fridge;
    }

    public Task<Fridge> updateFridge(Integer amount){
        String id = Fridge.generateId(currentUserId.getValue(), beerId.getValue());
        Fridge newFridge = new Fridge(currentUserId.getValue(), beerId.getValue(), new Date(), amount);

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