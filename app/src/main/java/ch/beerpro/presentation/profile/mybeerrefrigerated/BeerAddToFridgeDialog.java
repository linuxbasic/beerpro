package ch.beerpro.presentation.profile.mybeerrefrigerated;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.beerpro.R;
import ch.beerpro.domain.models.Beer;
import ch.beerpro.presentation.details.createrating.CreateRatingViewModel;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class BeerAddToFridgeDialog extends BottomSheetDialogFragment{
    private String beerId;
    public static final String BEER_ID = "beer_id";
    public static final String AMOUNT = "amount";
    private FridgeViewModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beer_list_dialog, container,
                false);

        model = ViewModelProviders.of(this).get(FridgeViewModel.class);
        BeerAddToFridgeDialog self = this;
        Bundle args = getArguments();
        String beerIdToAdd = args.getString(BEER_ID);
        Integer amount = args.getInt(AMOUNT, 6);
        args.putInt(AMOUNT, amount);

        SeekBar amountSlider = (SeekBar) view.findViewById(R.id.amountSlider);
        TextView amountText = (TextView) view.findViewById(R.id.amountText);
        TextView addToFridgeButton = (Button) view.findViewById(R.id.addToFridgeButton);

        amountSlider.setProgress(amount);
        amountText.setText(Integer.toString(amount));

        amountSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                amountText.setText(Integer.toString(progress));
                args.putInt(AMOUNT, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        addToFridgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer amount = args.getInt(AMOUNT);
                model.saveFridge(beerIdToAdd, amount);
                self.dismiss();
            }
        });

        return view;

    }

}
