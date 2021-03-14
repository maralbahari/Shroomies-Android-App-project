package com.example.shroomies;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DropDownPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.nfx.android.rangebarpreference.RangeBarHelper;
import com.nfx.android.rangebarpreference.RangeBarPreferenceCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SearchSettingsFragment extends PreferenceFragmentCompat {
    String responce;
    ListPreference stateDropDownPreference;
    DropDownPreference cityDropDownPreference;
    List<String> statesList;
    RangeBarPreferenceCompat rangeBarPreferenceCompat;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        stateDropDownPreference = findPreference("state_preference");
        cityDropDownPreference = findPreference("city_preference");
        rangeBarPreferenceCompat = findPreference("range_preference");
        
        statesList = new ArrayList<>();

        try {
            responce = loadJSONFromAsset();
            JSONObject jsonObject  = new JSONObject(responce);
            for (Iterator<String> it = jsonObject.keys() ; it.hasNext(); ) {
                String state = it.next();
                statesList.add(state);
            }

        }catch (JSONException e){

        }

        stateDropDownPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                cityDropDownPreference.setVisible(true);
                try {
                    populateCitiesDropDown((String)newValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return true;
            }
        });

        stateDropDownPreference.setEntries(statesList.toArray(new CharSequence[statesList.size()]));
        stateDropDownPreference.setEntryValues(statesList.toArray(new CharSequence[statesList.size()]) );



    }

    private void populateCitiesDropDown(String state) throws JSONException {
        if(responce!=null){
            JSONObject jsonObject  = new JSONObject(responce);
            JSONArray jsonArray = (JSONArray) jsonObject.get(state);
            List<String> citiesList = new ArrayList<>();
            citiesList.add("none");
            for(int i = 0; i < jsonArray.length(); i++){
                citiesList.add(jsonArray.get(i).toString());
            }

            cityDropDownPreference.setEntries(citiesList.toArray(new CharSequence[citiesList.size()]));
            cityDropDownPreference.setEntryValues(citiesList.toArray(new CharSequence[citiesList.size()]) );

        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("malaysian-states-cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
