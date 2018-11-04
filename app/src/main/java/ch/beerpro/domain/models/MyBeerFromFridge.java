package ch.beerpro.domain.models;

import java.util.Date;

import lombok.Data;

@Data
public class MyBeerFromFridge implements MyBeer {
    private Fridge fridge;
    private Beer beer;

    public MyBeerFromFridge(Fridge fridge, Beer beer) {
        this.fridge = fridge;
        this.beer = beer;
    }

    @Override
    public String getBeerId() {
        return fridge.getBeerId();
    }

    @Override
    public Date getDate() {
        return fridge.getAddedAt();
    }
}
