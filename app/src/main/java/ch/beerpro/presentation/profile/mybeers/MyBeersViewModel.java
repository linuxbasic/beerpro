package ch.beerpro.presentation.profile.mybeers;

import android.util.Log;
import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ch.beerpro.data.repositories.*;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.Fridge;
import ch.beerpro.domain.models.Rating;
import ch.beerpro.domain.models.Wish;
import ch.beerpro.domain.models.MyBeer;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.lifecycle.Transformations.map;
import static ch.beerpro.domain.utils.LiveDataExtensions.zip;

public class MyBeersViewModel extends ViewModel implements CurrentUser {

    private static final String TAG = "MyBeersViewModel";
    private final MutableLiveData<String> searchTerm = new MutableLiveData<>();

    private final WishlistRepository wishlistRepository;
    private final LiveData<List<MyBeer>> myFilteredBeers;
    private final LiveData<List<Fridge>> fridges;
    private final LiveData<List<Rating>> myRatings;
    private final LiveData<List<Wish>> myWishlist;
    private final LiveData<List<Beer>> allBeers;
    private final MutableLiveData<String> currentUserId;
    private final LiveData<List<MyBeer>> myBeers;

    public MyBeersViewModel() {

        wishlistRepository = new WishlistRepository();
        BeersRepository beersRepository = new BeersRepository();
        RatingsRepository ratingsRepository = new RatingsRepository();
        FridgeRepository fridgeRepository = new FridgeRepository();

        allBeers = beersRepository.getAllBeers();
        currentUserId = new MutableLiveData<>();
        myWishlist = wishlistRepository.getMyWishlist(currentUserId);
        myRatings = ratingsRepository.getMyRatings(currentUserId);
        fridges = fridgeRepository.getFridges(currentUserId);

        MyBeersRepository myBeersRepository = new MyBeersRepository(allBeers, myWishlist, myRatings, fridges);
        myBeers = myBeersRepository.getMyBeers();

        myFilteredBeers = map(zip(searchTerm, myBeers), MyBeersViewModel::filter);

        currentUserId.setValue(getCurrentUser().getUid());
    }

    private static List<MyBeer> filter(Pair<String, List<MyBeer>> input) {
        String searchTerm1 = input.first;
        List<MyBeer> myBeers = input.second;
        if (Strings.isNullOrEmpty(searchTerm1)) {
            return myBeers;
        }
        if (myBeers == null) {
            return Collections.emptyList();
        }
        ArrayList<MyBeer> filtered = new ArrayList<>();
        for (MyBeer beer : myBeers) {
            if (beer.getBeer().getName().toLowerCase().contains(searchTerm1.toLowerCase())) {
                filtered.add(beer);
            }
        }
        return filtered;
    }

    public LiveData<List<MyBeer>> getMyFilteredBeers() {
        return myFilteredBeers;
    }

    public void toggleItemInWishlist(String beerId) {
        wishlistRepository.toggleUserWishlistItem(getCurrentUser().getUid(), beerId);
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

    public void setSearchTerm(String searchTerm) {
        this.searchTerm.setValue(searchTerm);
    }
}