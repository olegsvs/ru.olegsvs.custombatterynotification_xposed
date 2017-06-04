package ru.olegsvs.custombatterynotifyxposed;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class IconSelectionView extends LinearLayout implements AdapterView.OnItemSelectedListener {
    private Context mContext;
    private int[] mIcons;
    private ImageView mImageView;
    private TextView mTextView;
    private Spinner mSpinner;
    private IconSelectionListener mListener;
    private String mKey;
    private SharedPreferences mPreferences;

    public IconSelectionView(Context context) {
        super(context);

        init(context);
    }

    public IconSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public IconSelectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        mContext = context;

        inflate(context, R.layout.icon_selection, this);
        mImageView = (ImageView) findViewById(R.id.icon_preview);
        mTextView = (TextView) findViewById(R.id.title);
        mSpinner = (Spinner) findViewById(R.id.icon_choose);
        setOrientation(VERTICAL);
    }

    public void setListener(IconSelectionListener listener) {
        mListener = listener;
    }

    public void setSelection(int position) {
        mSpinner.setSelection(position);
    }

    public void setTitle(String title) {
        mTextView.setText(title);
    }

    public void setStrings(String[] strings) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(mContext,
                        android.R.layout.simple_spinner_dropdown_item,
                        strings);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this);
    }

    public void setIcons(int[] iconIds) {
        mIcons = iconIds.clone();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mImageView.setImageResource(mIcons[position]);
        mPreferences.edit().putInt(mKey, position).apply();
        if (mListener != null)
            mListener.onSelect(this, position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void setKey(String key) {
        mKey = key;
        mSpinner.setSelection(mPreferences.getInt(mKey, 0));
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        mPreferences = sharedPreferences;
    }

    public interface IconSelectionListener {
        void onSelect(IconSelectionView view, int position);
    }
}
