package com.example.foodorderapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.foodorderapp.Interface.ItemClickListener;
import com.example.foodorderapp.Model.Food;
import com.example.foodorderapp.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;



    String categoryId="";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    // Search Functionality
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestionList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);


        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        recyclerView = (RecyclerView) findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Get Intent here
        if (getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if (!categoryId.isEmpty() && categoryId != null) {
            loadListFood(categoryId);
        }


    // Search
    materialSearchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
    materialSearchBar.setHint("What are you looking for?");
    loadSuggestions(); // Write function to load suggestions from Firebase
    materialSearchBar.setLastSuggestions(suggestionList);
    materialSearchBar.setCardViewElevation(10);
    materialSearchBar.addTextChangeListener(new TextWatcher() {
        @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // As user types, this will change the suggestion list

            List<String> suggest = new ArrayList<>();
            for(String search:suggestionList) {
                if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                    suggest.add(search);
            }
            materialSearchBar.setLastSuggestions(suggest);
        }

        @Override
                public void afterTextChanged(Editable editable) {

        }
    });

    materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener()

    {
        @Override
        public void onSearchStateChanged( boolean enabled) {
        // When Search Bar is closed, restore original adapter
        if (!enabled)
            recyclerView.setAdapter(adapter);
    }
        @Override
        public void onSearchConfirmed(CharSequence text){
        // When search is finished, show result of search adapter
        startSearch(text);
    }
        @Override
        public void onButtonClicked( int buttonCode) {

    }
    });
    }

private void startSearch(CharSequence text) {
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(foodList.orderByChild("name").equalTo(text.toString()), Food.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                holder.food_name.setText(model.getName());
                Picasso.get().load(model.getImage())
                        .into(holder.food_image);

                final Food local = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent (FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", searchAdapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });
            }


            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(itemView);
            }
        };

        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);
}

private void loadSuggestions() {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot postSnapshot:snapshot.getChildren()) {
                            Food item = postSnapshot.getValue(Food.class);
                            suggestionList.add(item.getName()); // Add name of food suggestion list
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
}
    private void loadListFood(String categoryID) {
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(foodList.orderByChild("menuId").equalTo(categoryID), Food.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {
               viewHolder.food_name.setText(model.getName());
               Picasso.get().load(model.getImage())
                       .into(viewHolder.food_image);
               final Food local = model;
               viewHolder.setItemClickListener(new ItemClickListener() {
                   @Override
                   public void onClick(View view, int position, boolean isLongClick) {
                       //Start New Activity
                       Intent foodDetail = new Intent (FoodList.this, FoodDetail.class);
                               foodDetail.putExtra("FoodId", adapter.getRef(position).getKey());
                               startActivity(foodDetail);

                   }
               });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(itemView);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
    }