package ch.beerpro.presentation.utils;

import org.apache.commons.lang3.tuple.Triple;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import ch.beerpro.domain.models.Entity;

public class EntityTripleDiffItemCallback<T extends Entity, U, R> extends DiffUtil.ItemCallback<Triple<T, U, R>> {
    @Override
    public boolean areItemsTheSame(@NonNull Triple<T, U, R> oldE, @NonNull Triple<T, U, R> newE) {
        return oldE.getLeft().getId().equals(newE.getLeft().getId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Triple<T, U, R> oldE, @NonNull Triple<T, U, R> newE) {
        return oldE.equals(newE);
    }
}
