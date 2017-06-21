package gs.maps.nestedscroll.demo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class BottomSheetFragment extends Fragment implements View.OnLayoutChangeListener {
    private ImageView image;
    private View content;
    private Button button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        image = (ImageView) view.findViewById(R.id.image);
        button = (Button) view.findViewById(R.id.chatEnter);
        content = view.findViewById(R.id.content);

        view.addOnLayoutChangeListener(this);
    }

    @Override
    public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        view.removeOnLayoutChangeListener(this);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) content.getLayoutParams();
        final int peekHeight = content.getHeight() + params.topMargin + params.bottomMargin;

        BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(view);
        behavior.setPeekHeight(peekHeight);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            {
                onSlide(null, 0);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                float offset = Math.max(slideOffset, 0);
                float inverseOffset = 1 - offset;
                int imageBottom = image.getBottom();

                image.setAlpha(2.5f * offset - 1.5f);
                image.setTranslationY(-imageBottom * inverseOffset);
                button.setTranslationY((peekHeight / 2 - imageBottom) * inverseOffset);
                content.setTranslationY(-imageBottom * inverseOffset);
            }

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

        });
    }

}
