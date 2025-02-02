package cheema.hardeep.sahibdeep.brotherhood.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cheema.hardeep.sahibdeep.brotherhood.Brotherhood;
import cheema.hardeep.sahibdeep.brotherhood.R;
import cheema.hardeep.sahibdeep.brotherhood.activities.ActorActivity;
import cheema.hardeep.sahibdeep.brotherhood.activities.GenreActivity;
import cheema.hardeep.sahibdeep.brotherhood.adapters.ActorAdapter;
import cheema.hardeep.sahibdeep.brotherhood.adapters.GenreAdapter;
import cheema.hardeep.sahibdeep.brotherhood.database.UserInfoManager;
import cheema.hardeep.sahibdeep.brotherhood.models.UserInfo;

public class SettingsFragment extends Fragment {

    @BindView(R.id.username)
    EditText username;

    @BindView(R.id.genreEdit)
    TextView genreEdit;

    @BindView(R.id.actorEdit)
    TextView actorEdit;

    @BindView(R.id.noGenreMessage)
    TextView noGenreMessage;

    @BindView(R.id.noActorsMessage)
    TextView noActorMessage;

    @BindView(R.id.settingsGenresRecyclerView)
    RecyclerView genreRecyclerView;

    @BindView(R.id.settingsActorsRecyclerView)
    RecyclerView actorRecyclerView;

    private GenreAdapter genreAdapter;
    private ActorAdapter actorAdapter;

    @Inject
    UserInfoManager userInfoManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Brotherhood) getActivity().getApplication()).getBrotherhoodComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        handleClickListeners();
        setupGenreRecyclerView();
        setupActorRecyclerView();
        return view;
    }

    private void handleClickListeners() {
        genreEdit.setOnClickListener(v -> startActivity(GenreActivity.createIntent(getContext())));
        actorEdit.setOnClickListener(v -> startActivity(ActorActivity.createIntent(getContext())));
    }

    private void setupGenreRecyclerView() {
        genreAdapter = new GenreAdapter(false);
        genreRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        genreRecyclerView.setAdapter(genreAdapter);
    }

    private void setupActorRecyclerView() {
        actorAdapter = new ActorAdapter(false);
        actorRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        actorRecyclerView.setAdapter(actorAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        UserInfo userInfo = userInfoManager.getFullUserInfo();
        username.setText(userInfo.getName());
        handleGenresUserPreferences(userInfo);
        handleUserActorsPreferences(userInfo);
    }

    private void handleGenresUserPreferences(UserInfo userInfo) {
        if (userInfo.getGenres().isEmpty()) {
            genreRecyclerView.setVisibility(View.GONE);
            noGenreMessage.setVisibility(View.VISIBLE);
        } else {
            noGenreMessage.setVisibility(View.GONE);
            genreRecyclerView.setVisibility(View.VISIBLE);
            genreAdapter.update(userInfo.getGenres());
        }
    }

    private void handleUserActorsPreferences(UserInfo userInfo) {
        if (userInfo.getActors().isEmpty()) {
            actorRecyclerView.setVisibility(View.GONE);
            noActorMessage.setVisibility(View.VISIBLE);
        } else {
            noActorMessage.setVisibility(View.GONE);
            actorRecyclerView.setVisibility(View.VISIBLE);
            actorAdapter.update(userInfo.getActors());
        }
    }
}
