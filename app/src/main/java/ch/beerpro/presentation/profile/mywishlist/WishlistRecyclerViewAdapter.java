package ch.beerpro.presentation.profile.mywishlist;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ch.beerpro.GlideApp;
import ch.beerpro.R;
import ch.beerpro.domain.models.Fridge;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.domain.models.Wish;
import ch.beerpro.presentation.utils.EntityTripleDiffItemCallback;
import com.bumptech.glide.request.RequestOptions;

import org.apache.commons.lang3.tuple.Triple;

import java.text.DateFormat;


public class WishlistRecyclerViewAdapter extends ListAdapter<Triple<Wish, Beer, Fridge>, WishlistRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "WishlistRecyclerViewAda";

    private static final DiffUtil.ItemCallback<Triple<Wish, Beer, Fridge>> DIFF_CALLBACK = new EntityTripleDiffItemCallback();

    private final OnWishlistItemInteractionListener listener;

    public WishlistRecyclerViewAdapter(OnWishlistItemInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.activity_my_wishlist_listentry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Triple<Wish, Beer, Fridge> item = getItem(position);
        holder.bind(item.getLeft(), item.getMiddle(), item.getRight(), listener);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.name)
        TextView name;

        @BindView(R.id.manufacturer)
        TextView manufacturer;

        @BindView(R.id.category)
        TextView category;

        @BindView(R.id.photo)
        ImageView photo;

        @BindView(R.id.ratingBar)
        RatingBar ratingBar;

        @BindView(R.id.numRatings)
        TextView numRatings;

        @BindView(R.id.addedAt)
        TextView addedAt;

        @BindView(R.id.removeFromWishlist)
        Button remove;

        @BindView(R.id.manageFridgeButton)
        Button manageFridgeButton;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }

        void bind(Wish wish, Beer item, Fridge fridge, OnWishlistItemInteractionListener listener) {
            name.setText(item.getName());
            manufacturer.setText(item.getManufacturer());
            category.setText(item.getCategory());
            name.setText(item.getName());
            GlideApp.with(itemView).load(item.getPhoto()).apply(new RequestOptions().override(240, 240).centerInside())
                    .into(photo);
            ratingBar.setNumStars(5);
            ratingBar.setRating(item.getAvgRating());
            numRatings.setText(itemView.getResources().getString(R.string.fmt_num_ratings, item.getNumRatings()));
            itemView.setOnClickListener(v -> listener.onMoreClickedListener(photo, item));
            manageFridgeButton.setOnClickListener(v -> listener.onFridgeClickedListener(item));

            String formattedDate =
                    DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(wish.getAddedAt());
            addedAt.setText(formattedDate);
            remove.setOnClickListener(v -> listener.onWishClickedListener(item));
        }

    }
}
