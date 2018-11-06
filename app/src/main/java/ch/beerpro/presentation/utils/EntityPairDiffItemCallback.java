package ch.beerpro.presentation.utils;

import android.util.Pair;

import org.apache.commons.lang3.tuple.Triple;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import ch.beerpro.domain.models.Entity;

public class EntityPairDiffItemCallback<T extends Entity, U> extends DiffUtil.ItemCallback<Pair<T, U>> {
    @Override
    public boolean areItemsTheSame(@NonNull Pair<T, U> oldE, @NonNull Pair<T, U> newE) {
        return oldE.first.getId().equals(newE.first.getId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Pair<T, U> oldE, @NonNull Pair<T, U> newE) {
        return oldE.equals(newE);
    }
}