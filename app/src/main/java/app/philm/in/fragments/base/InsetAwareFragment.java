/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.philm.in.fragments.base;

import android.graphics.Rect;
import android.support.v4.app.Fragment;

import java.util.List;

import app.philm.in.BasePhilmActivity;
import app.philm.in.util.PhilmCollections;

public abstract class InsetAwareFragment extends Fragment
        implements BasePhilmActivity.OnActivityInsetsCallback {

    private final Rect mBaseInsets = new Rect();
    private Rect mAdditionalInsets;

    private final Rect mPopulatedInsets = new Rect();

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof BasePhilmActivity) {
            ((BasePhilmActivity) getActivity()).addInsetChangedCallback(this);
        }
    }

    @Override
    public void onPause() {
        if (getActivity() instanceof BasePhilmActivity) {
            BasePhilmActivity activity = ((BasePhilmActivity) getActivity());
            activity.removeInsetChangedCallback(this);
            activity.resetInsets();
        }
        super.onPause();
    }

    @Override
    public final void onInsetsChanged(Rect insets) {
        mBaseInsets.set(insets);
        doPopulateInsets();
    }

    protected void populateInsets(Rect insets) {
    }

    public void setInsetTopAlpha(float alpha) {
        if (getActivity() instanceof BasePhilmActivity) {
            ((BasePhilmActivity) getActivity()).setInsetTopAlpha(alpha);
        }
    }

    public void setAdditionalInsets(final Rect rect) {
        mAdditionalInsets = rect;
        doPopulateInsets();
    }

    protected void propogateAdditionalInsetsToChildren(final Rect rect) {
        final List<Fragment> children = getChildFragmentManager().getFragments();
        if (!PhilmCollections.isEmpty(children)) {
            for (final Fragment fragment : children) {
                if (fragment instanceof InsetAwareFragment) {
                    ((InsetAwareFragment) fragment).setAdditionalInsets(rect);
                }
            }
        }
    }

    private void doPopulateInsets() {
        mPopulatedInsets.set(mBaseInsets);

        if (mAdditionalInsets != null) {
            mPopulatedInsets.left += mAdditionalInsets.left;
            mPopulatedInsets.top += mAdditionalInsets.top;
            mPopulatedInsets.right += mAdditionalInsets.right;
            mPopulatedInsets.bottom += mAdditionalInsets.bottom;
        }


        if (getView() != null) {
            populateInsets(mPopulatedInsets);
        }
    }

    protected Rect getInsets() {
        return mPopulatedInsets;
    }
}
