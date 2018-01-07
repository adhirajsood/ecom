package com.heady.headyecom;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.heady.headyecom.models.Categories;
import com.heady.headyecom.models.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomePage extends AppCompatActivity implements IAsyncCallback{

    private TextView mTextMessage;
    private ExpandableListView elv_categories;
    ArrayList<Category> categories;

    DBHandler dbHandler;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    getCategories();
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        dbHandler = new DBHandler(this);

        mTextMessage = (TextView) findViewById(R.id.message);
        elv_categories = (ExpandableListView) findViewById(R.id.elv_categories);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        APIManager.getInstance().sendAsyncCall("GET",this);
    }

    @Override
    public void onSuccessResponse(String successResponse) {

        Gson gson = new Gson();
        Categories categories = gson.fromJson(successResponse,Categories.class);

        categories.getCategories();

        setDataInDB(categories);

        getCategories();

    }

    private void setDataInDB(Categories categories) {
        dbHandler.addData(categories);
    }

    private void getCategories(){
        categories = dbHandler.getCategories();

        Gson gson = new Gson();
        System.out.println("test   "+gson.toJson(categories).toString());

        List<Category> categoryHeader = new ArrayList<>();
        List<Category> childListHashMap = new ArrayList<>();
        for (Category category:categories){
            if (category.getHasChildCount()==1){
                Category category1 = new Category();
                category1.setId(category.getId());
                category1.setName(category.getName());
                categoryHeader.add(category1);
            }
        }

    }

    @Override
    public void onErrorResponse(int errorCode, String errorResponse) {

    }
}
