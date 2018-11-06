package ch.beerpro.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.Entity;
import ch.beerpro.domain.models.Fridge;
import ch.beerpro.domain.models.Rating;
import ch.beerpro.domain.models.Wish;
import ch.beerpro.domain.models.MyBeer;
import ch.beerpro.domain.models.MyBeerFromRating;
import ch.beerpro.domain.models.MyBeerFromWishlist;
import java.util.*;

import static androidx.lifecycle.Transformations.map;
class MyBeerUpdate{
    public HashMap<String, Beer> beerMap;
    public List<Wish> wishes;
    public List<Rating> ratings;
    public List<Fridge> fridges;
    public boolean complete(){
        return (beerMap != null && wishes != null && ratings != null && fridges != null);
    }
    public MyBeerUpdate clone(){
        MyBeerUpdate clone = new MyBeerUpdate();
        clone.beerMap = beerMap;
        clone.wishes = wishes;
        clone.ratings = ratings;
        clone.fridges = fridges;
        return clone;
    }
}

public class MyBeersRepository extends MediatorLiveData<MyBeerUpdate> {


    MyBeerUpdate update = new MyBeerUpdate();

    public MyBeersRepository(LiveData<List<Beer>> beers, LiveData<List<Wish>> whishes, LiveData<List<Rating>> ratings, LiveData<List<Fridge>> fridges){
        this.addSource(map(beers, Entity::entitiesById), value -> {
            this.update.beerMap = (HashMap<String, Beer>) value;
            this.update();
        });
        this.addSource(whishes, value -> {
            this.update.wishes = (List<Wish>) value;
            this.update();
        });
        this.addSource(ratings, value -> {
            this.update.ratings = (List<Rating>) value;
            this.update();
        });
        this.addSource(fridges, value -> {
            this.update.fridges = (List<Fridge>) value;
            this.update();
        });
    }

    private void update(){
        if (this.update.complete()){
            this.setValue(this.update.clone());
        }
    }


    private static List<MyBeer> getMyBeers(MyBeerUpdate update) {
        ArrayList<MyBeer> result = new ArrayList<>();
        Set<String> beersAlreadyOnTheList = new HashSet<>();
        for (Wish wish : update.wishes) {
            String beerId = wish.getBeerId();
            result.add(new MyBeerFromWishlist(wish, update.beerMap.get(beerId)));
            beersAlreadyOnTheList.add(beerId);
        }

        for (Rating rating : update.ratings) {
            String beerId = rating.getBeerId();
            if (beersAlreadyOnTheList.contains(beerId)) {
                // if the beer is already on the wish list, don't add it again
            } else {
                result.add(new MyBeerFromRating(rating, update.beerMap.get(beerId)));
                // we also don't want to see a rated beer twice
                beersAlreadyOnTheList.add(beerId);
            }
        }
        Collections.sort(result, (r1, r2) -> r2.getDate().compareTo(r1.getDate()));
        return result;
    }


    public LiveData<List<MyBeer>> getMyBeers() {
        return map(this, MyBeersRepository::getMyBeers);
    }

}
