package ch.beerpro.presentation.explore.search;

import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ch.beerpro.data.repositories.BeersRepository;
import ch.beerpro.data.repositories.CurrentUser;
import ch.beerpro.data.repositories.SearchesRepository;
import ch.beerpro.data.repositories.WishlistRepository;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.Search;
import com.google.common.base.Strings;

import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.lifecycle.Transformations.map;
import static androidx.lifecycle.Transformations.switchMap;
import static ch.beerpro.domain.utils.LiveDataExtensions.zip;

public class SearchViewModel extends ViewModel implements CurrentUser {

    private static final String TAG = "SearchViewModel";
    private final MutableLiveData<String> searchTerm = new MutableLiveData<>();
    private final MutableLiveData<String> categoryFilter = new MutableLiveData<>();
    private final MutableLiveData<String> currentUserId = new MutableLiveData<>();

    private final LiveData<List<Beer>> filteredBeers;
    private final BeersRepository beersRepository;
    private final WishlistRepository wishlistRepository;
    private final SearchesRepository searchesRepository;
    private final LiveData<List<Search>> myLatestSearches;

    public SearchViewModel() {
        beersRepository = new BeersRepository();
        wishlistRepository = new WishlistRepository();
        searchesRepository = new SearchesRepository();
        filteredBeers = map(zip(searchTerm, categoryFilter, getAllBeers()), SearchViewModel::filter);
        myLatestSearches = switchMap(currentUserId, SearchesRepository::getLatestSearchesByUser);

        currentUserId.setValue(getCurrentUser().getUid());
    }

    public LiveData<List<Beer>> getAllBeers() {
        return beersRepository.getAllBeers();
    }

    private static List<Beer> filter(Triple<String, String, List<Beer>> input) {
        String searchTermValue = input.getLeft();
        String categoryFilterValue = input.getMiddle();
        List<Beer> allBeers = input.getRight();

        if (allBeers == null && categoryFilterValue == null) {
            return Collections.emptyList();
        }
        ArrayList<Beer> filtered = new ArrayList<>();
        for (Beer beer : allBeers) {
            if (Strings.isNullOrEmpty(searchTermValue) || beer.getName().toLowerCase().contains(searchTermValue.toLowerCase())) {
                if (Strings.isNullOrEmpty(categoryFilterValue) || categoryFilterValue.equals(beer.getCategory())) {
                    filtered.add(beer);
                }
            }
        }
        return filtered;
    }

    public LiveData<List<Search>> getMyLatestSearches() {
        return myLatestSearches;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm.setValue(searchTerm);
    }
    public void setCategoryFilter(String categoryFilter) {
        this.categoryFilter.setValue(categoryFilter);
    }

    public LiveData<List<Beer>> getFilteredBeers() {
        return filteredBeers;
    }


    public void toggleItemInWishlist(String beerId) {
        wishlistRepository.toggleUserWishlistItem(getCurrentUser().getUid(), beerId);
    }

    public void addToSearchHistory(String term) {
        searchesRepository.addSearchTerm(term);
    }
}