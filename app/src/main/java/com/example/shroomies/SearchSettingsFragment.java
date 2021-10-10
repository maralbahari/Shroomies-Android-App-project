package com.example.shroomies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.nfx.android.rangebarpreference.RangeBarPreferenceCompat;

import java.util.List;

public class SearchSettingsFragment extends PreferenceFragmentCompat {
    SwitchPreferenceCompat priceFilterSwitchPreference, preferencesSwitch;
    ListPreference stateDropDownPreference;
    List<String> statesList;
    RangeBarPreferenceCompat rangeBarPreferenceCompat;
    MultiSelectListPreference multiSelectListPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        stateDropDownPreference = findPreference("state_preference");
        rangeBarPreferenceCompat = findPreference("range_preference");
        priceFilterSwitchPreference = findPreference("filter_price");
        preferencesSwitch = findPreference("filter_preferences");
        multiSelectListPreference = findPreference("properties");

        //check if the preferences have been set before
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        boolean filterPrice = prefs.getBoolean("filter_price", false);
        rangeBarPreferenceCompat.setEnabled(filterPrice);

        boolean preferenceEnable = prefs.getBoolean("filter_preferences", false);
        multiSelectListPreference.setEnabled(preferenceEnable);

        preferencesSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            multiSelectListPreference.setEnabled((boolean) newValue);
            return true;
        });

        priceFilterSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            rangeBarPreferenceCompat.setEnabled((boolean) newValue);
            return true;
        });


        //get any previous selected states
        stateDropDownPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            stateDropDownPreference.setSummary((String) newValue);
            return true;
        });


    }
}
