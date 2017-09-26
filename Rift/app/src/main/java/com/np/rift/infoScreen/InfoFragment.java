package com.np.rift.infoScreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.np.rift.AppController;
import com.np.rift.R;
import com.np.rift.init.LoginActivity;
import com.np.rift.util.SharedPrefUtil;

public class InfoFragment extends Fragment {
    private static final String AboutApp = "AboutSplash";
    private static final String Background = "SplashBackground";
    private static final String ContextImage = "SplashTheme";
    private static final String AboutAppHeader = "AboutAppHeader";
    private static final String Position = "Position";
    SharedPrefUtil sharedPrefUtil = new SharedPrefUtil();

    public static InfoFragment newInstance(int position, String messageBody, String messageHead,
                                           int backgroundColor, int contextImage) {
        InfoFragment splashFragment = new InfoFragment();
        Bundle bundle = new Bundle(); //Store all the information coming from the Activity

        bundle.putString(AboutApp, messageBody);
        bundle.putInt(Background, backgroundColor);
        bundle.putInt(ContextImage, contextImage);
        bundle.putInt(Position, position);
        bundle.putString(AboutAppHeader, messageHead);
        splashFragment.setArguments(bundle);
        return splashFragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //get all the information from the activity via bundle
        String messageBody = getArguments().getString(AboutApp);
        int text_color = getArguments().getInt(Background);
        int contextImage = getArguments().getInt(ContextImage);
        int position = getArguments().getInt(Position);
        String messageHeader = getArguments().getString(AboutAppHeader);

        View view = inflater.inflate(R.layout.info_fragment_layout, container, false); //Link the fragment with layout
        LinearLayout linearLayout = view.findViewById(R.id.background_layout);
        TextView messageTV = view.findViewById(R.id.textView_body);
        TextView textView_letsrift = view.findViewById(R.id.textView_letsrift);
        ImageView contextImageIV = view.findViewById(R.id.imageView_context);
        Button proceed = view.findViewById(R.id.button_proceed);

        if (position == 2) { // Display the proceed button at the end of last fragment
            proceed.setText(getString(R.string.proceed));
            proceed.setVisibility(View.VISIBLE);
            textView_letsrift.setVisibility(View.VISIBLE);
        } else {
            proceed.setVisibility(View.INVISIBLE);
            textView_letsrift.setVisibility(View.INVISIBLE);
        }

        messageTV.setTextColor(text_color);
        linearLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.splashBG));
        messageTV.setText(messageBody);
        contextImageIV.setBackgroundResource(contextImage);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPrefUtil.setSharedPreferenceBoolean(AppController.getContext(),
                        "newApp", false);
                startActivity(new Intent(getActivity(), LoginActivity.class)); //Proceed to the LoginActivity
                getActivity().finish(); //Kills the Activity, onBackpress after here will take user to the Android Application Drawer
            }
        });
        return view;
    }
}
