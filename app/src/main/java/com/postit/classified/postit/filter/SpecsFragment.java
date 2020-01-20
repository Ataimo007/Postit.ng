package com.postit.classified.postit.filter;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.jaygoo.widget.SeekBar;
import com.postit.classified.postit.R;
import com.postit.classified.postit.helpers.Helper;

import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class SpecsFragment extends Fragment
{
    private View view;
    private RangeSeekBar priceSlider;
    private float leftPrice = 30000;
    private float rightPrice = 700000;

    private final NumberFormat format = NumberFormat.getInstance(Locale.US);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.spec_info, container, false);
        initSliders();
        return view;
    }

    private void initSliders() {
        priceSlider = view.findViewById(R.id.price_range);
        priceSlider.setIndicatorTextDecimalFormat("0,000.00");
        priceSlider.setValue( priceSlider.getMinProgress(), priceSlider.getMaxProgress() );
        leftPrice = priceSlider.getMinProgress();
        rightPrice = priceSlider.getMaxProgress();

        SeekBar leftSeekBar = priceSlider.getLeftSeekBar();
        SeekBar rightSeekBar = priceSlider.getRightSeekBar();
        TextInputEditText left = view.findViewById(R.id.price_min_field);
        TextInputEditText right = view.findViewById(R.id.price_max_field);

        left.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();

                left.removeTextChangedListener( this );
                Helper.updateNumberField( left, query );

                try {
                    float value = format.parse(query).floatValue();
                    if ( value != leftPrice && value >= 0 && value <= priceSlider.getMaxProgress() )
                    {
                        leftPrice = value;
                        priceSlider.setValue( value, rightPrice >= 0 ? rightPrice : 0 );
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                left.addTextChangedListener( this );
            }
        });

        right.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();

                right.removeTextChangedListener( this );
                Helper.updateNumberField( right, query );

                try {
                    float value = format.parse(query).floatValue();
                    if ( value != rightPrice && value >= 0 && value <= priceSlider.getMaxProgress()  )
                    {
                        rightPrice = value;
                        priceSlider.setValue( leftPrice >= 0 ? leftPrice : 0, value );
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                right.addTextChangedListener( this );
            }
        });

        priceSlider.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                if ( isFromUser )
                {
                    leftPrice = leftValue;
                    rightPrice = rightValue;

                    try {
                        String leftField = left.getText().toString();
                        if ( leftField.isEmpty()
                                || format.parse(leftField).floatValue() != leftValue )
                            left.setText( String.format( "%,.2f", leftValue ) );

                        String rightField = right.getText().toString();
                        if ( rightField.isEmpty()
                                || format.parse(rightField).floatValue() != rightValue )
                            right.setText( String.format( "%,.2f", rightValue ) );
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });
    }

    public JsonObject getProperties()
    {
        JsonObject params = new JsonObject();
        if ( leftPrice != priceSlider.getMinProgress() )
            params.addProperty( "price_min", leftPrice );
        if ( rightPrice != priceSlider.getMaxProgress() )
            params.addProperty( "price_max", rightPrice );
        addRadioGroup( params, R.id.ad_type_group, "type" );
        addRadioGroup( params, R.id.ad_conditions, "ad_condition" );
        addCheckedValue( params, R.id.ad_negotiable );
        return params;
    }

    private void addCheckedValue(JsonObject params, int checkedId)
    {
        CheckBox negotiable = view.findViewById( checkedId );
        if (negotiable.isChecked())
            params.addProperty( "is_negotiable", 1 );
    }

    private void addRadioGroup(JsonObject params, int groupId, String name)
    {
        RadioGroup typeGroup = view.findViewById(groupId);
        int type = typeGroup.getCheckedRadioButtonId();
        RadioButton radioValue = view.findViewById(type);
        if ( type != -1 ) {
            params.addProperty(name, radioValue.getText().toString() );
        }
    }
}
