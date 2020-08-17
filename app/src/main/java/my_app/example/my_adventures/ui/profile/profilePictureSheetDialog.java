package my_app.example.my_adventures.ui.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import my_app.example.my_adventures.R;


public class profilePictureSheetDialog extends BottomSheetDialogFragment {
        private final boolean hasProfilePicture;
        private BottomSheetListener mListener;

        public profilePictureSheetDialog(boolean hasProfilePicture) {
            this.hasProfilePicture = hasProfilePicture;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.activity_profile_bottom_sheet, container, false);
            Button button1 = v.findViewById(R.id.picture_camera);
            Button button2 = v.findViewById(R.id.picture_locally);
            Button button3 = v.findViewById(R.id.button_remove_picture);

            if (!hasProfilePicture){
                button3.setVisibility(View.GONE);
            }
            else{
                button3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onButtonClicked("remove");
                        dismiss();
                    }
                });
            }

            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onButtonClicked("camera");
                    dismiss();
                }
            });

            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onButtonClicked("locally");
                    dismiss();
                }
            });


            return v;
        }

        public interface BottomSheetListener {
            void onButtonClicked(String text);
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            try {
                mListener = (BottomSheetListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() +  " must implement BottomSheetListener");
            }
        }
}
