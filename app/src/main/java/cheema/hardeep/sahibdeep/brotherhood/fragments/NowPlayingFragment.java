package cheema.hardeep.sahibdeep.brotherhood.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.internal.StringUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cheema.hardeep.sahibdeep.brotherhood.Brotherhood;
import cheema.hardeep.sahibdeep.brotherhood.R;
import cheema.hardeep.sahibdeep.brotherhood.adapters.NowPlayingAdapter;
import cheema.hardeep.sahibdeep.brotherhood.api.LocationService;
import cheema.hardeep.sahibdeep.brotherhood.api.MovieApi;
import cheema.hardeep.sahibdeep.brotherhood.database.UserInfoManager;
import cheema.hardeep.sahibdeep.brotherhood.models.Genre;
import cheema.hardeep.sahibdeep.brotherhood.models.GenreResponse;
import cheema.hardeep.sahibdeep.brotherhood.models.Movie;
import cheema.hardeep.sahibdeep.brotherhood.models.NowPlaying;
import cheema.hardeep.sahibdeep.brotherhood.utils.PaginationListener;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static cheema.hardeep.sahibdeep.brotherhood.utils.Constants.COMMA;
import static cheema.hardeep.sahibdeep.brotherhood.utils.Constants.EN_US;
import static cheema.hardeep.sahibdeep.brotherhood.utils.Constants.HI;


public class NowPlayingFragment extends Fragment {

    private static final String NOW_PLAYING_ERROR = "onFailure: Error in getting the Now Playing";

    @Inject
    MovieApi movieApi;

    @Inject
    CompositeDisposable compositeDisposable;

    @Inject
    LocationService locationService;

    @Inject
    UserInfoManager userInfoManager;

    @BindView(R.id.nowPlayingTitle)
    TextView name;

    @BindView(R.id.nowPlayingRV)
    RecyclerView nowPlayingRV;

    @BindView(R.id.nowPlayingProgressBar)
    ProgressBar nowPlayingProgressBar;

    private NowPlayingAdapter nowPlayingAdapter;
    private GenreResponse genreResponse;
    private int currentPage = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Brotherhood) getActivity().getApplication()).getBrotherhoodComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_now_playing, container, false);
        ButterKnife.bind(this, view);

        name.setText(HI + userInfoManager.getUserName());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        nowPlayingRV.setLayoutManager(linearLayoutManager);
        nowPlayingAdapter = new NowPlayingAdapter(locationService);
        nowPlayingRV.setAdapter(nowPlayingAdapter);
        setUpPagination(linearLayoutManager);
        requestNowPlayingMoviesWithGenres();
        return view;
    }

    private void setUpPagination(LinearLayoutManager linearLayoutManager) {
        nowPlayingRV.addOnScrollListener(new PaginationListener(linearLayoutManager) {

            @Override
            protected void loadMoreItems() {
                currentPage++;
                requestNowPlayingMoviesWithPagination();
            }

        });
    }

    private void requestNowPlayingMoviesWithGenres() {
        compositeDisposable.add(Observable.zip(
                movieApi.getNowPlaying(EN_US, 1),
                movieApi.getGenre(EN_US),
                (nowPlaying, genreResponse) -> {
                    this.genreResponse = genreResponse;
                    updateMoviesWithGenreNames(nowPlaying, genreResponse);
                    return nowPlaying;
                }
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(disposable -> setIsProgressBarVisible(true))
                        .doOnTerminate(() -> setIsProgressBarVisible(false))
                        .subscribe(
                                this::handleNowPlayingResponse,
                                throwable -> Log.e(RecommendedFragment.class.getSimpleName(), NOW_PLAYING_ERROR + throwable)
                        )
        );
    }

    private void requestNowPlayingMoviesWithPagination() {
        compositeDisposable.add(
                movieApi.getNowPlaying(EN_US, currentPage)
                        .map(nowPlaying -> {
                            updateMoviesWithGenreNames(nowPlaying, genreResponse);
                            return nowPlaying;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(disposable -> setIsProgressBarVisible(true))
                        .doOnTerminate(() -> setIsProgressBarVisible(false))
                        .subscribe(
                                this::handleAddToNowPlayingResponse,
                                throwable -> Log.e(RecommendedFragment.class.getSimpleName(), NOW_PLAYING_ERROR + throwable)
                        )
        );
    }

    private void updateMoviesWithGenreNames(NowPlaying nowPlaying, GenreResponse genreResponse) {
        for (Movie movie : nowPlaying.getResults()) {
            List<String> genreNames = new ArrayList<>();
            for (long genreId : movie.getGenreIds()) {
                for (Genre genre : genreResponse.getGenres()) {
                    if (genre.getId() == genreId) {
                        genreNames.add(genre.getName());
                    }
                }
            }
            movie.setGenreNames(StringUtil.join(genreNames, COMMA));
        }
    }

    private void setIsProgressBarVisible(boolean visible) {
        nowPlayingProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        nowPlayingRV.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    private void handleNowPlayingResponse(NowPlaying nowPlaying) {
        nowPlayingAdapter.updateDataSet(nowPlaying.getResults());
    }

    private void handleAddToNowPlayingResponse(NowPlaying nowPlaying) {
        nowPlayingAdapter.addToDataSet(nowPlaying.getResults());
    }
}
