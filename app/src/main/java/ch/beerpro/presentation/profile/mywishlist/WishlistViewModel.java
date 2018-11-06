package ch.beerpro.presentation.profile.mywishlist;

import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ch.beerpro.data.repositories.BeersRepository;
import ch.beerpro.data.repositories.CurrentUser;
import ch.beerpro.data.repositories.FridgeRepository;
import ch.beerpro.data.repositories.WishlistRepository;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.Fridge;
import ch.beerpro.domain.models.Wish;
import com.google.android.gms.tasks.Task;

import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public class WishlistViewModel extends ViewModel implements CurrentUser {

    private static final String TAG = "WishlistViewModel";

    private final MutableLiveData<String> currentUserId = new MutableLiveData<>();
    private final WishlistRepository wishlistRepository;
    private final BeersRepository beersRepository;
    private final FridgeRepository fridgeRepository;
    private final LiveData<List<Fridge>> fridges;

    public WishlistViewModel() {
        wishlistRepository = new WishlistRepository();
        beersRepository = new BeersRepository();
        fridgeRepository = new FridgeRepository();
        fridges = fridgeRepository.getFridges(currentUserId);
        currentUserId.setValue(getCurrentUser().getUid());
    }

    public LiveData<List<Triple<Wish, Beer, Fridge>>> getMyWishlistWithBeers() {
        return wishlistRepository.getMyWishlistWithBeers(currentUserId, beersRepository.getAllBeers(), fridges);
    }

    public Task<Void> toggleItemInWishlist(String itemId) {
        return wishlistRepository.toggleUserWishlistItem(getCurrentUser().getUid(), itemId);
    }

    public Fridge getFridge(String beerId){
        List<Fridge> allFridges = fridges.getValue();
        if(allFridges != null){
            for (Fridge fridge : allFridges) {
                if(beerId.equals(fridge.getBeerId())){
                    return fridge;
                }
            }
        }
        return null;
    }

}