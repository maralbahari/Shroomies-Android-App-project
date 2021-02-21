package com.example.shroomies;

import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public interface DataChangedEvent {
        void onDataComplete(Context context  , boolean dataIsComplete , Button publishButton  , TextView warningTextView );
}
