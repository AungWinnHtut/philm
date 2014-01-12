package app.philm.in.view;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import app.philm.in.Constants;
import app.philm.in.Container;
import app.philm.in.model.PhilmMovie;
import app.philm.in.util.ImageHelper;
import app.philm.in.util.TextUtils;

public class PhilmImageView extends ImageView {

    private static final int TYPE_BACKDROP = 0;
    private static final int TYPE_POSTER = 1;

    private final ImageHelper mImageHelper;

    private int mType;
    private PhilmMovie mMovieToLoad;

    private Callback mCallback;

    public PhilmImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mImageHelper = Container.getInstance(context).getImageHelper();
    }

    public void loadPosterUrl(PhilmMovie movie) {
        loadPosterUrl(movie, null);
    }

    public void loadPosterUrl(PhilmMovie movie, Callback listener) {
        if (!TextUtils.isEmpty(movie.getPosterUrl())) {
            mCallback = listener;
            if (canLoadImage()) {
                loadUrlImmediate(movie, TYPE_POSTER);
            } else {
                mType = TYPE_POSTER;
                mMovieToLoad = movie;
            }
        } else {
            setImageDrawable(null);
        }
    }

    public void loadBackdropUrl(PhilmMovie movie) {
        loadBackdropUrl(movie, null);
    }

    public void loadBackdropUrl(PhilmMovie movie, Callback listener) {
        if (!TextUtils.isEmpty(movie.getPosterUrl())) {
            mCallback = listener;
            if (canLoadImage()) {
                loadUrlImmediate(movie, TYPE_BACKDROP);
            } else {
                mType = TYPE_BACKDROP;
                mMovieToLoad = movie;
            }
        } else {
            setImageDrawable(null);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed && mMovieToLoad != null && canLoadImage()) {
            loadUrlImmediate(mMovieToLoad, mType);
            mMovieToLoad = null;
        }
    }

    private boolean canLoadImage() {
        return getWidth() != 0 && getHeight() != 0;
    }

    private void loadUrlImmediate(final PhilmMovie movie, final int type) {
        String url = null;

        switch (type) {
            case TYPE_BACKDROP:
                url = mImageHelper.getFanartUrl(movie, getWidth());
                break;
            case TYPE_POSTER:
                url = mImageHelper.getPosterUrl(movie, getWidth());
                break;
        }

        if (url != null) {
            Picasso.with(getContext()).load(url).into(this, mCallback);

            if (Constants.DEBUG) {
                Log.d("PhilmImageView", "Loading " + url);
            }
        }
    }

}