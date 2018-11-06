package ch.beerpro.presentation.profile.mybeerrefrigerated;

import android.util.Pair;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ch.beerpro.data.repositories.BeersRepository;
import ch.beerpro.data.repositories.CurrentUser;
import ch.beerpro.data.repositories.FridgeRepository;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.Entity;
import ch.beerpro.domain.models.Fridge;
import ch.beerpro.presentation.utils.EntityClassSnapshotParser;
import static androidx.lifecycle.Transformations.map;
import static ch.beerpro.domain.utils.LiveDataExtensions.combineLatest;

public class FridgeViewModel extends ViewModel implements CurrentUser {
    private static final String TAG = "FridgeViewModel";

    private final MutableLiveData<String> currentUserId;
    private final MutableLiveData<String> beerId;

    private final FridgeRepository fridgeRepository;
    private final BeersRepository beersRepository;
    private final LiveData<Beer> beer;
    private final LiveData<Fridge> fridge;
    private final LiveData<List<Pair<Beer, Fridge>>> fridges;
    private EntityClassSnapshotParser<Fridge> parser = new EntityClassSnapshotParser<>(Fridge.class);

    public FridgeViewModel() {
        beersRepository = new BeersRepository();
        fridgeRepository = new FridgeRepository();

        beerId =  new MutableLiveData<>();
        currentUserId = new MutableLiveData<>();
        beer = beersRepository.getBeer(beerId);
        fridge = fridgeRepository.getFridgeFor(currentUserId, beerId);
        fridges = map(combineLatest(fridgeRepository.getFridges(currentUserId), map(beersRepository.getAllBeers(), Entity::entitiesById)), FridgeViewModel::addBeer);


        currentUserId.setValue(getCurrentUser().getUid());
    }

    private static List<Pair<Beer, Fridge>> addBeer(Pair<List<Fridge>, HashMap<String, Beer>> data){
        List<Fridge> fridges = data.first;
        HashMap<String, Beer> beerMap = data.second;
        ArrayList<Pair<Beer, Fridge>> out = new ArrayList();

        for (Fridge fridge : fridges){
            String beerId = fridge.getBeerId();
            out.add(Pair.create(beerMap.get(beerId), fridge));
        }
        return out;
    }

    public void setBeerId(String beerId){
        this.beerId.setValue(beerId);
    }

    public LiveData<Fridge> getFridge(){
        return fridge;
    }

    public Fridge getFridge(String beerId){
        List<Pair<Beer, Fridge>> allFridgesPairs = fridges.getValue();
        if(allFridgesPairs != null){
            for (Pair<Beer, Fridge> pair : allFridgesPairs) {
                Fridge fridge = pair.second;
                if(beerId.equals(fridge.getBeerId())){
                    return fridge;
                }
            }
        }
        return null;
    }

    public LiveData<List<Pair<Beer, Fridge>>> getBeersWithFridge(){
        return fridges;
    }

    public Task<Fridge> updateFridge(Integer amount){
        String id = Fridge.generateId(currentUserId.getValue(), beerId.getValue());
        Fridge newFridge = new Fridge(currentUserId.getValue(), beerId.getValue(), new Date(), amount);

        if (amount > 0){
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
        return FirebaseFirestore.getInstance().collection(Fridge.COLLECTION).document(id).delete().continueWithTask(task -> {
            if (task.isSuccessful()) {
                return null;
            } else {
                throw task.getException();
            }
        });

    }
}