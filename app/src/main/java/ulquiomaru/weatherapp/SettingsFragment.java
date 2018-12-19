package ulquiomaru.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class SettingsFragment extends Fragment {

    View view;
    ToggleButton toggleCelsius;
    ToggleButton toggleFahrenheit;
    ToggleButton toggleKmph;
    ToggleButton toggleMiles;
    SharedPreferences sharedPref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_settings, container, false);
        toggleCelsius = view.findViewById(R.id.toggleCelsius);
        toggleFahrenheit = view.findViewById(R.id.toggleFahrenheit);
        toggleKmph = view.findViewById(R.id.toggleKmph);
        toggleMiles = view.findViewById(R.id.toggleMiles);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        toggleCelsius.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggleFahrenheit.setChecked(false);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.key_degree), 0);
                    editor.apply();
                } else {
                    toggleFahrenheit.setChecked(true);
                }
            }
        });

        toggleFahrenheit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggleCelsius.setChecked(false);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.key_degree), 1);
                    editor.apply();
                } else {
                    toggleCelsius.setChecked(true);
                }
            }
        });

        toggleKmph.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggleMiles.setChecked(false);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.key_speed), 0);
                    editor.apply();
                } else {
                    toggleMiles.setChecked(true);
                }
            }
        });

        toggleMiles.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggleKmph.setChecked(false);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.key_speed), 1);
                    editor.apply();
                } else {
                    toggleKmph.setChecked(true);
                }
            }
        });

        // initialize toggles from shared preferences
        int degree = sharedPref.getInt(getString(R.string.key_degree), 0);
        int speed = sharedPref.getInt(getString(R.string.key_speed), 0);
        toggleCelsius.setChecked(degree == 0);
        toggleFahrenheit.setChecked(degree == 1);
        toggleKmph.setChecked(speed == 0);
        toggleMiles.setChecked(speed == 1);

        return view;
    }

}
