package app.philm.in.fragments;

import com.google.common.base.Preconditions;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import app.philm.in.Constants;
import app.philm.in.PhilmApplication;
import app.philm.in.R;
import app.philm.in.controllers.MovieController;
import app.philm.in.fragments.base.BaseDetailFragment;
import app.philm.in.model.PhilmPerson;
import app.philm.in.model.PhilmPersonCredit;
import app.philm.in.util.PhilmCollections;
import app.philm.in.view.MovieDetailCardLayout;
import app.philm.in.view.PhilmImageView;

public class PersonDetailFragment extends BaseDetailFragment
        implements MovieController.PersonUi, View.OnClickListener {

    private static final String LOG_TAG = PersonDetailFragment.class.getSimpleName();

    private static final String KEY_QUERY_PERSON_ID = "person_id";

    public static PersonDetailFragment create(String personId) {
        Preconditions.checkArgument(!TextUtils.isEmpty(personId), "personId cannot be empty");

        Bundle bundle = new Bundle();
        bundle.putString(KEY_QUERY_PERSON_ID, personId);

        PersonDetailFragment fragment = new PersonDetailFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textview_summary:
                final TextView summaryView = (TextView) view;
                final int defaultMaxLines = getResources()
                        .getInteger(R.integer.default_summary_maxlines);
                if (summaryView.getLineCount() == defaultMaxLines) {
                    summaryView.setMaxLines(Integer.MAX_VALUE);
                } else if (summaryView.getLineCount() > defaultMaxLines) {
                    summaryView.setMaxLines(defaultMaxLines);
                }
                break;
        }
    }

    private enum PersonItems implements DetailType {
        TITLE(R.layout.item_person_detail_title),
        BIOGRAPHY(R.layout.item_movie_detail_summary),
        CREDITS_CAST(R.layout.item_movie_detail_generic_card),
        CREDITS_CREW(R.layout.item_movie_detail_generic_card);

        private final int mLayoutId;

        private PersonItems(int layoutId) {
            mLayoutId = layoutId;
        }

        @Override
        public int getLayoutId() {
            return mLayoutId;
        }
    }

    private PhilmPerson mPerson;
    @Inject DateFormat mMediumDateFormatter;
    private Date mDate = new Date();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhilmApplication.from(getActivity()).inject(this);
    }

    @Override
    public MovieController.MovieQueryType getMovieQueryType() {
        return MovieController.MovieQueryType.PERSON_DETAIL;
    }

    @Override
    public String getRequestParameter() {
        return getArguments().getString(KEY_QUERY_PERSON_ID);
    }

    @Override
    public String getUiTitle() {
        if (mPerson != null) {
            return mPerson.getName();
        }
        return null;
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    public void setPerson(PhilmPerson person) {
        mPerson = person;

        populateUi();

        if (hasCallbacks()) {
            getCallbacks().onTitleChanged();
        }
    }

    private void populateUi() {
        if (mPerson == null) {
            return;
        }

        final ArrayList<PersonItems> items = new ArrayList<>();

        items.add(PersonItems.TITLE);

        if (!TextUtils.isEmpty(mPerson.getBiography())) {
            items.add(PersonItems.BIOGRAPHY);
        }

        if (!PhilmCollections.isEmpty(mPerson.getCastCredits())) {
            items.add(PersonItems.CREDITS_CAST);
        }
        if (!PhilmCollections.isEmpty(mPerson.getCrewCredits())) {
            items.add(PersonItems.CREDITS_CREW);
        }

        getListAdapter().setItems(items);
    }

    @Override
    protected ListAdapter createListAdapter() {
        return new PersonAdapter();
    }

    @Override
    protected PersonAdapter getListAdapter() {
        return (PersonAdapter) super.getListAdapter();
    }

    protected class PersonAdapter extends BaseDetailAdapter<PersonItems> {

        @Override
        public int getViewTypeCount() {
            return PersonItems.values().length;
        }

        @Override
        protected void bindView(PersonItems item, View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindView. Item: " + item.name());
            }

            switch (item) {
                case TITLE:
                    bindTitle(view);
                    break;
                case BIOGRAPHY:
                    bindBiography(view);
                    break;
                case CREDITS_CAST:
                    bindCast(view);
                    break;
                case CREDITS_CREW:
                    bindCrew(view);
                    break;
            }

            view.setTag(item);
        }

        private void bindTitle(View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindTitle");
            }

            PhilmImageView imageView = (PhilmImageView) view.findViewById(R.id.imageview_poster);
            imageView.loadProfileUrl(mPerson);

            TextView title = (TextView) view.findViewById(R.id.textview_title);
            title.setText(mPerson.getName());

            TextView bornDate = (TextView) view.findViewById(R.id.textview_born_date);
            if (mPerson.getDateOfBirth() != 0) {
                mDate.setTime(mPerson.getDateOfBirth());

                if (!TextUtils.isEmpty(mPerson.getPlaceOfBirth())) {
                    bornDate.setText(
                            getString(
                                    R.string.person_born_date_with_loc,
                                    mMediumDateFormatter.format(mDate),
                                    mPerson.getPlaceOfBirth(),
                                    mPerson.getAge()
                            )
                    );
                } else {
                    bornDate.setText(
                            getString(
                                    R.string.person_born_date,
                                    mMediumDateFormatter.format(mDate),
                                    mPerson.getAge()
                            )
                    );
                }

                bornDate.setVisibility(View.VISIBLE);
            } else {
                bornDate.setVisibility(View.GONE);
            }
        }

        private void bindBiography(final View view) {
            TextView summary = (TextView) view.findViewById(R.id.textview_summary);
            summary.setText(mPerson.getBiography());
            summary.setOnClickListener(PersonDetailFragment.this);
        }

        private void bindCast(View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindCast");
            }

            final View.OnClickListener seeMoreClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        getCallbacks().showPersonCastCredits(mPerson);
                    }
                }
            };

            CastCreditsAdapter adapter = new CastCreditsAdapter(LayoutInflater.from(getActivity()));

            MovieDetailCardLayout cardLayout = (MovieDetailCardLayout) view;
            cardLayout.setTitle(R.string.cast_movies);

            populateDetailGrid(
                    (ViewGroup) view.findViewById(R.id.card_content),
                    cardLayout,
                    seeMoreClickListener,
                    adapter);
        }

        private void bindCrew(View view) {
            if (Constants.DEBUG) {
                Log.d(LOG_TAG, "bindCrew");
            }

            final View.OnClickListener seeMoreClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        getCallbacks().showPersonCrewCredits(mPerson);
                    }
                }
            };

            CrewCreditsAdapter adapter = new CrewCreditsAdapter(LayoutInflater.from(getActivity()));

            MovieDetailCardLayout cardLayout = (MovieDetailCardLayout) view;
            cardLayout.setTitle(R.string.crew_movies);

            populateDetailGrid(
                    (ViewGroup) view.findViewById(R.id.card_content),
                    cardLayout,
                    seeMoreClickListener,
                    adapter);
        }
    }

    private class CastCreditsAdapter extends BaseCreditAdapter {
        CastCreditsAdapter(LayoutInflater inflater) {
            super(inflater, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        PhilmPersonCredit credit = (PhilmPersonCredit) view.getTag();
                        if (credit != null && credit != null) {
                            getCallbacks().showMovieDetail(credit);
                        }
                    }
                }
            });
        }

        @Override
        public int getCount() {
            return mPerson != null ? PhilmCollections.size(mPerson.getCastCredits()) : 0;
        }

        @Override
        public PhilmPersonCredit getItem(int position) {
            return mPerson.getCastCredits().get(position);
        }
    }

    private class CrewCreditsAdapter extends BaseCreditAdapter {
        CrewCreditsAdapter(LayoutInflater inflater) {
            super(inflater, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasCallbacks()) {
                        PhilmPersonCredit credit = (PhilmPersonCredit) view.getTag();
                        if (credit != null && credit != null) {
                            getCallbacks().showMovieDetail(credit);
                        }
                    }
                }
            });
        }

        @Override
        public int getCount() {
            return mPerson != null ? PhilmCollections.size(mPerson.getCrewCredits()) : 0;
        }

        @Override
        public PhilmPersonCredit getItem(int position) {
            return mPerson.getCrewCredits().get(position);
        }
    }

    private abstract class BaseCreditAdapter extends BaseAdapter {
        private final View.OnClickListener mItemOnClickListener;
        private final LayoutInflater mInflater;

        BaseCreditAdapter(LayoutInflater inflater, View.OnClickListener itemOnClickListener) {
            mInflater = inflater;
            mItemOnClickListener = itemOnClickListener;
        }

        @Override
        public abstract PhilmPersonCredit getItem(int position);

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(getLayoutId(), viewGroup, false);
            }

            final PhilmPersonCredit credit = getItem(position);

            final TextView title = (TextView) view.findViewById(R.id.textview_title);
            title.setText(credit.getTitle());

            final PhilmImageView imageView =
                    (PhilmImageView) view.findViewById(R.id.imageview_poster);
            imageView.loadPosterUrl(credit);

            TextView subTitle = (TextView) view.findViewById(R.id.textview_subtitle);
            subTitle.setText(credit.getJob());

            view.setOnClickListener(mItemOnClickListener);
            view.setTag(credit);

            return view;
        }

        protected int getLayoutId() {
            return R.layout.item_movie_detail_grid_item_2line;
        }
    }
}