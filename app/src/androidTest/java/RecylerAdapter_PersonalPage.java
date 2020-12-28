import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shroomies.R;


// only class responsible for displaying personal card
// items in my recycler view
// Creates the rows and maps items to those rows
public class RecylerAdapter_PersonalPage extends RecyclerView.Adapter<RecylerAdapter_PersonalPage.ViewHolder> {



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {



        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.personal_post_custom_card,parent,false);
        //this view holder takes the view of the row
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }


    //Returns the number of rows
    @Override
    public int getItemCount() {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView IV_userPic;
        TextView TV_userName;
        TextView TV_userOccupation;
        TextView TV_userBudget;
        TextView TV_DatePosted;
        TextView TV_userDescription;
        RelativeLayout Lay_REL;


    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        IV_userPic = itemView.findViewById(R.id.user_image_personal_card);
        TV_userName = itemView.findViewById(R.id.user_name_personal_card);
        TV_userOccupation = itemView.findViewById(R.id.bio_personal_card);
        TV_userBudget = itemView.findViewById(R.id.personal_post_budget_text_view);
        TV_DatePosted = itemView.findViewById(R.id.personal_post_date_text_view);
        TV_userDescription = itemView.findViewById(R.id.personal_card_text_view);
        Lay_REL = itemView.findViewById(R.id.relative_layout_personal_card);

    }
}
}
