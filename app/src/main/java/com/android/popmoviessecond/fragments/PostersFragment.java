package com.android.popmoviessecond.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.popmoviessecond.fragments.adapters.ImageAdapter;
import com.android.popmoviessecond.api.MovieAPI;
import com.android.popmoviessecond.R;
import com.android.popmoviessecond.api.model.Movie;
import com.android.popmoviessecond.api.model.response.MovieResponse;
import com.android.popmoviessecond.api.model.response.MovieResult;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Petya Marinova on 04-Mar-18.
 */

public class PostersFragment extends Fragment {
    @BindView(R.id.gridview)
    RecyclerView recyclerView;

    Movie movie;
    private final static String MENU_SELECTED = "selected";
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    public static int index = -1;
    public static int top = -1;
    GridLayoutManager mLayoutManager;

    public PostersFragment() {
    }


    public static PostersFragment newInstance() {


        PostersFragment fragment = new PostersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        pref = getActivity().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_posters, container, false);
        ButterKnife.bind(this, v);
        mLayoutManager = new GridLayoutManager(getActivity(),2);
        recyclerView.setLayoutManager(mLayoutManager);
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
//        index = mLayoutManager.findFirstVisibleItemPosition();
//        View v = recyclerView.getChildAt(0);
//        top = (v == null) ? 0 : (v.getTop() - recyclerView.getPaddingTop());
    }

    @Override
    public void onResume() {
//        if(index != -1)
//        {
//            mLayoutManager.scrollToPositionWithOffset( index, top);
//        }

        if (pref == null) {
            moviesRequest(createAPI().getPopularMovies(MovieAPI.KEY));

        }else {
            switch (pref.getInt(MENU_SELECTED, 1)) {
                case 1:
                    moviesRequest(createAPI().getPopularMovies(MovieAPI.KEY));
                    break;
                case 2:
                    moviesRequest(createAPI().getTopRated(MovieAPI.KEY));
                    break;
                default:
                    moviesRequest(createAPI().getPopularMovies(MovieAPI.KEY));
                    break;
            }
        }

        super.onResume();

    }


    private MovieAPI createAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(MovieAPI.BASE_URL)
                .build();

        return retrofit.create(MovieAPI.class);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void moviesRequest(Observable<MovieResponse> movieResponse) {

        movieResponse.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responses -> {
                    ArrayList<String> images = new ArrayList<>();
                    for (MovieResult img : responses.movieResults) {
                        images.add("http://image.tmdb.org/t/p/w185/" + img.posterPath);

                    }
                    ImageAdapter imageAdapter=new ImageAdapter(getActivity(), images);
                    imageAdapter.setClickListener((view, position) -> {
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        movie = new Movie();
                        movie.setMovie_id(responses.movieResults.get(position).id);
                        movie.setOriginalTitle(responses.movieResults.get(position).originalTitle);
                        movie.setOverview(responses.movieResults.get(position).overview);
                        movie.setReleaseDate(responses.movieResults.get(position).releaseDate);
                        movie.setUserRating(responses.movieResults.get(position).voteAverage);
                        movie.setImageThumb(responses.movieResults.get(position).posterPath);
                        fragmentTransaction.replace(R.id.container_fragments, DetailsFragment.newInstance(movie), DetailsFragment.class.getSimpleName());
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    });
                    recyclerView.setAdapter(imageAdapter);

//                    recyclerView.setOnItemClickListener((parent, v, position, id) -> {
//                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                        movie = new Movie();
//                        movie.setMovie_id(responses.movieResults.get(position).id);
//                        movie.setOriginalTitle(responses.movieResults.get(position).originalTitle);
//                        movie.setOverview(responses.movieResults.get(position).overview);
//                        movie.setReleaseDate(responses.movieResults.get(position).releaseDate);
//                        movie.setUserRating(responses.movieResults.get(position).voteAverage);
//                        movie.setImageThumb("http://image.tmdb.org/t/p/w185/" + responses.movieResults.get(position).posterPath);
//                        fragmentTransaction.replace(R.id.container_fragments, DetailsFragment.newInstance(movie), DetailsFragment.class.getSimpleName());
//                        fragmentTransaction.addToBackStack(null);
//                        fragmentTransaction.commit();
//                    });
                });


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.popular_movies:
                editor.putInt(MENU_SELECTED, 1).apply();
                moviesRequest(createAPI().getPopularMovies(MovieAPI.KEY));
                return true;
            case R.id.rated:
                editor.putInt(MENU_SELECTED, 2).apply();
                moviesRequest(createAPI().getTopRated(MovieAPI.KEY));
                return true;
            case R.id.personal:
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container_fragments, PersonalFragment.newInstance(), PersonalFragment.class.getSimpleName());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}